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
import kotlin.math.abs
import kotlin.math.roundToInt

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
     * Generates a smooth gradient across the visible spectrum for up to 20 routes
     */
    private fun getRouteColor(index: Int): String {
        val hueStep = 360f / ROUTE_COLOR_COUNT
        val hue = (index % ROUTE_COLOR_COUNT) * hueStep
        return hsvToHex(hue, COLOR_SATURATION, COLOR_VALUE)
    }

    private fun hsvToHex(hue: Float, saturation: Float, value: Float): String {
        val normalizedHue = ((hue % 360f) + 360f) % 360f
        val chroma = value * saturation
        val huePrime = normalizedHue / 60f
        val secondLargestComponent = chroma * (1 - abs((huePrime % 2) - 1))

        val (red, green, blue) = when {
            huePrime < 1f -> Triple(chroma, secondLargestComponent, 0f)
            huePrime < 2f -> Triple(secondLargestComponent, chroma, 0f)
            huePrime < 3f -> Triple(0f, chroma, secondLargestComponent)
            huePrime < 4f -> Triple(0f, secondLargestComponent, chroma)
            huePrime < 5f -> Triple(secondLargestComponent, 0f, chroma)
            else -> Triple(chroma, 0f, secondLargestComponent)
        }

        val match = value - chroma
        val r = ((red + match) * 255).roundToInt().coerceIn(0, 255)
        val g = ((green + match) * 255).roundToInt().coerceIn(0, 255)
        val b = ((blue + match) * 255).roundToInt().coerceIn(0, 255)

        return String.format("#%02X%02X%02X", r, g, b)
    }

    companion object {
        private const val ROUTE_COLOR_COUNT = 20
        private const val COLOR_SATURATION = 0.85f
        private const val COLOR_VALUE = 0.95f
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
