package com.followmemobile.camidecavalls.presentation.tracking

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import com.followmemobile.camidecavalls.domain.usecase.GetSimplifiedRoutesUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.CalculateSessionStatsUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingManager
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingState
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.RouteColorPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
    private val calculateSessionStatsUseCase: CalculateSessionStatsUseCase,
    private val languageRepository: LanguageRepository,
    private val routeId: Int? = null
) : ScreenModel {

    private val _uiState = MutableStateFlow<TrackingUiState>(
        TrackingUiState.Idle(
            strings = LocalizedStrings(languageRepository.getSystemLanguage())
        )
    )
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var selectedRoute: Route? = null
    private var routes: List<Route> = emptyList()
    private var cachedTrackPoints: List<TrackPoint> = emptyList()
    private var mapController: MapLayerController? = null

    // Helper to get current strings from state
    private val currentStrings: LocalizedStrings
        get() = _uiState.value.strings

    init {
        println("🏗️ TrackingScreenModel created: ${this.hashCode()}, routeId=$routeId")

        // Observe language changes
        screenModelScope.launch {
            languageRepository.observeCurrentLanguage().collect { languageCode ->
                val currentState = _uiState.value
                _uiState.value = when (currentState) {
                    is TrackingUiState.Idle -> currentState.copy(strings = LocalizedStrings(languageCode))
                    is TrackingUiState.AwaitingConfirmation -> currentState.copy(strings = LocalizedStrings(languageCode))
                    is TrackingUiState.Tracking -> currentState.copy(strings = LocalizedStrings(languageCode))
                    is TrackingUiState.Paused -> currentState.copy(strings = LocalizedStrings(languageCode))
                    is TrackingUiState.Completed -> currentState.copy(strings = LocalizedStrings(languageCode))
                    is TrackingUiState.Error -> currentState.copy(strings = LocalizedStrings(languageCode))
                }
            }
        }

        // Try to restore an active tracking session from the background service.
        // If no active session, reset to a clean Stopped state.
        screenModelScope.launch {
            if (!trackingManager.restoreActiveSession()) {
                trackingManager.resetToStopped()
                if (trackingManager.trackingState.value is TrackingState.Stopped &&
                    trackingManager.activeTrackPoints.value.isNotEmpty()
                ) {
                    trackingManager.resetToStopped(clearTrack = true)
                }
            }
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
                            strings = currentStrings,
                            routes = routes,
                            selectedRoute = selectedRoute,
                            currentLocation = trackingManager.currentLocation.value
                        )
                    }
                }

                mapController?.let { controller ->
                    renderRoutes(controller)
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
                                strings = currentStrings,
                                routes = routes,
                                selectedRoute = null,
                                currentLocation = trackingManager.currentLocation.value
                            )
                        }
                    }

                    mapController?.let { controller ->
                        renderRoutes(controller)
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

                mapController?.let { controller ->
                    renderCurrentLocation(controller)
                }
            }
        }

        // Observe track points so UI can be restored when returning to the screen
        screenModelScope.launch {
            trackingManager.activeTrackPoints.collect { points ->
                cachedTrackPoints = points
                val distance = calculateDistance(points)

                when (val state = _uiState.value) {
                    is TrackingUiState.Tracking -> {
                        _uiState.value = state.copy(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            trackPoints = points,
                            distanceMeters = distance
                        )
                    }

                    is TrackingUiState.Paused -> {
                        _uiState.value = state.copy(
                            routes = routes,
                            selectedRoute = selectedRoute,
                            trackPoints = points,
                            distanceMeters = distance
                        )
                    }

                    else -> {
                        // No-op for other states
                    }
                }

                mapController?.let { controller ->
                    renderTrack(controller)
                    renderCurrentLocation(controller)
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
                        mapController?.let { controller ->
                            renderTrack(controller)
                            renderCurrentLocation(controller)
                        }
                        TrackingUiState.Idle(
                            strings = currentStrings,
                            routes = routes,
                            selectedRoute = selectedRoute,
                            currentLocation = trackingManager.currentLocation.value
                        )
                    }

                    is TrackingState.Recording -> {
                        TrackingUiState.Tracking(
                            strings = currentStrings,
                            routes = routes,
                            selectedRoute = selectedRoute,
                            sessionId = state.sessionId,
                            currentLocation = state.currentLocation,
                            trackPoints = cachedTrackPoints,
                            distanceMeters = calculateDistance(cachedTrackPoints),
                            durationSeconds = state.elapsedSeconds
                        )
                    }

                    is TrackingState.Paused -> {
                        TrackingUiState.Paused(
                            strings = currentStrings,
                            routes = routes,
                            selectedRoute = selectedRoute,
                            sessionId = state.sessionId,
                            currentLocation = state.currentLocation
                                ?: trackingManager.currentLocation.value,
                            trackPoints = cachedTrackPoints,
                            distanceMeters = calculateDistance(cachedTrackPoints),
                            durationSeconds = state.elapsedSeconds
                        )
                    }

                    is TrackingState.Completed -> {
                        TrackingUiState.Completed(
                            strings = currentStrings,
                            session = state.session
                        )
                    }

                    is TrackingState.Error -> TrackingUiState.Error(
                        strings = currentStrings,
                        message = state.message
                    )
                }
            }
        }

    }

    fun onMapReady(controller: MapLayerController) {
        mapController = controller
        renderRoutes(controller)
    }

    fun onMapReleased(controller: MapLayerController) {
        if (mapController === controller) {
            mapController = null
        }
    }

    private fun renderRoutes(controller: MapLayerController) {
        controller.clearAll()

        routes.forEachIndexed { index, route ->
            route.gpxData?.let { geoJson ->
                controller.addRoutePath(
                    routeId = "tracking-route-${route.id}",
                    geoJsonLineString = geoJson,
                    color = RouteColorPalette.colorForIndex(index),
                    width = 4f
                )
            }
        }

        renderTrack(controller)
        renderCurrentLocation(controller)
    }

    private fun renderTrack(controller: MapLayerController) {
        if (cachedTrackPoints.size >= 2) {
            val geoJson = trackPointsToGeoJson(cachedTrackPoints)
            controller.addRoutePath(
                routeId = "user-track",
                geoJsonLineString = geoJson,
                color = "#4CAF50",
                width = 5f
            )
        } else {
            controller.removeLayer("user-track")
        }
    }

    private fun renderCurrentLocation(controller: MapLayerController) {
        val location = trackingManager.currentLocation.value
        if (location != null) {
            controller.removeLayer("current-location")
            controller.addMarker(
                markerId = "current-location",
                latitude = location.latitude,
                longitude = location.longitude,
                color = "#FF5722",
                radius = 10f
            )
        } else {
            controller.removeLayer("current-location")
        }
    }

    private fun trackPointsToGeoJson(trackPoints: List<TrackPoint>): String {
        val coordinates = trackPoints.map { point ->
            JsonArray(
                listOf(
                    JsonPrimitive(point.longitude),
                    JsonPrimitive(point.latitude)
                )
            )
        }

        return buildJsonObject {
            put("type", JsonPrimitive("LineString"))
            put("coordinates", JsonArray(coordinates))
        }.toString()
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
                    strings = currentStrings,
                    message = "Location permission is required for tracking. Please grant permission in Settings."
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
                // Set stage name and localized notification strings for background notification
                val stageName = selectedRoute?.let { route ->
                    currentStrings.routeStage(route.number)
                } ?: currentStrings.notebookGeneralTracking
                trackingManager.setStageName(stageName)
                trackingManager.setNotificationStrings(
                    title = currentStrings.notificationTitle,
                    channelName = currentStrings.notificationChannelName
                )

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
                        strings = currentStrings,
                        routes = routes,
                        selectedRoute = selectedRoute,
                        currentLocation = currentLocation,
                        distanceFromRoute = distance
                    )
                }
            } catch (e: Exception) {
                _uiState.value = TrackingUiState.Error(
                    strings = currentStrings,
                    message = e.message ?: "Failed to start tracking"
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
                // Set stage name and localized notification strings for background notification
                val stageName = selectedRoute?.let { route ->
                    currentStrings.routeStage(route.number)
                } ?: currentStrings.notebookGeneralTracking
                trackingManager.setStageName(stageName)
                trackingManager.setNotificationStrings(
                    title = currentStrings.notificationTitle,
                    channelName = currentStrings.notificationChannelName
                )

                trackingManager.startTracking(routeId = routeId)
            } catch (e: Exception) {
                _uiState.value = TrackingUiState.Error(
                    strings = currentStrings,
                    message = e.message ?: "Failed to start tracking"
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
                    strings = currentStrings,
                    message = e.message ?: "Failed to pause tracking"
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
                    strings = currentStrings,
                    message = e.message ?: "Failed to resume tracking"
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
                strings = currentStrings,
                routes = routes,
                selectedRoute = selectedRoute,
                currentLocation = trackingManager.currentLocation.value
            )
        }
    }

    /**
     * Stop tracking with name and optional notes
     */
    fun stopTracking(name: String = "", notes: String = "") {
        screenModelScope.launch {
            try {
                trackingManager.stopTracking(name, notes)
            } catch (e: Exception) {
                _uiState.value = TrackingUiState.Error(
                    strings = currentStrings,
                    message = e.message ?: "Failed to stop tracking"
                )
            }
        }
    }

    /**
     * Get the default session name based on route and date
     */
    fun getDefaultSessionName(): String {
        val routeName = selectedRoute?.let { route ->
            currentStrings.routeStage(route.number)
        } ?: currentStrings.menuTracking

        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val localDate = now.toLocalDateTime(timeZone)
        val dateStr = "${localDate.dayOfMonth.toString().padStart(2, '0')}/${localDate.monthNumber.toString().padStart(2, '0')}/${localDate.year}"

        return "$routeName - $dateStr"
    }

    fun startNewSession() {
        screenModelScope.launch {
            trackingManager.resetToStopped(clearTrack = true)
            cachedTrackPoints = emptyList()
            _uiState.value = TrackingUiState.Idle(
                strings = currentStrings,
                routes = routes,
                selectedRoute = selectedRoute,
                currentLocation = trackingManager.currentLocation.value
            )

            mapController?.let { controller ->
                renderTrack(controller)
                renderCurrentLocation(controller)
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
                    strings = currentStrings,
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
                strings = currentStrings,
                routes = routes,
                selectedRoute = selectedRoute,
                currentLocation = trackingManager.currentLocation.value
            )

            mapController?.let { controller ->
                renderTrack(controller)
                renderCurrentLocation(controller)
            }
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

    /**
     * Calculate distance traveled from track points
     */
    private fun calculateDistance(trackPoints: List<TrackPoint>): Double {
        return calculateSessionStatsUseCase(trackPoints).distanceMeters
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
    val strings: LocalizedStrings

    data class Idle(
        override val strings: LocalizedStrings,
        val routes: List<Route> = emptyList(),
        val selectedRoute: Route? = null,
        val currentLocation: LocationData? = null
    ) : TrackingUiState

    data class AwaitingConfirmation(
        override val strings: LocalizedStrings,
        val routes: List<Route>,
        val selectedRoute: Route?,
        val currentLocation: LocationData?,
        val distanceFromRoute: Double
    ) : TrackingUiState

    data class Tracking(
        override val strings: LocalizedStrings,
        val routes: List<Route>,
        val selectedRoute: Route?,
        val sessionId: String,
        val currentLocation: LocationData?,
        val trackPoints: List<TrackPoint> = emptyList(),
        val distanceMeters: Double = 0.0,
        val durationSeconds: Long = 0
    ) : TrackingUiState

    data class Paused(
        override val strings: LocalizedStrings,
        val routes: List<Route>,
        val selectedRoute: Route?,
        val sessionId: String,
        val currentLocation: LocationData?,
        val trackPoints: List<TrackPoint> = emptyList(),
        val distanceMeters: Double = 0.0,
        val durationSeconds: Long = 0
    ) : TrackingUiState

    data class Completed(
        override val strings: LocalizedStrings,
        val session: TrackingSession?
    ) : TrackingUiState

    data class Error(
        override val strings: LocalizedStrings,
        val message: String
    ) : TrackingUiState
}
