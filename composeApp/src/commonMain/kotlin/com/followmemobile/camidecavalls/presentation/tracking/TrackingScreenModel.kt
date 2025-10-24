package com.followmemobile.camidecavalls.presentation.tracking

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingManager
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel for the tracking screen.
 * Manages GPS tracking state and user interactions.
 */
class TrackingScreenModel(
    private val trackingManager: TrackingManager,
    private val permissionHandler: PermissionHandler,
    private val routeId: Int? = null
) : ScreenModel {

    private val _uiState = MutableStateFlow<TrackingUiState>(TrackingUiState.Idle)
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        // Observe tracking state
        screenModelScope.launch {
            trackingManager.trackingState.collect { state ->
                _uiState.value = when (state) {
                    is TrackingState.Idle -> TrackingUiState.Idle
                    is TrackingState.Tracking -> TrackingUiState.Tracking(
                        sessionId = state.sessionId,
                        currentLocation = state.currentLocation
                    )
                    is TrackingState.Completed -> TrackingUiState.Completed(state.session)
                    is TrackingState.Error -> TrackingUiState.Error(state.message)
                }
            }
        }

        // Try to resume active session if exists
        screenModelScope.launch {
            trackingManager.resumeIfActive()
        }
    }

    /**
     * Request location permission
     */
    fun requestPermission(onGranted: () -> Unit) {
        screenModelScope.launch {
            val granted = permissionHandler.requestLocationPermission()
            if (granted) {
                onGranted()
            } else {
                _uiState.value = TrackingUiState.Error(
                    "Location permission is required for tracking. Please grant permission in Settings."
                )
            }
        }
    }

    /**
     * Check if permission is already granted
     */
    fun isPermissionGranted(): Boolean {
        return permissionHandler.isLocationPermissionGranted()
    }

    /**
     * Start tracking (permission must be granted first)
     */
    fun startTracking() {
        screenModelScope.launch {
            try {
                trackingManager.startTracking(routeId = routeId)
            } catch (e: Exception) {
                _uiState.value = TrackingUiState.Error(
                    e.message ?: "Failed to start tracking"
                )
            }
        }
    }

    /**
     * Stop tracking with optional notes
     */
    fun stopTracking(notes: String = "") {
        screenModelScope.launch {
            try {
                trackingManager.stopTracking(notes)
            } catch (e: Exception) {
                _uiState.value = TrackingUiState.Error(
                    e.message ?: "Failed to stop tracking"
                )
            }
        }
    }

    /**
     * Get last known location (doesn't start tracking)
     */
    fun getLastLocation() {
        screenModelScope.launch {
            val location = trackingManager.getLastLocation()
            if (location != null && _uiState.value is TrackingUiState.Idle) {
                _uiState.value = TrackingUiState.Idle // Could add a "preview" state
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        if (_uiState.value is TrackingUiState.Error) {
            _uiState.value = TrackingUiState.Idle
        }
    }
}

/**
 * UI state for tracking screen
 */
sealed interface TrackingUiState {
    data object Idle : TrackingUiState

    data class Tracking(
        val sessionId: String,
        val currentLocation: LocationData?
    ) : TrackingUiState

    data class Completed(
        val session: TrackingSession?
    ) : TrackingUiState

    data class Error(
        val message: String
    ) : TrackingUiState
}
