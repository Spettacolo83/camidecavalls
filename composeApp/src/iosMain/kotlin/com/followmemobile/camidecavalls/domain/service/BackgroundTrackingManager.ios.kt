package com.followmemobile.camidecavalls.domain.service

import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

/**
 * iOS implementation of BackgroundTrackingManager.
 * On iOS, background location is handled by the CLLocationManager with allowsBackgroundLocationUpdates = true.
 * The blue status bar indicator shows when the app is using location in the background.
 */
actual class BackgroundTrackingManager {
    actual fun startBackgroundTracking(
        stageName: String,
        startTimeMs: Long,
        notificationTitle: String,
        channelName: String,
        sessionId: String,
        routeId: Int?,
        accumulatedSeconds: Long
    ) {
        // iOS handles background tracking through the location manager configuration
        // (allowsBackgroundLocationUpdates = true in IOSLocationService)
        // No additional service needed
    }

    actual fun updateTrackingInfo(stageName: String) {
        // No notification to update on iOS
        // The blue status bar indicator is managed by the system
    }

    actual fun stopBackgroundTracking() {
        // Stopping location updates is handled by IOSLocationService.stopTracking()
    }

    actual fun hasBackgroundPermission(): Boolean {
        val manager = CLLocationManager()
        val status = manager.authorizationStatus
        // "When In Use" with UIBackgroundModes "location" allows background tracking
        // "Always" also works
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
               status == kCLAuthorizationStatusAuthorizedAlways
    }

    actual fun shouldShowBackgroundPermissionRationale(): Boolean {
        // iOS handles permission rationale through the Info.plist usage descriptions
        // The system shows these automatically when requesting permission
        return false
    }

    actual fun hasActiveTrackingSession(): Boolean = false

    actual fun getActiveSessionId(): String? = null

    actual fun getAccumulatedSeconds(): Long = 0L

    actual fun isHandlingDatabaseWrites(): Boolean = false

    actual fun isRunningOnEmulator(): Boolean {
        // On iOS, TARGET_OS_SIMULATOR is set at compile time.
        // The Kotlin/Native compiler sets Platform.osFamily but for runtime detection
        // we check the process info — simulators run on x86_64/arm64 Mac hardware.
        val processInfo = platform.Foundation.NSProcessInfo.processInfo
        val environment = processInfo.environment
        return environment.containsKey("SIMULATOR_DEVICE_NAME")
    }
}
