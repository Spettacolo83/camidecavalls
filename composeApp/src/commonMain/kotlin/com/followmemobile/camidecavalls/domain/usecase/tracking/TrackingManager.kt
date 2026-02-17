package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import com.followmemobile.camidecavalls.domain.service.BackgroundTrackingManager
import com.followmemobile.camidecavalls.domain.service.LocationConfig
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Manages GPS tracking sessions with battery optimization and offline support.
 *
 * Features:
 * - Battery optimized tracking (configurable intervals and accuracy)
 * - Works completely offline (GPS only, no network required)
 * - All data saved locally in SQLDelight database
 * - Automatic statistics calculation
 * - Pause detection (when user stops moving)
 */
class TrackingManager(
    private val locationService: LocationService,
    private val backgroundTrackingManager: BackgroundTrackingManager,
    private val trackingRepository: TrackingRepository,
    private val startTrackingSessionUseCase: StartTrackingSessionUseCase,
    private val stopTrackingSessionUseCase: StopTrackingSessionUseCase,
    private val addTrackPointUseCase: AddTrackPointUseCase,
    private val scope: CoroutineScope
) {
    private var trackingJob: Job? = null
    private var timerJob: Job? = null
    private var currentSessionId: String? = null
    private var currentRouteId: Int? = null
    private var currentConfig: LocationConfig = LocationConfig()

    // Track last location for speed calculation
    private var lastLocationForSpeed: LocationData? = null

    // Filter stale/inaccurate first GPS fix after tracking start
    private var hasAcceptedFirstLocation: Boolean = false

    // Duration tracking
    private var trackingStartTime: Long = 0L
    private var accumulatedSeconds: Long = 0L

    // Stage name for background notification (set externally before starting tracking)
    private var currentStageName: String = ""

    // Localized notification strings (set externally before starting tracking)
    private var notificationTitle: String = "GPS tracking active"
    private var notificationChannelName: String = "GPS Tracking"

    /**
     * Set the stage name to display in the background notification.
     * Call this before startTracking() with the stage/route name.
     */
    fun setStageName(name: String) {
        currentStageName = name
        // If already tracking, update the notification
        if (_trackingState.value is TrackingState.Recording) {
            backgroundTrackingManager.updateTrackingInfo(name)
        }
    }

    /**
     * Set localized notification strings for the background service.
     * Call this before startTracking().
     */
    fun setNotificationStrings(title: String, channelName: String) {
        notificationTitle = title
        notificationChannelName = channelName
    }

    /**
     * Calculate current elapsed seconds including accumulated time from pauses
     */
    private fun calculateElapsedSeconds(): Long {
        if (trackingStartTime == 0L) return accumulatedSeconds
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return accumulatedSeconds + (currentTime - trackingStartTime) / 1000
    }

    /**
     * Start a timer that updates the tracking state every second.
     * This ensures the duration updates smoothly, independent of GPS updates.
     */
    private fun startDurationTimer(sessionId: String, routeId: Int?) {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000L)
                val currentState = _trackingState.value
                if (currentState is TrackingState.Recording) {
                    _trackingState.value = currentState.copy(
                        elapsedSeconds = calculateElapsedSeconds()
                    )
                }
            }
        }
    }

    /**
     * Stop the duration timer.
     */
    private fun stopDurationTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private val _trackingState = MutableStateFlow<TrackingState>(TrackingState.Stopped)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation.asStateFlow()

    private val _activeTrackPoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val activeTrackPoints: StateFlow<List<TrackPoint>> = _activeTrackPoints.asStateFlow()

    /**
     * Start a new tracking session with battery-optimized settings.
     * All data is saved locally and works offline.
     *
     * @param routeId Optional route ID if tracking a specific stage
     * @param config Location configuration for battery optimization
     */
    suspend fun startTracking(
        routeId: Int? = null,
        config: LocationConfig = LocationConfig()
    ) {
        if (_trackingState.value is TrackingState.Recording) {
            return
        }

        if (_trackingState.value is TrackingState.Completed ||
            _trackingState.value is TrackingState.Error) {
            _trackingState.value = TrackingState.Stopped
        }

        if (_trackingState.value !is TrackingState.Stopped) {
            return
        }

        if (!locationService.hasLocationPermission()) {
            _trackingState.value = TrackingState.Error("Location permission not granted")
            return
        }

        if (!locationService.isLocationEnabled()) {
            _trackingState.value = TrackingState.Error("Location services disabled")
            return
        }

        try {
            val session = startTrackingSessionUseCase(routeId)
            currentSessionId = session.id
            currentRouteId = routeId
            currentConfig = config

            // Initialize duration tracking
            trackingStartTime = Clock.System.now().toEpochMilliseconds()
            accumulatedSeconds = 0L

            // Start background tracking service (Android foreground service / iOS background mode)
            backgroundTrackingManager.startBackgroundTracking(
                stageName = currentStageName,
                startTimeMs = trackingStartTime,
                notificationTitle = notificationTitle,
                channelName = notificationChannelName,
                sessionId = session.id,
                routeId = routeId,
                accumulatedSeconds = 0L
            )

            startLocationUpdates(
                sessionId = session.id,
                routeId = routeId,
                resetLocation = true
            )

            // Start the duration timer for smooth updates
            startDurationTimer(session.id, routeId)
        } catch (e: Exception) {
            _trackingState.value = TrackingState.Error(e.message ?: "Failed to start tracking")
        }
    }

    private suspend fun startLocationUpdates(
        sessionId: String,
        routeId: Int?,
        resetLocation: Boolean
    ) {
        if (resetLocation) {
            _activeTrackPoints.value = emptyList()
            lastLocationForSpeed = null
            hasAcceptedFirstLocation = false
        }

        locationService.startTracking(currentConfig)

        trackingJob?.cancel()
        trackingJob = scope.launch {
            locationService.locationUpdates
                .filterNotNull()
                .collect { location ->
                    val accuracy = location.accuracy
                    val onEmulator = backgroundTrackingManager.isRunningOnEmulator()

                    // Filter stale/inaccurate first GPS fix (cached or cell-tower position)
                    if (!hasAcceptedFirstLocation) {
                        // Skip timestamp check on emulator — GPX-replayed locations
                        // have non-current timestamps that would always be rejected
                        if (!onEmulator) {
                            val nowMs = Clock.System.now().toEpochMilliseconds()
                            val ageMs = nowMs - location.timestamp
                            if (ageMs > 10_000L) return@collect
                        }
                        if (accuracy != null && accuracy > 30f) return@collect
                        hasAcceptedFirstLocation = true
                    }

                    // Ongoing accuracy filter: discard any GPS fix with poor accuracy
                    // to prevent spikes from cell-tower/WiFi fallback during the session
                    if (accuracy != null && accuracy > 50f) {
                        return@collect
                    }

                    val calculatedSpeed = calculateSpeedFromLastLocation(location)
                    val locationWithSpeed = location.copy(speed = calculatedSpeed)

                    _currentLocation.value = locationWithSpeed

                    val trackPoint = TrackPoint(
                        latitude = locationWithSpeed.latitude,
                        longitude = locationWithSpeed.longitude,
                        altitude = locationWithSpeed.altitude,
                        timestamp = Clock.System.now(),
                        speedKmh = locationWithSpeed.speed?.times(3.6)
                    )

                    val isDuplicate = _activeTrackPoints.value.lastOrNull()?.let { last ->
                        last.latitude == trackPoint.latitude &&
                                last.longitude == trackPoint.longitude
                    } ?: false

                    if (!isDuplicate) {
                        if (backgroundTrackingManager.isHandlingDatabaseWrites()) {
                            // On Android: service writes to DB, we only update in-memory list for UI
                            _activeTrackPoints.update { it + trackPoint }
                        } else {
                            // On iOS: TrackingManager writes to DB via use case
                            try {
                                addTrackPointUseCase(
                                    sessionId = sessionId,
                                    latitude = locationWithSpeed.latitude,
                                    longitude = locationWithSpeed.longitude,
                                    altitude = locationWithSpeed.altitude,
                                    accuracy = locationWithSpeed.accuracy,
                                    speed = locationWithSpeed.speed,
                                    bearing = locationWithSpeed.bearing
                                )
                                _activeTrackPoints.update { it + trackPoint }
                            } catch (e: Exception) {
                                _trackingState.value = TrackingState.Error(
                                    e.message ?: "Failed to save track point"
                                )
                                locationService.stopTracking()
                                return@collect
                            }
                        }
                    }

                    _trackingState.value = TrackingState.Recording(
                        sessionId = sessionId,
                        routeId = routeId,
                        currentLocation = locationWithSpeed,
                        elapsedSeconds = calculateElapsedSeconds()
                    )

                    lastLocationForSpeed = locationWithSpeed
                }
        }

        _trackingState.value = TrackingState.Recording(
            sessionId = sessionId,
            routeId = routeId,
            currentLocation = _currentLocation.value,
            elapsedSeconds = calculateElapsedSeconds()
        )
    }

    suspend fun pauseTracking() {
        val recordingState = _trackingState.value as? TrackingState.Recording ?: return

        // Stop the duration timer
        stopDurationTimer()

        // Stop background tracking service while paused (saves battery)
        backgroundTrackingManager.stopBackgroundTracking()

        // Save accumulated time before pausing
        accumulatedSeconds = calculateElapsedSeconds()
        trackingStartTime = 0L

        trackingJob?.cancel()
        trackingJob = null

        try {
            locationService.stopTracking()
        } catch (_: Exception) {
            // Ignore stop errors while pausing
        }

        _trackingState.value = TrackingState.Paused(
            sessionId = recordingState.sessionId,
            routeId = recordingState.routeId,
            currentLocation = _currentLocation.value,
            elapsedSeconds = accumulatedSeconds
        )
    }

    suspend fun resumeTracking() {
        val pausedState = _trackingState.value as? TrackingState.Paused ?: return

        if (!locationService.hasLocationPermission()) {
            _trackingState.value = TrackingState.Error("Location permission not granted")
            return
        }

        if (!locationService.isLocationEnabled()) {
            _trackingState.value = TrackingState.Error("Location services disabled")
            return
        }

        currentSessionId = pausedState.sessionId
        currentRouteId = pausedState.routeId
        lastLocationForSpeed = _currentLocation.value

        // Restart duration tracking from current time (accumulated time is already saved)
        trackingStartTime = Clock.System.now().toEpochMilliseconds()

        // Restart background tracking service
        // Use original start time minus accumulated seconds to show correct elapsed time in notification
        val originalStartTime = trackingStartTime - (accumulatedSeconds * 1000)
        backgroundTrackingManager.startBackgroundTracking(
            stageName = currentStageName,
            startTimeMs = originalStartTime,
            notificationTitle = notificationTitle,
            channelName = notificationChannelName,
            sessionId = pausedState.sessionId,
            routeId = pausedState.routeId,
            accumulatedSeconds = accumulatedSeconds
        )

        startLocationUpdates(
            sessionId = pausedState.sessionId,
            routeId = pausedState.routeId,
            resetLocation = false
        )

        // Restart the duration timer
        startDurationTimer(pausedState.sessionId, pausedState.routeId)
    }

    /**
     * Stop the current tracking session.
     * Calculates final statistics and saves everything to database.
     *
     * @param name Name for the session (shown in notebook)
     * @param notes Optional notes to save with the session
     */
    suspend fun stopTracking(name: String = "", notes: String = "") {
        // Stop the duration timer
        stopDurationTimer()

        // Stop background tracking service
        backgroundTrackingManager.stopBackgroundTracking()

        trackingJob?.cancel()
        trackingJob = null

        try {
            locationService.stopTracking()
        } catch (_: Exception) {
            // Ignore stop errors when cleaning up
        }

        try {
            val sessionId = currentSessionId
            if (sessionId != null) {
                val finalSession = stopTrackingSessionUseCase(sessionId, name, notes)
                _trackingState.value = TrackingState.Completed(finalSession)
            } else {
                _trackingState.value = TrackingState.Stopped
            }
        } catch (e: Exception) {
            _trackingState.value = TrackingState.Error(e.message ?: "Failed to stop tracking")
        } finally {
            currentSessionId = null
            currentRouteId = null
            lastLocationForSpeed = null
            currentConfig = LocationConfig()
            currentStageName = ""
            notificationTitle = "GPS tracking active"
            notificationChannelName = "GPS Tracking"
        }
    }

    /**
     * Discard the current tracking session.
     * Stops all services and deletes the session from the database.
     */
    suspend fun discardTracking() {
        stopDurationTimer()
        backgroundTrackingManager.stopBackgroundTracking()
        trackingJob?.cancel()
        trackingJob = null

        try {
            locationService.stopTracking()
        } catch (_: Exception) {
            // Ignore stop errors when cleaning up
        }

        try {
            val sessionId = currentSessionId
            if (sessionId != null) {
                trackingRepository.deleteSession(sessionId)
            }
        } catch (_: Exception) {
            // Best effort deletion
        } finally {
            _trackingState.value = TrackingState.Stopped
            _activeTrackPoints.value = emptyList()
            currentSessionId = null
            currentRouteId = null
            lastLocationForSpeed = null
            currentConfig = LocationConfig()
            currentStageName = ""
            notificationTitle = "GPS tracking active"
            notificationChannelName = "GPS Tracking"
        }
    }

    /**
     * Get last known location without starting tracking (doesn't consume battery)
     */
    suspend fun getLastLocation(): LocationData? {
        return locationService.getLastKnownLocation()
    }

    /**
     * Reset tracking state to Stopped.
     * Used when navigating to tracking screen to clear previous Completed/Error states.
     */
    fun resetToStopped(clearTrack: Boolean = false) {
        if (_trackingState.value is TrackingState.Completed ||
            _trackingState.value is TrackingState.Error) {
            _trackingState.value = TrackingState.Stopped
        }

        if (clearTrack) {
            _activeTrackPoints.value = emptyList()
            lastLocationForSpeed = null
            currentConfig = LocationConfig()
            currentSessionId = null
        }
    }

    /**
     * Attempt to restore an active tracking session from the background service.
     *
     * On Android, if the foreground service is still running (or was restarted via START_STICKY),
     * this loads the session and track points from the database, restores in-memory state,
     * and resumes the UI in Recording state.
     *
     * @return true if a session was successfully restored, false otherwise
     */
    suspend fun restoreActiveSession(): Boolean {
        val activeSessionId = backgroundTrackingManager.getActiveSessionId() ?: return false

        val session = trackingRepository.getSessionById(activeSessionId) ?: return false

        // Restore in-memory track points from DB
        val trackPoints = session.trackPoints.map { point ->
            TrackPoint(
                latitude = point.latitude,
                longitude = point.longitude,
                altitude = point.altitude,
                timestamp = point.timestamp,
                speedKmh = point.speedKmh
            )
        }
        _activeTrackPoints.value = trackPoints

        // Restore session state
        currentSessionId = activeSessionId
        currentRouteId = session.routeId

        // Restore accumulated seconds from the persisted service state
        accumulatedSeconds = backgroundTrackingManager.getAccumulatedSeconds()
        trackingStartTime = Clock.System.now().toEpochMilliseconds()
        // Reset accumulated to 0 since trackingStartTime is now — the already-elapsed
        // time is captured in accumulatedSeconds
        // Actually we need trackingStartTime to calculate ongoing elapsed properly:
        // calculateElapsedSeconds() = accumulatedSeconds + (now - trackingStartTime) / 1000
        // Since trackingStartTime = now, the ongoing part starts at 0, plus the already
        // accumulated seconds. This is correct.

        // Set state to Recording
        _trackingState.value = TrackingState.Recording(
            sessionId = activeSessionId,
            routeId = session.routeId,
            currentLocation = _currentLocation.value,
            elapsedSeconds = accumulatedSeconds
        )

        // Start the duration timer
        startDurationTimer(activeSessionId, session.routeId)

        // Start location updates (app-side, for UI updates)
        try {
            startLocationUpdates(
                sessionId = activeSessionId,
                routeId = session.routeId,
                resetLocation = false
            )
        } catch (_: Exception) {
            // Service is handling GPS — app-side updates are best-effort for UI
        }

        return true
    }

    /**
     * Calculate speed from consecutive GPS points using distance and time.
     * More reliable than GPS-provided speed, especially at low speeds.
     *
     * @param currentLocation The new GPS location
     * @return Calculated speed in meters/second, or null if this is the first point
     */
    private fun calculateSpeedFromLastLocation(currentLocation: LocationData): Float? {
        val lastLoc = lastLocationForSpeed ?: return null

        // Calculate time difference in seconds
        val timeDiffMs = currentLocation.timestamp - lastLoc.timestamp
        if (timeDiffMs <= 0) return lastLoc.speed // Same timestamp, keep previous speed

        val timeDiffSeconds = timeDiffMs / 1000.0

        // Don't calculate if too much time has passed (> 30 seconds = probably paused)
        if (timeDiffSeconds > 30) {
            return 0f
        }

        // Calculate distance using Haversine formula
        val distanceMeters = calculateHaversineDistance(
            lat1 = lastLoc.latitude,
            lon1 = lastLoc.longitude,
            lat2 = currentLocation.latitude,
            lon2 = currentLocation.longitude
        )

        // Speed = Distance / Time (m/s)
        val speedMps = (distanceMeters / timeDiffSeconds).toFloat()

        // Sanity check: ignore unrealistic speeds (> 50 m/s = 180 km/h)
        return if (speedMps > 50f) {
            lastLoc.speed // Keep previous speed if unrealistic
        } else {
            speedMps
        }
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula.
     * Returns distance in meters.
     */
    private fun calculateHaversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0 // Earth radius in meters

        // Convert degrees to radians
        val dLat = kotlin.math.PI * (lat2 - lat1) / 180.0
        val dLon = kotlin.math.PI * (lon2 - lon1) / 180.0
        val lat1Rad = kotlin.math.PI * lat1 / 180.0
        val lat2Rad = kotlin.math.PI * lat2 / 180.0

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadiusMeters * c
    }
}

/**
 * Tracking state
 */
sealed interface TrackingState {
    data object Stopped : TrackingState

    data class Recording(
        val sessionId: String,
        val routeId: Int?,
        val currentLocation: LocationData?,
        val elapsedSeconds: Long = 0
    ) : TrackingState

    data class Paused(
        val sessionId: String,
        val routeId: Int?,
        val currentLocation: LocationData?,
        val elapsedSeconds: Long = 0
    ) : TrackingState

    data class Completed(
        val session: TrackingSession?
    ) : TrackingState

    data class Error(
        val message: String
    ) : TrackingState
}
