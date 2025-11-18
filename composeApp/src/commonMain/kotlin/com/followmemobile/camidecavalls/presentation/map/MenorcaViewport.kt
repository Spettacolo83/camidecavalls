package com.followmemobile.camidecavalls.presentation.map

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

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

    private const val TILE_SIZE = 256.0
    private const val MAX_ZOOM = 18.0
    private const val MIN_ZOOM = 3.0
    private const val DEFAULT_ZOOM = 10.0

    // Geographic bounds (degrees) of the 20 official routes.
    private const val MIN_LAT = 39.8095598223
    private const val MAX_LAT = 40.0675523638
    private const val MIN_LON = 3.7952079277
    private const val MAX_LON = 4.3016355713

    // Additional padding (percentage of the island size) applied to each axis.
    private const val HORIZONTAL_MARGIN_RATIO = 0.12
    private const val VERTICAL_MARGIN_RATIO = 0.08

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

        val zoom = computeZoom(widthPx, heightPx, west, east, south, north)
        val latitude = (south + north) / 2.0
        val longitude = (west + east) / 2.0

        return MapCameraConfig(latitude = latitude, longitude = longitude, zoom = zoom)
    }

    private fun computeZoom(
        widthPx: Int,
        heightPx: Int,
        west: Double,
        east: Double,
        south: Double,
        north: Double
    ): Double {
        val lonDelta = ((east - west + 360.0) % 360.0).coerceAtLeast(1e-6)
        val lonFraction = (lonDelta / 360.0).coerceAtLeast(1e-6)

        val latFraction = (
            latToMercator(north) - latToMercator(south)
        ).let { abs(it) / PI }.coerceAtLeast(1e-6)

        val zoomLon = log2(widthPx / (TILE_SIZE * lonFraction))
        val zoomLat = log2(heightPx / (TILE_SIZE * latFraction))

        val targetZoom = min(zoomLon, zoomLat) - 0.3
        return targetZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
    }

    private fun latToMercator(lat: Double): Double {
        val clamped = lat.coerceIn(-85.05112878, 85.05112878)
        val rad = clamped * PI / 180.0
        return ln(kotlin.math.tan(PI / 4.0 + rad / 2.0))
    }

    private fun log2(value: Double): Double {
        return ln(max(value, 1e-6)) / LN2
    }

    private val LN2 = ln(2.0)
}
