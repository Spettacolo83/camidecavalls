package com.followmemobile.camidecavalls.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.followmemobile.camidecavalls.domain.service.LocationConfig
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.LocationPriority
import com.followmemobile.camidecavalls.domain.service.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android implementation of LocationService using FusedLocationProviderClient.
 * Optimized for battery consumption and works offline (GPS only).
 */
class AndroidLocationService(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationService {

    private val _locationFlow = kotlinx.coroutines.flow.MutableStateFlow<LocationData?>(null)
    private var mockLocationJob: Job? = null

    // DEBUG: Force mock mode even on real device (set to true for testing)
    private val FORCE_MOCK_GPS = false

    // Detect if running on emulator
    private val isEmulator: Boolean
        get() {
            val fingerprint = Build.FINGERPRINT.lowercase()
            val model = Build.MODEL.lowercase()
            val manufacturer = Build.MANUFACTURER.lowercase()
            val brand = Build.BRAND.lowercase()
            val device = Build.DEVICE.lowercase()
            val product = Build.PRODUCT.lowercase()

            android.util.Log.d("AndroidLocationService", "🔍 Device Info:")
            android.util.Log.d("AndroidLocationService", "  FINGERPRINT: ${Build.FINGERPRINT}")
            android.util.Log.d("AndroidLocationService", "  MODEL: ${Build.MODEL}")
            android.util.Log.d("AndroidLocationService", "  MANUFACTURER: ${Build.MANUFACTURER}")
            android.util.Log.d("AndroidLocationService", "  BRAND: ${Build.BRAND}")
            android.util.Log.d("AndroidLocationService", "  DEVICE: ${Build.DEVICE}")
            android.util.Log.d("AndroidLocationService", "  PRODUCT: ${Build.PRODUCT}")

            return fingerprint.contains("generic") ||
                    fingerprint.contains("unknown") ||
                    model.contains("google_sdk") ||
                    model.contains("emulator") ||
                    model.contains("android sdk built for") ||
                    manufacturer.contains("genymotion") ||
                    manufacturer == "google" && (brand == "google" || brand.startsWith("generic")) ||
                    product.contains("sdk") ||
                    product.contains("emulator") ||
                    product.contains("vbox")
        }

    // Dummy route coordinates (Route 1: Maó - Es Grau)
    // For emulator testing only
    private val dummyRouteCoordinates = listOf(
        Pair(39.8975369760724, 4.25736044721203),
        Pair(39.8974507634291, 4.26061349991107),
        Pair(39.8975754955373, 4.26232961418119),
        Pair(39.8980392039615, 4.26532837419277),
        Pair(39.8995774313929, 4.27108640224583),
        Pair(39.9004019271154, 4.27193067185863),
        Pair(39.9013611356276, 4.28027056371725),
        Pair(39.9043328240847, 4.28427477201588),
        Pair(39.9069875324383, 4.29009779303921),
        Pair(39.9106732672117, 4.29059789693833),
        Pair(39.9154070356275, 4.28571592086467),
        Pair(39.9195154728012, 4.2851675424664),
        Pair(39.9235468637651, 4.28381775651478),
        Pair(39.9296853279228, 4.27660864970012),
        Pair(39.9329450753404, 4.27417781990955),
        Pair(39.9384898579814, 4.26601575481646),
        Pair(39.9432963685023, 4.26711384163608),
        Pair(39.9459782878142, 4.26621141163317)
    )
    private var dummyLocationIndex = 0

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
                _locationFlow.value = locationData
                android.util.Log.d("AndroidLocationService", "📍 Real location update: ${locationData.latitude}, ${locationData.longitude}, accuracy: ${locationData.accuracy}m")
            }
        }
    }

    override val locationUpdates: Flow<LocationData?> = _locationFlow

    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun startTracking(config: LocationConfig) {
        android.util.Log.d("AndroidLocationService", "🚀 startTracking called")
        android.util.Log.d("AndroidLocationService", "🔍 Is Emulator: $isEmulator")
        android.util.Log.d("AndroidLocationService", "🔍 Force Mock GPS: $FORCE_MOCK_GPS")

        if (!hasLocationPermission()) {
            android.util.Log.e("AndroidLocationService", "❌ Location permission not granted")
            throw SecurityException("Location permission not granted")
        }

        android.util.Log.d("AndroidLocationService", "✅ Location permission granted")

        // EMULATOR MODE or FORCED MOCK: Use mock coordinates
        if (isEmulator || FORCE_MOCK_GPS) {
            if (FORCE_MOCK_GPS) {
                android.util.Log.d("AndroidLocationService", "🎮 FORCED MOCK MODE - Using mock route coordinates")
            } else {
                android.util.Log.d("AndroidLocationService", "🎮 EMULATOR DETECTED - Using mock route coordinates")
            }
            startMockLocationUpdates(config.updateIntervalMs)
            return
        }

        // REAL DEVICE MODE: Use actual GPS
        android.util.Log.d("AndroidLocationService", "📱 REAL DEVICE - Using actual GPS")
        android.util.Log.d("AndroidLocationService", "⚙️ Creating LocationRequest with interval=${config.updateIntervalMs}ms, minDistance=${config.minDistanceMeters}m")

        val locationRequest = LocationRequest.Builder(
            mapPriority(config.priority),
            config.updateIntervalMs
        ).apply {
            setMinUpdateIntervalMillis(config.fastestIntervalMs)
            setMinUpdateDistanceMeters(config.minDistanceMeters)

            // For hiking: always use FINE granularity for GPS precision
            setGranularity(Granularity.GRANULARITY_FINE)

            // Don't wait - deliver location immediately for faster initial fix
            setWaitForAccurateLocation(false)

            // Maximum wait time for a location
            setMaxUpdateDelayMillis(config.updateIntervalMs * 2)
        }.build()

        try {
            android.util.Log.d("AndroidLocationService", "📡 Requesting location updates...")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            android.util.Log.d("AndroidLocationService", "✅ Location updates requested successfully")

            // Emulator workaround: Try to get last location immediately
            // This helps when emulator has mock location set but FusedLocationProvider doesn't emit
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    android.util.Log.d("AndroidLocationService", "📍 Got last location (emulator workaround): ${it.latitude}, ${it.longitude}")
                    val locationData = LocationData(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        altitude = if (it.hasAltitude()) it.altitude else null,
                        accuracy = if (it.hasAccuracy()) it.accuracy else null,
                        speed = if (it.hasSpeed()) it.speed else null,
                        bearing = if (it.hasBearing()) it.bearing else null,
                        timestamp = it.time
                    )
                    _locationFlow.value = locationData
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("AndroidLocationService", "❌ SecurityException: ${e.message}")
            throw SecurityException("Location permission not granted")
        } catch (e: Exception) {
            android.util.Log.e("AndroidLocationService", "❌ Error requesting location updates: ${e.message}")
            throw e
        }
    }

    override suspend fun stopTracking() {
        android.util.Log.d("AndroidLocationService", "🛑 stopTracking called")

        // Stop mock location job if running
        mockLocationJob?.cancel()
        mockLocationJob = null

        // Stop real location updates
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            android.util.Log.d("AndroidLocationService", "✅ Location updates removed")
        } catch (e: Exception) {
            android.util.Log.w("AndroidLocationService", "⚠️ Error stopping location updates (may already be stopped): ${e.message}")
        }
    }

    /**
     * Start emitting mock location updates for emulator testing.
     * Simulates walking along Route 1 coordinates.
     */
    private fun startMockLocationUpdates(intervalMs: Long) {
        mockLocationJob?.cancel()
        dummyLocationIndex = 0

        mockLocationJob = CoroutineScope(Dispatchers.Default).launch {
            android.util.Log.d("AndroidLocationService", "🎮 Starting mock location updates every ${intervalMs}ms")

            while (true) {
                val (lat, lon) = dummyRouteCoordinates[dummyLocationIndex]

                val mockLocation = LocationData(
                    latitude = lat,
                    longitude = lon,
                    altitude = 10.0, // Mock altitude
                    accuracy = 5.0f, // Good accuracy
                    speed = 1.2f, // Walking speed ~4.3 km/h
                    bearing = null,
                    timestamp = System.currentTimeMillis()
                )

                _locationFlow.value = mockLocation
                android.util.Log.d("AndroidLocationService", "🎮 Mock location ${dummyLocationIndex + 1}/${dummyRouteCoordinates.size}: $lat, $lon")

                // Move to next point (loop back to start when done)
                dummyLocationIndex = (dummyLocationIndex + 1) % dummyRouteCoordinates.size

                delay(intervalMs)
            }
        }
    }

    override suspend fun getLastKnownLocation(): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            suspendCoroutine { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        val locationData = location?.let {
                            LocationData(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                altitude = if (it.hasAltitude()) it.altitude else null,
                                accuracy = if (it.hasAccuracy()) it.accuracy else null,
                                speed = if (it.hasSpeed()) it.speed else null,
                                bearing = if (it.hasBearing()) it.bearing else null,
                                timestamp = it.time
                            )
                        }
                        continuation.resume(locationData)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }
        } catch (e: SecurityException) {
            null
        }
    }

    /**
     * Map our LocationPriority to Google Play Services Priority
     */
    private fun mapPriority(priority: LocationPriority): Int = when (priority) {
        LocationPriority.HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
        LocationPriority.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        LocationPriority.LOW_POWER -> Priority.PRIORITY_LOW_POWER
    }
}
