package com.followmemobile.camidecavalls.presentation.tracking

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import com.followmemobile.camidecavalls.domain.usecase.GetSimplifiedRoutesUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingManager
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getSimplifiedRoutesUseCase: GetSimplifiedRoutesUseCase,
    private val routeId: Int? = null
) : ScreenModel {

    private val _uiState = MutableStateFlow<TrackingUiState>(TrackingUiState.Idle())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var selectedRoute: Route? = null
    private var routes: List<Route> = emptyList()
    private var cachedTrackPoints: List<TrackPoint> = emptyList()

    init {
        println("🏗️ TrackingScreenModel created: ${this.hashCode()}, routeId=$routeId")

        // Reset any previous Completed/Error state when screen is opened
        // This ensures a fresh start when navigating back to the tracking screen
        trackingManager.resetToStopped()
        if (trackingManager.trackingState.value is TrackingState.Stopped &&
            trackingManager.activeTrackPoints.value.isNotEmpty()
        ) {
            trackingManager.resetToStopped(clearTrack = true)
        }

        // Load routes for display
        if (routeId != null) {
            screenModelScope.launch {
                selectedRoute = getSimplifiedRoutesUseCase.getSimplifiedRoute(
                    routeId,
                    GetSimplifiedRoutesUseCase.TOLERANCE_SINGLE_ROUTE
                )
                routes = selectedRoute?.let { listOf(it) } ?: emptyList()

                when (val state = _uiState.value) {
                    is TrackingUiState.Tracking -> {
                        _uiState.value = state.copy(routes = routes, selectedRoute = selectedRoute)
                    }

                    is TrackingUiState.Paused -> {
                        _uiState.value = state.copy(routes = routes, selectedRoute = selectedRoute)
                    }

                    is TrackingUiState.AwaitingConfirmation -> {
                        _uiState.value = state.copy(routes = routes, selectedRoute = selectedRoute)
                    }

                    is TrackingUiState.Idle -> {
                        _uiState.value = state.copy(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            currentLocation = state.currentLocation
                                ?: trackingManager.currentLocation.value
                        )
                    }

                    else -> {
                        _uiState.value = TrackingUiState.Idle(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            currentLocation = trackingManager.currentLocation.value
                        )
                    }
                }
            }
        } else {
            screenModelScope.launch {
                getSimplifiedRoutesUseCase(GetSimplifiedRoutesUseCase.TOLERANCE_FULL_MAP).collect { result ->
                    selectedRoute = null
                    routes = result.routes

                    when (val state = _uiState.value) {
                        is TrackingUiState.Tracking -> {
                            _uiState.value = state.copy(routes = routes, selectedRoute = null)
                        }

                        is TrackingUiState.Paused -> {
                            _uiState.value = state.copy(routes = routes, selectedRoute = null)
                        }

                        is TrackingUiState.AwaitingConfirmation -> {
                            _uiState.value = state.copy(routes = routes, selectedRoute = null)
                        }

                        is TrackingUiState.Idle -> {
                            _uiState.value = state.copy(
                                routes = routes,
                                selectedRoute = null,
                                currentLocation = state.currentLocation
                                    ?: trackingManager.currentLocation.value
                            )
                        }

                        else -> {
                            _uiState.value = TrackingUiState.Idle(
                                routes = routes,
                                selectedRoute = null,
                                currentLocation = trackingManager.currentLocation.value
                            )
                        }
                    }
                }
            }
        }

        // Observe current location to update Idle state
        screenModelScope.launch {
            trackingManager.currentLocation.collect { location ->
                when (val state = _uiState.value) {
                    is TrackingUiState.Idle -> {
                        _uiState.value = state.copy(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            currentLocation = location
                        )
                    }

                    is TrackingUiState.AwaitingConfirmation -> {
                        _uiState.value = state.copy(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            currentLocation = location
                        )
                    }

                    else -> {
                        // No-op for other states
                    }
                }
            }
        }

        // Observe track points so UI can be restored when returning to the screen
        screenModelScope.launch {
            trackingManager.activeTrackPoints.collect { points ->
                cachedTrackPoints = points

                when (val state = _uiState.value) {
                    is TrackingUiState.Tracking -> {
                        _uiState.value = state.copy(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            trackPoints = points
                        )
                    }

                    is TrackingUiState.Paused -> {
                        _uiState.value = state.copy(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            trackPoints = points
                        )
                    }

                    else -> {
                        // No-op for other states
                    }
                }
            }
        }

        // Observe tracking state - ONLY track points received in real-time
        // Never load from database to avoid confusion with old sessions
        screenModelScope.launch {
            trackingManager.trackingState.collect { state ->
                _uiState.value = when (state) {
                    is TrackingState.Stopped -> {
                        cachedTrackPoints = emptyList()
                        TrackingUiState.Idle(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            currentLocation = trackingManager.currentLocation.value
                        )
                    }

                    is TrackingState.Recording -> {
                        TrackingUiState.Tracking(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            sessionId = state.sessionId,
                            currentLocation = state.currentLocation,
                            trackPoints = cachedTrackPoints
                        )
                    }

                    is TrackingState.Paused -> {
                        TrackingUiState.Paused(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            sessionId = state.sessionId,
                            currentLocation = state.currentLocation
                                ?: trackingManager.currentLocation.value,
                            trackPoints = cachedTrackPoints
                        )
                    }

                    is TrackingState.Completed -> {
                        TrackingUiState.Completed(state.session)
                    }

                    is TrackingState.Error -> TrackingUiState.Error(state.message)
                }
            }
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

                val distance = calculateDistanceToClosestRoute(currentLocation, routes)

                if (distance == null || distance <= 2000.0) {
                    trackingManager.startTracking(routeId = routeId)
                } else {
                    _uiState.value = TrackingUiState.AwaitingConfirmation(
                        routes = routes,
                        selectedRoute = selectedRoute,
                        currentLocation = currentLocation,
                        distanceFromRoute = distance
                    )
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

    fun pauseTracking() {
        screenModelScope.launch {
            try {
                trackingManager.pauseTracking()
            } catch (e: Exception) {
                _uiState.value = TrackingUiState.Error(
                    e.message ?: "Failed to pause tracking"
                )
            }
        }
    }

    fun resumeTracking() {
        screenModelScope.launch {
            try {
                trackingManager.resumeTracking()
            } catch (e: Exception) {
                _uiState.value = TrackingUiState.Error(
                    e.message ?: "Failed to resume tracking"
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
                routes = routes,
                selectedRoute = selectedRoute,
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

    fun startNewSession() {
        screenModelScope.launch {
            trackingManager.resetToStopped(clearTrack = true)
            cachedTrackPoints = emptyList()
            _uiState.value = TrackingUiState.Idle(
                routes = routes,
                selectedRoute = selectedRoute,
                currentLocation = trackingManager.currentLocation.value
            )
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
                    routes = routes,
                    selectedRoute = selectedRoute,
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
            trackingManager.resetToStopped()
            _uiState.value = TrackingUiState.Idle(
                routes = routes,
                selectedRoute = selectedRoute,
                currentLocation = trackingManager.currentLocation.value
            )
        }
    }

    /**
     * Calculate distance from current location to the closest available route.
     * Returns null when no route contains GPS data.
     */
    private fun calculateDistanceToClosestRoute(
        location: LocationData,
        routes: List<Route>
    ): Double? {
        val distances = routes.mapNotNull { route ->
            route.gpxData?.let { geoJson ->
                val routeCoordinates = parseGeoJsonLineString(geoJson)
                if (routeCoordinates.isEmpty()) {
                    null
                } else {
                    calculateMinDistanceToRoute(location, routeCoordinates)
                }
            }
        }

        return distances.minOrNull()
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

    override fun onDispose() {
        super.onDispose()
        println("🗑️ TrackingScreenModel(${this.hashCode()}) disposed, cached ${cachedTrackPoints.size} points")
    }
}

/**
 * UI state for tracking screen
 */
sealed interface TrackingUiState {
    data class Idle(
        val routes: List<Route> = emptyList(),
        val selectedRoute: Route? = null,
        val currentLocation: LocationData? = null
    ) : TrackingUiState

    data class AwaitingConfirmation(
        val routes: List<Route>,
        val selectedRoute: Route?,
        val currentLocation: LocationData?,
        val distanceFromRoute: Double
    ) : TrackingUiState

    data class Tracking(
        val routes: List<Route>,
        val selectedRoute: Route?,
        val sessionId: String,
        val currentLocation: LocationData?,
        val trackPoints: List<TrackPoint> = emptyList()
    ) : TrackingUiState

    data class Paused(
        val routes: List<Route>,
        val selectedRoute: Route?,
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
