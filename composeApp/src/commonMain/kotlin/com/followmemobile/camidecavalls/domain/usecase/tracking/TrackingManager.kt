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
        config: LocationConfig = LocationConfig(
            updateIntervalMs = 5000L,      // Update every 5 seconds
            fastestIntervalMs = 2000L,      // But max once every 2 seconds
            minDistanceMeters = 5f,         // Only update if moved 5+ meters
            priority = LocationPriority.BALANCED  // Good accuracy, decent battery
        )
    ) {
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
                        _currentLocation.value = location

                        // Save track point to database (offline)
                        currentSessionId?.let { sessionId ->
                            addTrackPointUseCase(
                                sessionId = sessionId,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                altitude = location.altitude,
                                accuracy = location.accuracy,
                                speed = location.speed,
                                bearing = location.bearing
                            )
                        }

                        // Update tracking state with current session
                        _trackingState.value = TrackingState.Tracking(
                            sessionId = session.id,
                            routeId = routeId,
                            currentLocation = location
                        )
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
