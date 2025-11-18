package com.followmemobile.camidecavalls.presentation.map

import kotlin.math.ln
import kotlin.math.max

/**
 * Camera configuration used to position the map.
 */
data class MapCameraConfig(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double
)

/**
 * Utility to compute a camera that keeps the entire island of Menorca visible with
 * a bit of padding on each side. The computation is based on the geographic bounds
 * extracted from the official Camí de Cavalls GPX tracks.
 */
object MenorcaViewportCalculator {

    // MapLibre uses 512px tiles for style rendering. Using the correct tile size keeps
    // our manual zoom calculation aligned with what the SDK does internally.
    private const val TILE_SIZE = 512.0
    private const val MAX_ZOOM = 18.0
    private const val MIN_ZOOM = 3.0
    private const val DEFAULT_ZOOM = 10.0

    // Geographic bounds (degrees) of the 20 official routes.
    private const val MIN_LAT = 39.8095598223
    private const val MAX_LAT = 40.0675523638
    private const val MIN_LON = 3.7952079277
    private const val MAX_LON = 4.3016355713

    // Additional padding (percentage of the island size) applied to each axis.
    private const val HORIZONTAL_MARGIN_RATIO = 0.18
    private const val VERTICAL_MARGIN_RATIO = 0.08

    // Final reduction applied to the computed zoom so the island has a little margin even
    // after the bounds fitting computation. This also keeps the initial zoom identical on
    // all screens because we base the calculation on the screen width (see computeZoom).
    private const val EXTRA_ZOOM_PADDING = 0.5

    /**
     * Calculate the optimal camera settings for the given container size.
     */
    fun calculateForSize(widthPx: Int, heightPx: Int): MapCameraConfig {
        if (widthPx <= 0 || heightPx <= 0) {
            return MapCameraConfig(latitude = 39.95, longitude = 4.05, zoom = DEFAULT_ZOOM)
        }

        val latRange = MAX_LAT - MIN_LAT
        val lonRange = MAX_LON - MIN_LON

        val south = (MIN_LAT - latRange * VERTICAL_MARGIN_RATIO).coerceAtLeast(-85.0)
        val north = (MAX_LAT + latRange * VERTICAL_MARGIN_RATIO).coerceAtMost(85.0)
        val west = MIN_LON - lonRange * HORIZONTAL_MARGIN_RATIO
        val east = MAX_LON + lonRange * HORIZONTAL_MARGIN_RATIO

        val zoom = computeZoom(widthPx, west, east)
        val latitude = (south + north) / 2.0
        val longitude = (west + east) / 2.0

        return MapCameraConfig(latitude = latitude, longitude = longitude, zoom = zoom)
    }

    private fun computeZoom(
        widthPx: Int,
        west: Double,
        east: Double
    ): Double {
        // We only consider horizontal bounds so every screen (Map, Tracking, POIs) ends up
        // with the same initial zoom as long as the available width is the same.
        val lonDelta = ((east - west + 360.0) % 360.0).coerceAtLeast(1e-6)
        val lonFraction = (lonDelta / 360.0).coerceAtLeast(1e-6)

        val zoomLon = log2(widthPx / (TILE_SIZE * lonFraction))
        val targetZoom = zoomLon - EXTRA_ZOOM_PADDING
        return targetZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
    }

    private fun log2(value: Double): Double {
        return ln(max(value, 1e-6)) / LN2
    }

    private val LN2 = ln(2.0)
}
