package com.followmemobile.camidecavalls.domain.service

/**
 * Manages background tracking capabilities on each platform.
 *
 * On Android: Starts/stops a foreground service to keep GPS tracking alive.
 * On iOS: The location manager handles background updates automatically via allowsBackgroundLocationUpdates.
 */
expect class BackgroundTrackingManager {
    /**
     * Start background tracking with a persistent notification (Android)
     * or enable background location updates (iOS).
     *
     * @param stageName The name of the current stage being tracked (for notification)
     * @param startTimeMs The timestamp when tracking started (for duration calculation)
     * @param notificationTitle Localized title for the notification (Android only)
     * @param channelName Localized channel name for the notification (Android only)
     * @param sessionId The tracking session ID (for DB writes in service)
     * @param routeId The route ID being tracked (nullable)
     * @param accumulatedSeconds Seconds already accumulated from previous pause/resume cycles
     */
    fun startBackgroundTracking(
        stageName: String,
        startTimeMs: Long,
        notificationTitle: String,
        channelName: String,
        sessionId: String,
        routeId: Int?,
        accumulatedSeconds: Long
    )

    /**
     * Update the notification with new stage information (Android only).
     * Does nothing on iOS.
     */
    fun updateTrackingInfo(stageName: String)

    /**
     * Stop background tracking and remove the notification.
     */
    fun stopBackgroundTracking()

    /**
     * Check if background location permission is granted.
     * On Android 10+, this requires ACCESS_BACKGROUND_LOCATION.
     * On iOS, this requires "Always" authorization or "When In Use" with background mode.
     */
    fun hasBackgroundPermission(): Boolean

    /**
     * Check if we should show a rationale for background location permission.
     */
    fun shouldShowBackgroundPermissionRationale(): Boolean

    /**
     * Check if there is an active tracking session persisted in the service.
     * On Android: reads from SharedPreferences written by LocationForegroundService.
     * On iOS: always returns false (no service persistence needed).
     */
    fun hasActiveTrackingSession(): Boolean

    /**
     * Get the session ID of the active tracking session from the service.
     * On Android: reads from SharedPreferences.
     * On iOS: always returns null.
     */
    fun getActiveSessionId(): String?

    /**
     * Get the accumulated seconds from the persisted service state.
     * On Android: calculates from SharedPreferences startTimeMs + accumulatedSeconds.
     * On iOS: always returns 0.
     */
    fun getAccumulatedSeconds(): Long

    /**
     * Whether the background service handles writing track points to the database.
     * On Android: returns true (service writes directly to DB).
     * On iOS: returns false (TrackingManager writes to DB via use cases).
     */
    fun isHandlingDatabaseWrites(): Boolean

    /**
     * Whether we're running on an emulator/simulator.
     * Used to relax timestamp-based GPS filters that would reject
     * GPX-replayed locations with non-current timestamps.
     */
    fun isRunningOnEmulator(): Boolean
}
