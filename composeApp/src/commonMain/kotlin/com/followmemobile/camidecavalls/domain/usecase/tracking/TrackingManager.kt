package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.service.LocationConfig
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.LocationPriority
import com.followmemobile.camidecavalls.domain.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    private val startTrackingSessionUseCase: StartTrackingSessionUseCase,
    private val stopTrackingSessionUseCase: StopTrackingSessionUseCase,
    private val addTrackPointUseCase: AddTrackPointUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val scope: CoroutineScope
) {
    private var trackingJob: Job? = null
    private var currentSessionId: String? = null

    // Track last location for speed calculation
    private var lastLocationForSpeed: LocationData? = null

    private val _trackingState = MutableStateFlow<TrackingState>(TrackingState.Idle)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation.asStateFlow()

    /**
     * Start a new tracking session with battery-optimized settings.
     * All data is saved locally and works offline.
     *
     * @param routeId Optional route ID if tracking a specific stage
     * @param config Location configuration for battery optimization
     */
    suspend fun startTracking(
        routeId: Int? = null,
        config: LocationConfig = LocationConfig()  // Use defaults: minDistance=0f, HIGH_ACCURACY
    ) {
        // Reset any previous Completed/Error state to allow starting a new session
        if (_trackingState.value is TrackingState.Completed ||
            _trackingState.value is TrackingState.Error) {
            _trackingState.value = TrackingState.Idle
        }

        // Check if already tracking
        if (_trackingState.value is TrackingState.Tracking) {
            return
        }

        // Check permissions
        if (!locationService.hasLocationPermission()) {
            _trackingState.value = TrackingState.Error("Location permission not granted")
            return
        }

        if (!locationService.isLocationEnabled()) {
            _trackingState.value = TrackingState.Error("Location services disabled")
            return
        }

        try {
            // Create new tracking session in database
            val session = startTrackingSessionUseCase(routeId)
            currentSessionId = session.id

            // Start location service
            locationService.startTracking(config)

            // Collect location updates and save to database
            trackingJob = scope.launch {
                locationService.locationUpdates
                    .filterNotNull()
                    .collect { location ->
                        // Calculate speed from consecutive GPS points
                        val calculatedSpeed = calculateSpeedFromLastLocation(location)
                        val locationWithSpeed = location.copy(speed = calculatedSpeed)

                        _currentLocation.value = locationWithSpeed

                        // Save track point to database (offline)
                        currentSessionId?.let { sessionId ->
                            addTrackPointUseCase(
                                sessionId = sessionId,
                                latitude = locationWithSpeed.latitude,
                                longitude = locationWithSpeed.longitude,
                                altitude = locationWithSpeed.altitude,
                                accuracy = locationWithSpeed.accuracy,
                                speed = locationWithSpeed.speed,
                                bearing = locationWithSpeed.bearing
                            )
                        }

                        // Update tracking state with current session
                        _trackingState.value = TrackingState.Tracking(
                            sessionId = session.id,
                            routeId = routeId,
                            currentLocation = locationWithSpeed
                        )

                        // Store this location for next speed calculation
                        lastLocationForSpeed = locationWithSpeed
                    }
            }

            _trackingState.value = TrackingState.Tracking(
                sessionId = session.id,
                routeId = routeId,
                currentLocation = null
            )

        } catch (e: Exception) {
            _trackingState.value = TrackingState.Error(e.message ?: "Failed to start tracking")
            stopTracking()
        }
    }

    /**
     * Stop the current tracking session.
     * Calculates final statistics and saves everything to database.
     *
     * @param notes Optional notes to save with the session
     */
    suspend fun stopTracking(notes: String = "") {
        trackingJob?.cancel()
        trackingJob = null

        try {
            locationService.stopTracking()

            currentSessionId?.let { sessionId ->
                // Stop session and calculate final statistics
                val finalSession = stopTrackingSessionUseCase(sessionId, notes)
                _trackingState.value = TrackingState.Completed(finalSession)
            }
        } catch (e: Exception) {
            _trackingState.value = TrackingState.Error(e.message ?: "Failed to stop tracking")
        } finally {
            currentSessionId = null
            _currentLocation.value = null
            lastLocationForSpeed = null // Reset for next session
        }
    }

    /**
     * Resume tracking if there's an active session (e.g., after app restart)
     */
    suspend fun resumeIfActive() {
        val activeSession = getActiveSessionUseCase().first()
        if (activeSession != null) {
            currentSessionId = activeSession.id
            startTracking(activeSession.routeId)
        }
    }

    /**
     * Get last known location without starting tracking (doesn't consume battery)
     */
    suspend fun getLastLocation(): LocationData? {
        return locationService.getLastKnownLocation()
    }

    /**
     * Reset tracking state to Idle.
     * Used when navigating to tracking screen to clear previous Completed/Error states.
     */
    fun resetToIdle() {
        if (_trackingState.value is TrackingState.Completed ||
            _trackingState.value is TrackingState.Error) {
            _trackingState.value = TrackingState.Idle
        }
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
    data object Idle : TrackingState

    data class Tracking(
        val sessionId: String,
        val routeId: Int?,
        val currentLocation: LocationData?
    ) : TrackingState

    data class Completed(
        val session: TrackingSession?
    ) : TrackingState

    data class Error(
        val message: String
    ) : TrackingState
}
