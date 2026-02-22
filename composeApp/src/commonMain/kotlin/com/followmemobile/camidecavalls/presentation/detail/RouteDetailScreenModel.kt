package com.followmemobile.camidecavalls.presentation.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Difficulty
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.route.GetAllRoutesUseCase
import com.followmemobile.camidecavalls.domain.usecase.route.GetRouteByIdUseCase
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * ScreenModel for the Route Detail screen.
 * Displays detailed information about a specific route.
 * For routeId=0, builds a "Complete Route" from all 20 routes.
 */
class RouteDetailScreenModel(
    private val routeId: Int,
    private val getRouteByIdUseCase: GetRouteByIdUseCase,
    private val getAllRoutesUseCase: GetAllRoutesUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow<RouteDetailUiState>(RouteDetailUiState.Loading)
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()

    init {
        loadRoute()
        observeLanguageChanges()
    }

    private fun loadRoute() {
        screenModelScope.launch {
            _uiState.value = RouteDetailUiState.Loading
            val currentLanguage = languageRepository.getCurrentLanguage()
            val strings = LocalizedStrings(currentLanguage)

            if (routeId == 0) {
                // Build complete route from all routes
                val allRoutes = getAllRoutesUseCase().first()
                if (allRoutes.isNotEmpty()) {
                    // Combine GPX data from all routes
                    val combinedGpx = combineGeoJsonLineStrings(allRoutes)

                    // Weighted asphalt percentage by distance
                    val totalDist = allRoutes.sumOf { it.distanceKm }
                    val weightedAsphalt = if (totalDist > 0) {
                        (allRoutes.sumOf { it.asphaltPercentage * it.distanceKm } / totalDist).toInt()
                    } else 0

                    // Localized about descriptions
                    val caStrings = LocalizedStrings("ca")
                    val esStrings = LocalizedStrings("es")
                    val enStrings = LocalizedStrings("en")
                    val deStrings = LocalizedStrings("de")
                    val frStrings = LocalizedStrings("fr")
                    val itStrings = LocalizedStrings("it")

                    val completeRoute = Route(
                        id = 0,
                        number = 0,
                        name = strings.completeRouteName,
                        startPoint = "Maó",
                        endPoint = "Maó",
                        distanceKm = 185.0,
                        elevationGainMeters = allRoutes.sumOf { it.elevationGainMeters },
                        elevationLossMeters = allRoutes.sumOf { it.elevationLossMeters },
                        maxAltitudeMeters = allRoutes.maxOf { it.maxAltitudeMeters },
                        minAltitudeMeters = allRoutes.minOf { it.minAltitudeMeters },
                        asphaltPercentage = weightedAsphalt,
                        difficulty = Difficulty.HIGH,
                        estimatedDurationMinutes = allRoutes.sumOf { it.estimatedDurationMinutes },
                        description = enStrings.aboutDescription,
                        gpxData = combinedGpx,
                        descriptionCa = caStrings.aboutDescription,
                        descriptionEs = esStrings.aboutDescription,
                        descriptionEn = enStrings.aboutDescription,
                        descriptionDe = deStrings.aboutDescription,
                        descriptionFr = frStrings.aboutDescription,
                        descriptionIt = itStrings.aboutDescription
                    )
                    _uiState.value = RouteDetailUiState.Success(completeRoute, currentLanguage, strings)
                } else {
                    _uiState.value = RouteDetailUiState.Error("Route not found")
                }
            } else {
                val route = getRouteByIdUseCase(routeId)
                _uiState.value = if (route != null) {
                    RouteDetailUiState.Success(route, currentLanguage, strings)
                } else {
                    RouteDetailUiState.Error("Route not found")
                }
            }
        }
    }

    /**
     * Combine GeoJSON LineString coordinates from all routes into a single GeoJSON LineString.
     */
    private fun combineGeoJsonLineStrings(routes: List<Route>): String? {
        val allCoordinates = mutableListOf<String>()
        // Sort routes by number to get correct order
        for (route in routes.sortedBy { it.number }) {
            val gpx = route.gpxData ?: continue
            try {
                val json = Json.parseToJsonElement(gpx).jsonObject
                val coords = json["coordinates"]?.jsonArray ?: continue
                for (coord in coords) {
                    allCoordinates.add(coord.toString())
                }
            } catch (_: Exception) {
                // Skip malformed GeoJSON
            }
        }
        if (allCoordinates.isEmpty()) return null
        return """{"type":"LineString","coordinates":[${allCoordinates.joinToString(",")}]}"""
    }

    private fun observeLanguageChanges() {
        screenModelScope.launch {
            languageRepository.observeCurrentLanguage().collect { newLanguage ->
                val currentState = _uiState.value
                if (currentState is RouteDetailUiState.Success) {
                    val newStrings = LocalizedStrings(newLanguage)
                    _uiState.value = currentState.copy(
                        currentLanguage = newLanguage,
                        strings = newStrings
                    )
                }
            }
        }
    }
}

sealed interface RouteDetailUiState {
    data object Loading : RouteDetailUiState
    data class Success(
        val route: Route,
        val currentLanguage: String,
        val strings: LocalizedStrings
    ) : RouteDetailUiState
    data class Error(val message: String) : RouteDetailUiState
}
