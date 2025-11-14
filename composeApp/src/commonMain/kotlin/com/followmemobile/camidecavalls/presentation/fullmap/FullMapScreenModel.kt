package com.followmemobile.camidecavalls.presentation.fullmap

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Language
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.poi.GetAllPOIsUseCase
import com.followmemobile.camidecavalls.domain.usecase.GetSimplifiedRoutesUseCase
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

/**
 * ScreenModel for FullMapScreen.
 * Handles loading and displaying all 20 routes on the map.
 */
class FullMapScreenModel(
    private val getSimplifiedRoutesUseCase: GetSimplifiedRoutesUseCase,
    private val getAllPOIsUseCase: GetAllPOIsUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(FullMapUiState())
    val uiState: StateFlow<FullMapUiState> = _uiState.asStateFlow()

    init {
        observeLanguage()
    }

    private fun observeLanguage() {
        screenModelScope.launch {
            val currentLang = languageRepository.getCurrentLanguage()
            _uiState.update {
                it.copy(
                    strings = LocalizedStrings(currentLang),
                    currentLanguage = Language.fromCode(currentLang)
                )
            }

            languageRepository.observeCurrentLanguage().collect { languageCode ->
                _uiState.update {
                    it.copy(
                        strings = LocalizedStrings(languageCode),
                        currentLanguage = Language.fromCode(languageCode)
                    )
                }
            }
        }
    }

    private var mapController: MapLayerController? = null

    /**
     * Called when MapLibre map is ready
     */
    fun onMapReady(controller: MapLayerController) {
        println("🗺️  FullMapScreen: onMapReady called")
        mapController = controller
        controller.setOnMarkerClickListener { markerId ->
            onMarkerClick(markerId)
        }

        loadAllRoutes()
        loadAllPOIs()
    }

    /**
     * Load all 20 routes with simplified GPS data
     */
    private fun loadAllRoutes() {
        println("🗺️  FullMapScreen: loadAllRoutes called")
        screenModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingRoutes = true, errorRoutes = null) }
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
                            isLoadingRoutes = false,
                            simplificationStats = result.stats.toString()
                        )
                    }

                    println("✅ Loaded ${routes.size} routes on full map")
                    println("📊 Simplification: ${result.stats}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingRoutes = false,
                        errorRoutes = "Error loading routes: ${e.message}"
                    )
                }
                println("❌ Error loading routes: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadAllPOIs() {
        println("🗺️  FullMapScreen: loadAllPOIs called")
        screenModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingPOIs = true, errorPOIs = null) }

                getAllPOIsUseCase().collect { pois ->
                    println("🗺️  FullMapScreen: Received ${pois.size} POIs from database")

                    pois.forEach { poi ->
                        val color = getColorForPOIType(poi.type.name)
                        mapController?.addMarker(
                            markerId = "poi-marker-${poi.id}",
                            latitude = poi.latitude,
                            longitude = poi.longitude,
                            color = color,
                            radius = 8f
                        )
                    }

                    println("🗺️  FullMapScreen: Added ${pois.size} POI markers to map")

                    _uiState.update {
                        it.copy(
                            pois = pois,
                            isLoadingPOIs = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingPOIs = false,
                        errorPOIs = "Error loading POIs: ${e.message}"
                    )
                }
                println("❌ Error loading POIs: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun onMarkerClick(markerId: String) {
        println("🎯 FullMap marker clicked: $markerId")

        val poiId = markerId.removePrefix("poi-marker-").toIntOrNull() ?: return
        val poi = _uiState.value.pois.find { it.id == poiId } ?: return

        val currentZoom = mapController?.getCurrentZoom() ?: 10.0
        val baseOffset = 0.015
        val zoomFactor = 2.0.pow(currentZoom - 10.0)
        val latitudeOffset = baseOffset / zoomFactor

        println("🎯 Zoom: $currentZoom, Offset: $latitudeOffset")

        val offsetLatitude = poi.latitude - latitudeOffset
        mapController?.updateCamera(
            latitude = offsetLatitude,
            longitude = poi.longitude,
            zoom = null,
            animated = true
        )

        _uiState.update { it.copy(selectedPoi = poi) }
    }

    fun closePoiPopup() {
        _uiState.update { it.copy(selectedPoi = null) }
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
    private fun getColorForPOIType(type: String): String {
        return when (type) {
            "BEACH" -> "#6FBAFF"
            "NATURAL" -> "#7FD17F"
            "HISTORIC" -> "#FF8080"
            else -> "#9E9E9E"
        }
    }
}

data class FullMapUiState(
    val routes: List<Route> = emptyList(),
    val pois: List<PointOfInterest> = emptyList(),
    val selectedRoute: Route? = null,
    val selectedPoi: PointOfInterest? = null,
    val isLoadingRoutes: Boolean = false,
    val isLoadingPOIs: Boolean = false,
    val errorRoutes: String? = null,
    val errorPOIs: String? = null,
    val simplificationStats: String? = null,
    val strings: LocalizedStrings = LocalizedStrings("en"),
    val currentLanguage: Language = Language.ENGLISH
)
