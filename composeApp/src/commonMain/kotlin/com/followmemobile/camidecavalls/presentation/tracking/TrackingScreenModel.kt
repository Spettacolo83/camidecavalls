package com.followmemobile.camidecavalls.presentation.tracking

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import com.followmemobile.camidecavalls.domain.usecase.route.GetRouteByIdUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.GetActiveSessionUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingManager
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * ScreenModel for the tracking screen.
 * Manages GPS tracking state and user interactions.
 */
class TrackingScreenModel(
    private val trackingManager: TrackingManager,
    private val permissionHandler: PermissionHandler,
    private val getRouteByIdUseCase: GetRouteByIdUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val routeId: Int? = null
) : ScreenModel {

    private val _uiState = MutableStateFlow<TrackingUiState>(TrackingUiState.Idle())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var route: Route? = null

    init {
        // Load route if routeId is provided
        routeId?.let { id ->
            screenModelScope.launch {
                route = getRouteByIdUseCase(id)
                // Update UI state if still in Idle
                if (_uiState.value is TrackingUiState.Idle) {
                    _uiState.value = TrackingUiState.Idle(
                        route = route,
                        currentLocation = trackingManager.currentLocation.value
                    )
                }
            }
        }

        // Observe current location to update Idle state
        screenModelScope.launch {
            trackingManager.currentLocation.collect { location ->
                if (_uiState.value is TrackingUiState.Idle) {
                    _uiState.value = TrackingUiState.Idle(
                        route = route,
                        currentLocation = location
                    )
                }
            }
        }

        // Observe active session for track points
        screenModelScope.launch {
            getActiveSessionUseCase().filterNotNull().collect { session ->
                if (_uiState.value is TrackingUiState.Tracking) {
                    _uiState.value = TrackingUiState.Tracking(
                        route = route,
                        sessionId = session.id,
                        currentLocation = trackingManager.currentLocation.value,
                        trackPoints = session.trackPoints
                    )
                }
            }
        }

        // Observe tracking state
        screenModelScope.launch {
            trackingManager.trackingState.collect { state ->
                _uiState.value = when (state) {
                    is TrackingState.Idle -> TrackingUiState.Idle(
                        route = route,
                        currentLocation = trackingManager.currentLocation.value
                    )
                    is TrackingState.Tracking -> TrackingUiState.Tracking(
                        route = route,
                        sessionId = state.sessionId,
                        currentLocation = state.currentLocation,
                        trackPoints = emptyList() // Will be updated by session observer
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
     * Start tracking (permission must be granted first).
     * Will check distance from route and ask for confirmation if too far.
     */
    fun startTracking() {
        screenModelScope.launch {
            try {
                val currentLocation = trackingManager.currentLocation.value

                // If no current location, start tracking anyway
                if (currentLocation == null) {
                    trackingManager.startTracking(routeId = routeId)
                    return@launch
                }

                // Check if near route (only if route has GPS data)
                val nearRoute = isNearRoute(currentLocation, route)

                when (nearRoute) {
                    null -> {
                        // Route has no GPS data, start tracking
                        trackingManager.startTracking(routeId = routeId)
                    }
                    true -> {
                        // Within 1km, start tracking
                        trackingManager.startTracking(routeId = routeId)
                    }
                    false -> {
                        // Far from route, show confirmation dialog
                        val routeCoordinates = route?.gpxData?.let { parseGeoJsonLineString(it) }
                        val distance = if (routeCoordinates != null) {
                            calculateMinDistanceToRoute(currentLocation, routeCoordinates)
                        } else {
                            0.0
                        }
                        _uiState.value = TrackingUiState.AwaitingConfirmation(
                            route = route,
                            currentLocation = currentLocation,
                            distanceFromRoute = distance
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TrackingUiState.Error(
                    e.message ?: "Failed to start tracking"
                )
            }
        }
    }

    /**
     * Start tracking without distance check (after user confirmation)
     */
    fun startTrackingForced() {
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
     * Cancel the awaiting confirmation state and return to Idle
     */
    fun cancelConfirmation() {
        if (_uiState.value is TrackingUiState.AwaitingConfirmation) {
            _uiState.value = TrackingUiState.Idle(
                route = route,
                currentLocation = trackingManager.currentLocation.value
            )
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
                _uiState.value = TrackingUiState.Idle(
                    route = route,
                    currentLocation = location
                )
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        if (_uiState.value is TrackingUiState.Error) {
            _uiState.value = TrackingUiState.Idle(
                route = route,
                currentLocation = trackingManager.currentLocation.value
            )
        }
    }

    /**
     * Check if current location is within acceptable distance from route.
     * Returns null if route has no GPS data, true if within 1km, false otherwise.
     */
    fun isNearRoute(location: LocationData, route: Route?): Boolean? {
        if (route?.gpxData == null) return null

        val routeCoordinates = parseGeoJsonLineString(route.gpxData)
        if (routeCoordinates.isEmpty()) return null

        val minDistance = calculateMinDistanceToRoute(
            location = location,
            routeCoordinates = routeCoordinates
        )

        return minDistance <= 1000.0 // 1km threshold in meters
    }

    /**
     * Calculate minimum distance from location to any point on the route
     */
    private fun calculateMinDistanceToRoute(
        location: LocationData,
        routeCoordinates: List<Pair<Double, Double>>
    ): Double {
        return routeCoordinates.minOf { (lon, lat) ->
            calculateHaversineDistance(
                lat1 = location.latitude,
                lon1 = location.longitude,
                lat2 = lat,
                lon2 = lon
            )
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in meters
     */
    private fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a = sin(dLat / 2).pow(2.0) +
                cos(lat1 * PI / 180.0) *
                cos(lat2 * PI / 180.0) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }

    /**
     * Parse GeoJSON LineString coordinates from JSON string.
     * Returns list of (longitude, latitude) pairs.
     */
    private fun parseGeoJsonLineString(geoJson: String): List<Pair<Double, Double>> {
        return try {
            val json = Json.parseToJsonElement(geoJson).jsonObject
            val coordinates = json["coordinates"]?.jsonArray ?: return emptyList()

            coordinates.map { coord ->
                val array = coord.jsonArray
                val lon = array[0].jsonPrimitive.content.toDouble()
                val lat = array[1].jsonPrimitive.content.toDouble()
                Pair(lon, lat)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * UI state for tracking screen
 */
sealed interface TrackingUiState {
    data class Idle(
        val route: Route? = null,
        val currentLocation: LocationData? = null
    ) : TrackingUiState

    data class AwaitingConfirmation(
        val route: Route?,
        val currentLocation: LocationData?,
        val distanceFromRoute: Double
    ) : TrackingUiState

    data class Tracking(
        val route: Route?,
        val sessionId: String,
        val currentLocation: LocationData?,
        val trackPoints: List<TrackPoint> = emptyList()
    ) : TrackingUiState

    data class Completed(
        val session: TrackingSession?
    ) : TrackingUiState

    data class Error(
        val message: String
    ) : TrackingUiState
}
