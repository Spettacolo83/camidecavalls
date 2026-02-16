package com.followmemobile.camidecavalls.domain.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.followmemobile.camidecavalls.service.LocationForegroundService
import com.followmemobile.camidecavalls.util.EmulatorDetector

/**
 * Android implementation of BackgroundTrackingManager.
 * Uses a foreground service with a persistent notification to keep tracking alive.
 */
actual class BackgroundTrackingManager(
    private val context: Context
) {
    companion object {
        const val PREFS_NAME = "tracking_service_prefs"
        const val KEY_IS_TRACKING = "is_tracking"
        const val KEY_SESSION_ID = "session_id"
        const val KEY_ACCUMULATED_SECONDS = "accumulated_seconds"
        const val KEY_START_TIME_MS = "start_time_ms"
    }

    actual fun startBackgroundTracking(
        stageName: String,
        startTimeMs: Long,
        notificationTitle: String,
        channelName: String,
        sessionId: String,
        routeId: Int?,
        accumulatedSeconds: Long
    ) {
        LocationForegroundService.start(
            context = context,
            stageName = stageName,
            startTimeMs = startTimeMs,
            notificationTitle = notificationTitle,
            channelName = channelName,
            sessionId = sessionId,
            routeId = routeId,
            accumulatedSeconds = accumulatedSeconds
        )
    }

    actual fun updateTrackingInfo(stageName: String) {
        LocationForegroundService.update(context, stageName)
    }

    actual fun stopBackgroundTracking() {
        LocationForegroundService.stop(context)
    }

    actual fun hasBackgroundPermission(): Boolean {
        // On Android 10+ (API 29+), background location requires separate permission
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On older versions, foreground location permission is enough
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    actual fun shouldShowBackgroundPermissionRationale(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false
        }

        val activity = context as? ComponentActivity ?: return false
        return activity.shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    actual fun hasActiveTrackingSession(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_TRACKING, false)
    }

    actual fun getActiveSessionId(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_IS_TRACKING, false)) return null
        return prefs.getString(KEY_SESSION_ID, null)
    }

    actual fun getAccumulatedSeconds(): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_IS_TRACKING, false)) return 0L
        val startTimeMs = prefs.getLong(KEY_START_TIME_MS, 0L)
        val savedAccumulated = prefs.getLong(KEY_ACCUMULATED_SECONDS, 0L)
        if (startTimeMs == 0L) return savedAccumulated
        val elapsedSinceStart = (System.currentTimeMillis() - startTimeMs) / 1000
        return savedAccumulated + elapsedSinceStart
    }

    actual fun isHandlingDatabaseWrites(): Boolean = true

    actual fun isRunningOnEmulator(): Boolean = EmulatorDetector.isEmulator
}
