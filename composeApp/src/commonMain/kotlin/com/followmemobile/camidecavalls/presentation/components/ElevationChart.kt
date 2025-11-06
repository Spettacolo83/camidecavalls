package com.followmemobile.camidecavalls.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Data class representing elevation profile data points
 */
data class ElevationPoint(
    val distanceKm: Double,
    val elevationMeters: Double
)

/**
 * Elevation chart component that displays an interactive elevation profile.
 *
 * Features:
 * - Interactive tap to highlight specific points
 * - Shows current elevation, distance, and gradient
 * - Smooth gradient fill under the line
 * - Material 3 design
 */
@Composable
fun ElevationChart(
    gpxData: String,
    strings: com.followmemobile.camidecavalls.domain.util.LocalizedStrings,
    modifier: Modifier = Modifier,
    onPointSelected: ((Pair<Double, Double>?) -> Unit)? = null
) {
    // Parse elevation data from GeoJSON
    val elevationPoints = remember(gpxData) {
        parseElevationData(gpxData)
    }

    if (elevationPoints.isEmpty()) {
        Text(
            text = "No elevation data available",
            modifier = modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    // Track selected point
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val density = LocalDensity.current

    Column(modifier = modifier) {
        // Chart - Use BoxWithConstraints to get actual width
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val chartWidth = maxWidth  // Actual width of the chart

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .pointerInput(Unit) {
                        // Helper function to update selected point from position
                        fun updateSelectedPoint(x: Float) {
                            val chartWidth = size.width
                            val padding = 20f
                            val pointsCount = elevationPoints.size
                            val effectiveWidth = chartWidth - 2 * padding

                            // Adjust for padding and calculate index
                            val adjustedX = x - padding
                            val pointSpacing = effectiveWidth / (pointsCount - 1)
                            val tappedIndex = ((adjustedX / pointSpacing) + 0.5).toInt()
                                .coerceIn(0, pointsCount - 1)

                            selectedPointIndex = tappedIndex
                            // Get the coordinates for this point from the original GPX data
                            val coordinates = parseGeoJsonCoordinates(gpxData)
                            if (tappedIndex < coordinates.size) {
                                val coord = coordinates[tappedIndex]
                                onPointSelected?.invoke(Pair(coord.first, coord.second)) // lon, lat
                            }
                        }

                        // Detect both tap and drag gestures
                        detectDragGestures(
                            onDragStart = { offset ->
                                updateSelectedPoint(offset.x)
                            },
                            onDrag = { change, _ ->
                                updateSelectedPoint(change.position.x)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        // Also handle simple taps
                        detectTapGestures { offset ->
                            val chartWidth = size.width
                            val padding = 20f
                            val pointsCount = elevationPoints.size
                            val effectiveWidth = chartWidth - 2 * padding

                            val adjustedX = offset.x - padding
                            val pointSpacing = effectiveWidth / (pointsCount - 1)
                            val tappedIndex = ((adjustedX / pointSpacing) + 0.5).toInt()
                                .coerceIn(0, pointsCount - 1)

                            selectedPointIndex = tappedIndex
                            val coordinates = parseGeoJsonCoordinates(gpxData)
                            if (tappedIndex < coordinates.size) {
                                val coord = coordinates[tappedIndex]
                                onPointSelected?.invoke(Pair(coord.first, coord.second))
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val padding = 20f

                // Calculate min/max for scaling
                val minElevation = elevationPoints.minOf { it.elevationMeters }
                val maxElevation = elevationPoints.maxOf { it.elevationMeters }
                val elevationRange = maxElevation - minElevation

                // Add some padding to the range
                val paddedMin = minElevation - (elevationRange * 0.1)
                val paddedMax = maxElevation + (elevationRange * 0.1)
                val paddedRange = paddedMax - paddedMin

                // Create path for the elevation line with smooth curves
                val path = Path()
                val fillPath = Path()

                // Convert elevation points to screen coordinates
                val points = elevationPoints.mapIndexed { index, point ->
                    val x = padding + (index.toFloat() / (elevationPoints.size - 1)) * (width - 2 * padding)
                    val normalizedElevation = (point.elevationMeters - paddedMin) / paddedRange
                    val y = height - padding - (normalizedElevation * (height - 2 * padding)).toFloat()
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    // Start paths
                    path.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, height - padding)
                    fillPath.lineTo(points.first().x, points.first().y)

                    // Draw smooth curves using quadratic Bezier
                    for (i in 0 until points.size - 1) {
                        val current = points[i]
                        val next = points[i + 1]

                        // Control point is the midpoint between current and next
                        val controlX = (current.x + next.x) / 2f
                        val controlY = (current.y + next.y) / 2f

                        // Draw quadratic Bezier curve
                        path.quadraticTo(
                            current.x, current.y,  // Control point at current
                            controlX, controlY      // End at midpoint
                        )
                        fillPath.quadraticTo(
                            current.x, current.y,
                            controlX, controlY
                        )

                        // If this is the last segment, complete the curve to the final point
                        if (i == points.size - 2) {
                            path.quadraticTo(
                                next.x, next.y,
                                next.x, next.y
                            )
                            fillPath.quadraticTo(
                                next.x, next.y,
                                next.x, next.y
                            )
                        }
                    }

                    // Close fill path
                    fillPath.lineTo(points.last().x, height - padding)
                    fillPath.close()
                }

                // Draw gradient fill
                drawPath(
                    path = fillPath,
                    color = primaryColor.copy(alpha = 0.2f)
                )

                // Draw elevation line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round
                    )
                )

                // Draw grid lines
                val gridLineCount = 5
                repeat(gridLineCount) { i ->
                    val y = padding + (i.toFloat() / (gridLineCount - 1)) * (height - 2 * padding)
                    drawLine(
                        color = onSurfaceColor.copy(alpha = 0.1f),
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f
                    )
                }

                // Draw selected point indicator
                selectedPointIndex?.let { index ->
                    val point = elevationPoints[index]
                    val x = padding + (index.toFloat() / (elevationPoints.size - 1)) * (width - 2 * padding)
                    val normalizedElevation = (point.elevationMeters - paddedMin) / paddedRange
                    val y = height - padding - (normalizedElevation * (height - 2 * padding)).toFloat()

                    // Vertical line
                    drawLine(
                        color = primaryColor.copy(alpha = 0.5f),
                        start = Offset(x, padding),
                        end = Offset(x, height - padding),
                        strokeWidth = 2f
                    )

                    // Point marker
                    drawCircle(
                        color = primaryColor,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = surfaceColor,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }
            }

            // Info overlay above selected point
            selectedPointIndex?.let { index ->
                val point = elevationPoints[index]
                val padding = with(density) { 20f.toDp() }

                // Calculate position using actual chart width
                val pointX = padding + (chartWidth - padding * 2) * (index.toFloat() / (elevationPoints.size - 1))

                Box(
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                // Center the box horizontally on the point
                                val xOffset = with(density) { pointX.toPx().toInt() } - placeable.width / 2
                                val yOffset = with(density) { 4.dp.toPx().toInt() }
                                placeable.place(xOffset, yOffset)
                            }
                        }
                        .background(
                            color = primaryColor.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${point.elevationMeters.toInt()} m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(point.distanceKm * 100).toInt() / 100.0} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Parse elevation data from GeoJSON string
 */
private fun parseElevationData(gpxData: String): List<ElevationPoint> {
    return try {
        val json = Json.parseToJsonElement(gpxData).jsonObject
        val coordinates = json["coordinates"]?.jsonArray ?: return emptyList()

        var cumulativeDistance = 0.0
        val points = mutableListOf<ElevationPoint>()

        coordinates.forEachIndexed { index, coord ->
            val coordArray = coord.jsonArray
            if (coordArray.size >= 3) {
                val lon = coordArray[0].jsonPrimitive.content.toDouble()
                val lat = coordArray[1].jsonPrimitive.content.toDouble()
                val elevation = coordArray[2].jsonPrimitive.content.toDouble()

                // Calculate distance from previous point
                if (index > 0) {
                    val prevCoord = coordinates[index - 1].jsonArray
                    val prevLon = prevCoord[0].jsonPrimitive.content.toDouble()
                    val prevLat = prevCoord[1].jsonPrimitive.content.toDouble()

                    cumulativeDistance += calculateDistance(prevLat, prevLon, lat, lon)
                }

                points.add(
                    ElevationPoint(
                        distanceKm = cumulativeDistance,
                        elevationMeters = elevation
                    )
                )
            }
        }

        points
    } catch (e: Exception) {
        println("Error parsing elevation data: ${e.message}")
        emptyList()
    }
}

/**
 * Calculate gradient (slope) between two elevation points
 */
private fun calculateGradient(
    point1: ElevationPoint,
    point2: ElevationPoint
): Double {
    val distanceDiff = (point2.distanceKm - point1.distanceKm) * 1000.0 // Convert to meters
    val elevationDiff = point2.elevationMeters - point1.elevationMeters

    return if (distanceDiff > 0) {
        (elevationDiff / distanceDiff) * 100.0
    } else 0.0
}

/**
 * Calculate distance between two lat/lon coordinates (in km) using Haversine formula
 */
private fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val earthRadius = 6371.0 // km

    val dLat = (lat2 - lat1) * PI / 180.0
    val dLon = (lon2 - lon1) * PI / 180.0

    val a = sin(dLat / 2).pow(2.0) +
            cos(lat1 * PI / 180.0) *
            cos(lat2 * PI / 180.0) *
            sin(dLon / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

/**
 * Parse GeoJSON coordinates to get (lon, lat) pairs
 */
private fun parseGeoJsonCoordinates(geoJson: String): List<Pair<Double, Double>> {
    return try {
        val json = Json.parseToJsonElement(geoJson).jsonObject
        val coordinates = json["coordinates"]?.jsonArray ?: return emptyList()

        coordinates.map { coord ->
            val array = coord.jsonArray
            val lon = array[0].jsonPrimitive.content.toDouble()
            val lat = array[1].jsonPrimitive.content.toDouble()
            Pair(lon, lat)
        }
    } catch (e: Exception) {
        emptyList()
    }
}
