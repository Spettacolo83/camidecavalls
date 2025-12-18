package com.followmemobile.camidecavalls.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific controller for rendering custom layers on MapLibre maps.
 * Provides a unified interface for adding routes, markers, and POIs across platforms.
 */
expect class MapLayerController() {
    /**
     * Set click listener for markers
     * @param onClick Callback when a marker is clicked, receives markerId
     */
    fun setOnMarkerClickListener(onClick: (String) -> Unit)
    /**
     * Add a route path to the map
     * @param routeId Unique identifier for this route
     * @param geoJsonLineString GeoJSON LineString data
     * @param color Route line color in hex format (e.g., "#2196F3")
     * @param width Route line width in dp
     * @param withCasing Whether to add a white border/casing around the line
     */
    fun addRoutePath(
        routeId: String,
        geoJsonLineString: String,
        color: String = "#2196F3",
        width: Float = 4f,
        withCasing: Boolean = true
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
     * Center the camera on a coordinate, optionally lifting the point by a
     * specific amount of vertical pixels (used to keep POIs above popups).
     */
    fun centerLocationWithVerticalOffset(
        latitude: Double,
        longitude: Double,
        offsetPixels: Float? = null,
        animated: Boolean = true
    )

    /**
     * Get current zoom level
     */
    fun getCurrentZoom(): Double

    /**
     * Highlight a marker with an animated ripple and ensure it is visible.
     * Pass null to clear the highlight.
     */
    fun setHighlightedMarker(markerId: String?, colorHex: String? = null)

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
 * Map style URL for OpenFreeMap Liberty style
 */
object MapStyles {
    const val LIBERTY = "https://tiles.openfreemap.org/styles/liberty"
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
    styleUrl: String = MapStyles.LIBERTY,
    onMapReady: (MapLayerController) -> Unit,
    onCameraMoved: (() -> Unit)? = null,
    onZoomChanged: ((Double) -> Unit)? = null
)
