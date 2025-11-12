package com.followmemobile.camidecavalls.presentation.fullmap

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.GetSimplifiedRoutesUseCase
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel for FullMapScreen.
 * Handles loading and displaying all 20 routes on the map.
 */
class FullMapScreenModel(
    private val getSimplifiedRoutesUseCase: GetSimplifiedRoutesUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(FullMapUiState())
    val uiState: StateFlow<FullMapUiState> = _uiState.asStateFlow()

    init {
        loadLanguage()
    }

    private fun loadLanguage() {
        screenModelScope.launch {
            val currentLang = languageRepository.getCurrentLanguage()
            _uiState.update { it.copy(strings = LocalizedStrings(currentLang)) }
        }
    }

    private var mapController: MapLayerController? = null

    /**
     * Called when MapLibre map is ready
     */
    fun onMapReady(controller: MapLayerController) {
        println("🗺️  FullMapScreen: onMapReady called")
        mapController = controller
        loadAllRoutes()
    }

    /**
     * Load all 20 routes with simplified GPS data
     */
    private fun loadAllRoutes() {
        println("🗺️  FullMapScreen: loadAllRoutes called")
        screenModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                println("🗺️  FullMapScreen: Starting to load routes...")

                // Get simplified routes with full map tolerance (≈11m precision)
                getSimplifiedRoutesUseCase(
                    tolerance = GetSimplifiedRoutesUseCase.TOLERANCE_FULL_MAP
                ).collect { result ->
                    val routes = result.routes
                    println("🗺️  FullMapScreen: Received ${routes.size} routes from use case")

                    // Add each route to the map with different colors
                    var addedCount = 0
                    routes.forEachIndexed { index, route ->
                        if (route.gpxData != null) {
                            val color = getRouteColor(index)
                            println("🗺️  Adding route ${route.id}: ${route.name} (color: $color)")
                            mapController?.addRoutePath(
                                routeId = "route_${route.id}",
                                geoJsonLineString = route.gpxData,
                                color = color,
                                width = 3f
                            )
                            addedCount++
                        } else {
                            println("⚠️  Route ${route.id} has no GPX data!")
                        }
                    }

                    println("🗺️  FullMapScreen: Added $addedCount routes to map")

                    _uiState.update {
                        it.copy(
                            routes = routes,
                            isLoading = false,
                            simplificationStats = result.stats.toString()
                        )
                    }

                    println("✅ Loaded ${routes.size} routes on full map")
                    println("📊 Simplification: ${result.stats}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error loading routes: ${e.message}"
                    )
                }
                println("❌ Error loading routes: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Select a route (highlight it and show popup)
     */
    fun selectRoute(routeId: Int) {
        val route = _uiState.value.routes.find { it.id == routeId }
        if (route != null) {
            _uiState.update { it.copy(selectedRoute = route) }
            // TODO: Implement route highlighting and popup in future PR
            println("📍 Route selected: ${route.name}")
        }
    }

    /**
     * Get color for route based on index
     * Uses a color palette that provides good contrast on map
     */
    private fun getRouteColor(index: Int): String {
        val colors = listOf(
            "#E91E63", // Pink
            "#9C27B0", // Purple
            "#673AB7", // Deep Purple
            "#3F51B5", // Indigo
            "#2196F3", // Blue
            "#03A9F4", // Light Blue
            "#00BCD4", // Cyan
            "#009688", // Teal
            "#4CAF50", // Green
            "#8BC34A", // Light Green
            "#CDDC39", // Lime
            "#FFEB3B", // Yellow
            "#FFC107", // Amber
            "#FF9800", // Orange
            "#FF5722", // Deep Orange
            "#F44336", // Red
            "#795548", // Brown
            "#607D8B", // Blue Grey
            "#9E9E9E", // Grey
            "#000000"  // Black
        )
        return colors[index % colors.size]
    }
}

/**
 * UI State for FullMapScreen
 */
data class FullMapUiState(
    val routes: List<Route> = emptyList(),
    val selectedRoute: Route? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val simplificationStats: String? = null,
    val strings: LocalizedStrings = LocalizedStrings("en")
)
