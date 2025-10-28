package com.followmemobile.camidecavalls.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific controller for rendering custom layers on MapLibre maps.
 * Provides a unified interface for adding routes, markers, and POIs across platforms.
 */
expect class MapLayerController() {
    /**
     * Add a route path to the map
     * @param routeId Unique identifier for this route
     * @param geoJsonLineString GeoJSON LineString data
     * @param color Route line color in hex format (e.g., "#2196F3")
     * @param width Route line width in dp
     */
    fun addRoutePath(
        routeId: String,
        geoJsonLineString: String,
        color: String = "#2196F3",
        width: Float = 4f
    )

    /**
     * Add a marker to the map
     * @param markerId Unique identifier for this marker
     * @param latitude Marker latitude
     * @param longitude Marker longitude
     * @param color Marker color in hex format
     * @param radius Marker radius in dp
     */
    fun addMarker(
        markerId: String,
        latitude: Double,
        longitude: Double,
        color: String,
        radius: Float = 8f
    )

    /**
     * Update camera position with smooth animation
     * @param latitude Target latitude
     * @param longitude Target longitude
     * @param zoom Target zoom level (if null, maintains current zoom)
     * @param animated Whether to animate the transition
     */
    fun updateCamera(
        latitude: Double,
        longitude: Double,
        zoom: Double? = null,
        animated: Boolean = true
    )

    /**
     * Get current zoom level
     */
    fun getCurrentZoom(): Double

    /**
     * Remove a specific layer from the map
     */
    fun removeLayer(layerId: String)

    /**
     * Clear all custom layers
     */
    fun clearAll()
}

/**
 * Composable wrapper for MapLibre map with custom layer support
 */
@Composable
expect fun MapWithLayers(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Double,
    styleUrl: String = "https://tiles.openfreemap.org/styles/liberty",
    onMapReady: (MapLayerController) -> Unit,
    onCameraMoved: (() -> Unit)? = null,
    onZoomChanged: ((Double) -> Unit)? = null
)
