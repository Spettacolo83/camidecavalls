package com.followmemobile.camidecavalls.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

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

    // Additional padding (percentage of the island size) applied to each axis. These ratios
    // were tuned so the island is fully visible with a visible margin on tablets as well as
    // on narrow phones.
    private const val HORIZONTAL_MARGIN_RATIO = 0.45
    private const val VERTICAL_MARGIN_RATIO = 0.22

    // Final reduction applied to the computed zoom so the island has a little margin even
    // after the bounds fitting computation.
    private const val EXTRA_ZOOM_PADDING = 0.85

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

        val zoom = computeZoom(widthPx, heightPx, south, north, west, east)
        val latitude = (south + north) / 2.0
        val longitude = (west + east) / 2.0

        return MapCameraConfig(latitude = latitude, longitude = longitude, zoom = zoom)
    }

    private fun computeZoom(
        widthPx: Int,
        heightPx: Int,
        south: Double,
        north: Double,
        west: Double,
        east: Double
    ): Double {
        val latFraction = ((latRad(north) - latRad(south)) / PI).coerceAtLeast(1e-6)
        val lonDelta = ((east - west + 360.0) % 360.0).coerceAtLeast(1e-6)
        val lonFraction = (lonDelta / 360.0).coerceAtLeast(1e-6)

        val zoomLat = log2(heightPx / (TILE_SIZE * latFraction))
        val zoomLon = log2(widthPx / (TILE_SIZE * lonFraction))
        val targetZoom = min(zoomLat, zoomLon) - EXTRA_ZOOM_PADDING
        return targetZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
    }

    private fun latRad(lat: Double): Double {
        val sin = sin(lat * PI / 180.0)
        val radX2 = ln((1 + sin) / (1 - sin)) / 2.0
        return max(min(radX2, PI), -PI)
    }

    private fun log2(value: Double): Double {
        return ln(max(value, 1e-6)) / LN2
    }

    private val LN2 = ln(2.0)
}

class MenorcaViewportState(
    private val calculator: MenorcaViewportCalculator = MenorcaViewportCalculator
) {
    private var lastWidth = 0
    private var lastHeight = 0
    private var lastCamera = calculator.calculateForSize(0, 0)

    fun updateSize(widthPx: Int, heightPx: Int): MapCameraConfig {
        if (widthPx == lastWidth && heightPx == lastHeight) {
            return lastCamera
        }
        lastWidth = widthPx
        lastHeight = heightPx
        lastCamera = calculator.calculateForSize(widthPx, heightPx)
        return lastCamera
    }

    val camera: MapCameraConfig
        get() = lastCamera
}

@Composable
fun rememberMenorcaViewportState(): MenorcaViewportState {
    return remember { MenorcaViewportState() }
}
