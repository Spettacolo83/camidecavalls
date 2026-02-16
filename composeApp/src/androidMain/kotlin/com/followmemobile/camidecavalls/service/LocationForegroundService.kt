package com.followmemobile.camidecavalls.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.followmemobile.camidecavalls.MainActivity
import com.followmemobile.camidecavalls.data.local.CamiDatabaseWrapper
import com.followmemobile.camidecavalls.domain.service.BackgroundTrackingManager
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.followmemobile.camidecavalls.util.EmulatorDetector
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Foreground service that owns GPS tracking on Android.
 *
 * This service:
 * - Owns the FusedLocationProviderClient and requests location updates directly
 * - Writes track points to the database on each GPS fix (O(1) insert)
 * - Persists tracking state to SharedPreferences for restart recovery
 * - Exposes location updates via static StateFlow for the app UI to observe
 * - Handles START_STICKY restart by reading persisted state
 *
 * This ensures GPS tracking continues even when Android deprioritizes the app process.
 */
class LocationForegroundService : Service(), KoinComponent {

    private val database: CamiDatabaseWrapper by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var notificationUpdateJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var stageName: String = ""
    private var startTimeMs: Long = 0L
    private var accumulatedSeconds: Long = 0L
    private var notificationTitleText: String = "GPS tracking active"
    private var notificationChannelText: String = "GPS Tracking"
    private var sessionId: String = ""
    private var routeId: Int? = null

    // Filter stale/inaccurate first GPS fix after tracking start
    private var hasAcceptedFirstLocation: Boolean = false

    // Track last saved coordinates to skip duplicates
    private var lastSavedLat: Double = 0.0
    private var lastSavedLon: Double = 0.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val locationData = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else null,
                    accuracy = if (location.hasAccuracy()) location.accuracy else null,
                    speed = if (location.hasSpeed()) location.speed else null,
                    bearing = if (location.hasBearing()) location.bearing else null,
                    timestamp = location.time
                )

                val accuracy = locationData.accuracy

                // Filter stale/inaccurate first GPS fix (cached or cell-tower position)
                if (!hasAcceptedFirstLocation) {
                    // Skip timestamp check on emulator — GPX-replayed locations
                    // have non-current timestamps that would always be rejected
                    if (!EmulatorDetector.isEmulator) {
                        val ageMs = System.currentTimeMillis() - locationData.timestamp
                        if (ageMs > 10_000L) return@let
                    }
                    if (accuracy != null && accuracy > 30f) return@let
                    hasAcceptedFirstLocation = true
                }

                // Ongoing accuracy filter: discard any fix with poor accuracy
                if (accuracy != null && accuracy > 50f) return@let

                // Update static flow for app UI
                _serviceLocationUpdates.value = locationData

                // Write track point directly to DB (O(1) insert), skip duplicates
                if (sessionId.isNotEmpty()) {
                    val isDuplicate = locationData.latitude == lastSavedLat &&
                            locationData.longitude == lastSavedLon
                    if (!isDuplicate) {
                        val speedKmh = locationData.speed?.let { it * 3.6 }
                        try {
                            database.trackPointQueries.insertTrackPoint(
                                sessionId = sessionId,
                                latitude = locationData.latitude,
                                longitude = locationData.longitude,
                                altitude = locationData.altitude,
                                timestamp = locationData.timestamp,
                                speedKmh = speedKmh?.toDouble()
                            )
                            lastSavedLat = locationData.latitude
                            lastSavedLon = locationData.longitude
                        } catch (e: Exception) {
                            // Log but don't crash — next fix will try again
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE = "ACTION_UPDATE"

        const val EXTRA_STAGE_NAME = "EXTRA_STAGE_NAME"
        const val EXTRA_START_TIME = "EXTRA_START_TIME"
        const val EXTRA_NOTIFICATION_TITLE = "EXTRA_NOTIFICATION_TITLE"
        const val EXTRA_CHANNEL_NAME = "EXTRA_CHANNEL_NAME"
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_ROUTE_ID = "EXTRA_ROUTE_ID"
        const val EXTRA_ACCUMULATED_SECONDS = "EXTRA_ACCUMULATED_SECONDS"

        // Static StateFlows for app to observe service location updates
        private val _serviceLocationUpdates = MutableStateFlow<LocationData?>(null)
        val serviceLocationUpdates: StateFlow<LocationData?> = _serviceLocationUpdates.asStateFlow()

        private val _isServiceTracking = MutableStateFlow(false)
        val isServiceTracking: StateFlow<Boolean> = _isServiceTracking.asStateFlow()

        /**
         * Start the foreground service with tracking info.
         */
        fun start(
            context: Context,
            stageName: String,
            startTimeMs: Long,
            notificationTitle: String,
            channelName: String,
            sessionId: String,
            routeId: Int?,
            accumulatedSeconds: Long
        ) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_STAGE_NAME, stageName)
                putExtra(EXTRA_START_TIME, startTimeMs)
                putExtra(EXTRA_NOTIFICATION_TITLE, notificationTitle)
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_ROUTE_ID, routeId ?: -1)
                putExtra(EXTRA_ACCUMULATED_SECONDS, accumulatedSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Update the notification with new info (e.g., when stage changes).
         */
        fun update(context: Context, stageName: String) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_STAGE_NAME, stageName)
            }
            context.startService(intent)
        }

        /**
         * Stop the foreground service.
         */
        fun stop(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                stageName = intent.getStringExtra(EXTRA_STAGE_NAME) ?: ""
                startTimeMs = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                accumulatedSeconds = intent.getLongExtra(EXTRA_ACCUMULATED_SECONDS, 0L)
                notificationTitleText = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE) ?: notificationTitleText
                notificationChannelText = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: notificationChannelText
                sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: ""
                val routeIdExtra = intent.getIntExtra(EXTRA_ROUTE_ID, -1)
                routeId = if (routeIdExtra == -1) null else routeIdExtra

                saveTrackingState()
                createNotificationChannel()
                startForegroundTracking()
            }
            ACTION_UPDATE -> {
                stageName = intent.getStringExtra(EXTRA_STAGE_NAME) ?: stageName
                updateNotification()
            }
            ACTION_STOP -> {
                stopForegroundTracking()
            }
            null -> {
                // START_STICKY restart with null intent — try to recover from SharedPreferences
                if (tryRestoreFromPrefs()) {
                    createNotificationChannel()
                    startForegroundTracking()
                } else {
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundTracking() {
        acquireWakeLock()
        hasAcceptedFirstLocation = false

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startLocationUpdates()
        startNotificationUpdates()

        _isServiceTracking.value = true
    }

    private fun stopForegroundTracking() {
        _isServiceTracking.value = false

        notificationUpdateJob?.cancel()
        notificationUpdateJob = null

        stopLocationUpdates()
        clearTrackingState()
        releaseWakeLock()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 second interval
        ).apply {
            setMinUpdateIntervalMillis(2000L)
            setGranularity(Granularity.GRANULARITY_FINE)
            setWaitForAccurateLocation(false)
            setMaxUpdateDelayMillis(10000L)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Permission was revoked — stop service
            stopForegroundTracking()
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {
            // Ignore errors during cleanup
        }
    }

    private fun saveTrackingState() {
        getPrefs().edit().apply {
            putBoolean(BackgroundTrackingManager.KEY_IS_TRACKING, true)
            putString(BackgroundTrackingManager.KEY_SESSION_ID, sessionId)
            putLong(BackgroundTrackingManager.KEY_START_TIME_MS, startTimeMs)
            putLong(BackgroundTrackingManager.KEY_ACCUMULATED_SECONDS, accumulatedSeconds)
            putString(KEY_STAGE_NAME, stageName)
            putString(KEY_NOTIFICATION_TITLE, notificationTitleText)
            putString(KEY_CHANNEL_NAME, notificationChannelText)
            putInt(KEY_ROUTE_ID, routeId ?: -1)
            apply()
        }
    }

    private fun clearTrackingState() {
        getPrefs().edit().clear().apply()
    }

    private fun tryRestoreFromPrefs(): Boolean {
        val prefs = getPrefs()
        if (!prefs.getBoolean(BackgroundTrackingManager.KEY_IS_TRACKING, false)) return false

        sessionId = prefs.getString(BackgroundTrackingManager.KEY_SESSION_ID, null) ?: return false
        startTimeMs = prefs.getLong(BackgroundTrackingManager.KEY_START_TIME_MS, 0L)
        accumulatedSeconds = prefs.getLong(BackgroundTrackingManager.KEY_ACCUMULATED_SECONDS, 0L)
        stageName = prefs.getString(KEY_STAGE_NAME, "") ?: ""
        notificationTitleText = prefs.getString(KEY_NOTIFICATION_TITLE, notificationTitleText) ?: notificationTitleText
        notificationChannelText = prefs.getString(KEY_CHANNEL_NAME, notificationChannelText) ?: notificationChannelText
        val routeIdVal = prefs.getInt(KEY_ROUTE_ID, -1)
        routeId = if (routeIdVal == -1) null else routeIdVal

        return sessionId.isNotEmpty()
    }

    private fun getPrefs(): SharedPreferences {
        return getSharedPreferences(BackgroundTrackingManager.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "CamiDeCavalls:LocationTracking"
        ).apply {
            // Acquire for a long time (10 hours max as safety)
            acquire(10 * 60 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun startNotificationUpdates() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = serviceScope.launch {
            while (true) {
                delay(5000L) // Update every 5 seconds
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        // Calculate elapsed time including accumulated seconds from pause/resume
        val elapsedMs = System.currentTimeMillis() - startTimeMs
        val totalSeconds = accumulatedSeconds + (elapsedMs / 1000)
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        val timeText = if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }

        val contentText = if (stageName.isNotEmpty()) {
            "$stageName · $timeText"
        } else {
            timeText
        }

        // Intent to open app when notification is tapped
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(notificationTitleText)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                notificationChannelText,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = notificationTitleText
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        _isServiceTracking.value = false
        stopLocationUpdates()
        serviceScope.cancel()
        releaseWakeLock()
        super.onDestroy()
    }
}

// Private SharedPreferences keys used only within the service
private const val KEY_STAGE_NAME = "stage_name"
private const val KEY_NOTIFICATION_TITLE = "notification_title"
private const val KEY_CHANNEL_NAME = "channel_name"
private const val KEY_ROUTE_ID = "route_id"
