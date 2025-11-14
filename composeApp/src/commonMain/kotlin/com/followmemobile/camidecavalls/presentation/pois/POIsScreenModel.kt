package com.followmemobile.camidecavalls.presentation.pois

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Language
import com.followmemobile.camidecavalls.domain.model.POIType
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.poi.GetAllPOIsUseCase
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

/**
 * ScreenModel for POIsScreen.
 * Handles loading and displaying all POIs on the Menorca map.
 */
class POIsScreenModel(
    private val getAllPOIsUseCase: GetAllPOIsUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(POIsUiState())
    val uiState: StateFlow<POIsUiState> = _uiState.asStateFlow()

    private var mapController: MapLayerController? = null

    init {
        // Observe language changes
        screenModelScope.launch {
            languageRepository.observeCurrentLanguage().collect { languageCode ->
                val language = Language.fromCode(languageCode)
                _uiState.update {
                    it.copy(
                        currentLanguage = language,
                        strings = LocalizedStrings(languageCode)
                    )
                }
            }
        }
    }

    /**
     * Called when MapLibre map is ready
     */
    fun onMapReady(controller: MapLayerController) {
        println("🗺️  POIsScreen: onMapReady called")
        mapController = controller

        // Set up marker click listener
        controller.setOnMarkerClickListener { markerId ->
            onMarkerClick(markerId)
        }

        loadAllPOIs()
    }

    /**
     * Load all POIs from database and display them on the map
     */
    private fun loadAllPOIs() {
        println("🗺️  POIsScreen: loadAllPOIs called")
        screenModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                getAllPOIsUseCase().collect { pois ->
                    println("🗺️  POIsScreen: Received ${pois.size} POIs from database")

                    _uiState.update { state ->
                        val refreshedSelected = state.selectedPoi?.let { selected ->
                            pois.find { it.id == selected.id }
                        }

                        state.copy(
                            pois = pois,
                            selectedPoi = refreshedSelected?.takeIf { poi ->
                                state.visibleTypes.contains(poi.type)
                            },
                            isLoading = false
                        )
                    }

                    refreshMarkers(
                        pois = pois,
                        visibleTypes = _uiState.value.visibleTypes
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error loading POIs: ${e.message}"
                    )
                }
                println("❌ Error loading POIs: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Handle marker click
     */
    private fun onMarkerClick(markerId: String) {
        println("🎯 Marker clicked: $markerId")

        // Extract POI ID from marker ID
        val poiId = markerId.removePrefix("poi-marker-").toIntOrNull() ?: return

        // Find the POI
        val poi = _uiState.value.pois.find { it.id == poiId } ?: return

        // Get current zoom level to calculate appropriate offset
        val currentZoom = mapController?.getCurrentZoom() ?: 10.0

        // Calculate offset based on zoom level
        // At higher zoom levels, we need smaller offsets
        // Formula: offset = base / (2^(zoom-10))
        // This ensures the marker is positioned consistently above the popup
        val baseOffset = 0.015 // Base offset at zoom 10
        val zoomFactor = 2.0.pow(currentZoom - 10.0)
        val latitudeOffset = baseOffset / zoomFactor

        println("🎯 Zoom: $currentZoom, Offset: $latitudeOffset")

        // Center camera on POI with smooth animation
        // Offset the latitude DOWN (negative value) so the popup at bottom doesn't cover the marker
        val offsetLatitude = poi.latitude - latitudeOffset
        mapController?.updateCamera(
            latitude = offsetLatitude,
            longitude = poi.longitude,
            zoom = null, // Keep user's current zoom level
            animated = true
        )

        // Update selected POI to show popup
        _uiState.update { it.copy(selectedPoi = poi) }
    }

    /**
     * Close the POI popup
     */
    fun closePopup() {
        _uiState.update { it.copy(selectedPoi = null) }
    }

    /**
     * Toggle the visibility of a POI type and refresh markers accordingly.
     */
    fun toggleType(type: POIType) {
        val currentState = _uiState.value
        val newVisibleTypes = if (type in currentState.visibleTypes) {
            currentState.visibleTypes - type
        } else {
            currentState.visibleTypes + type
        }

        val updatedSelected = currentState.selectedPoi?.let { selected ->
            if (newVisibleTypes.contains(selected.type)) {
                currentState.pois.find { it.id == selected.id }
            } else {
                null
            }
        }

        _uiState.update {
            it.copy(
                visibleTypes = newVisibleTypes,
                selectedPoi = updatedSelected
            )
        }

        refreshMarkers(pois = currentState.pois, visibleTypes = newVisibleTypes)
    }

    private fun refreshMarkers(
        pois: List<PointOfInterest>,
        visibleTypes: Set<POIType>
    ) {
        val controller = mapController ?: return

        pois.forEach { poi ->
            controller.removeLayer("poi-marker-${poi.id}")
        }

        val filtered = pois.filter { poi -> visibleTypes.contains(poi.type) }

        filtered.forEach { poi ->
            controller.addMarker(
                markerId = "poi-marker-${poi.id}",
                latitude = poi.latitude,
                longitude = poi.longitude,
                color = PoiTypeColors.markerHex(poi.type),
                radius = 8f
            )
        }

        println("🗺️  POIsScreen: Showing ${filtered.size} markers after filter update")
    }
}

/**
 * UI State for POIsScreen
 */
data class POIsUiState(
    val pois: List<PointOfInterest> = emptyList(),
    val selectedPoi: PointOfInterest? = null,
    val currentLanguage: Language = Language.CATALAN,
    val strings: LocalizedStrings = LocalizedStrings("ca"),
    val isLoading: Boolean = false,
    val error: String? = null,
    val visibleTypes: Set<POIType> = POIType.entries.toSet()
)
