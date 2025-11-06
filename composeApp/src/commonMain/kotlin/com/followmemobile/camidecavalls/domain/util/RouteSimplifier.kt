package com.followmemobile.camidecavalls.domain.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Utility object for simplifying GPS route data using the Douglas-Peucker algorithm.
 * This reduces the number of points in a route while maintaining its overall shape,
 * which is essential for performance when displaying multiple routes simultaneously.
 */
object RouteSimplifier {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Simplifies a GeoJSON LineString using the Douglas-Peucker algorithm.
     *
     * @param geoJsonString The GeoJSON LineString as a string
     * @param tolerance The perpendicular distance threshold in degrees (default: 0.0001 ≈ 11 meters)
     *                  Higher values = more simplification, fewer points
     *                  Recommended values:
     *                  - 0.00005 (≈5.5m) - High detail for individual route view
     *                  - 0.0001 (≈11m) - Medium detail for full map view (default)
     *                  - 0.0002 (≈22m) - Lower detail for performance
     * @return Simplified GeoJSON LineString as a string, or original if parsing fails
     */
    fun simplifyGeoJson(geoJsonString: String, tolerance: Double = 0.0001): String {
        return try {
            val geoJson = json.parseToJsonElement(geoJsonString).jsonObject
            val coordinates = geoJson["coordinates"]?.jsonArray ?: return geoJsonString

            // Convert JsonArray to List<Point>
            val points = coordinates.map { coordArray ->
                val coord = coordArray.jsonArray
                Point(
                    longitude = coord[0].jsonPrimitive.double,
                    latitude = coord[1].jsonPrimitive.double
                )
            }

            // Apply Douglas-Peucker simplification
            val simplifiedPoints = douglasPeucker(points, tolerance)

            // Convert back to GeoJSON format
            val simplifiedCoordinates = simplifiedPoints.map { point ->
                "[${point.longitude},${point.latitude}]"
            }.joinToString(",")

            """{"type":"LineString","coordinates":[$simplifiedCoordinates]}"""
        } catch (e: Exception) {
            println("⚠️ Error simplifying GeoJSON: ${e.message}")
            geoJsonString // Return original on error
        }
    }

    /**
     * Douglas-Peucker algorithm implementation for line simplification.
     *
     * This algorithm recursively divides the line and removes points that are within
     * the specified tolerance distance from the line segment between the first and last points.
     *
     * @param points List of points to simplify
     * @param tolerance Maximum perpendicular distance threshold
     * @return Simplified list of points
     */
    private fun douglasPeucker(points: List<Point>, tolerance: Double): List<Point> {
        if (points.size <= 2) {
            return points
        }

        // Find the point with the maximum perpendicular distance from the line segment
        var maxDistance = 0.0
        var maxIndex = 0
        val firstPoint = points.first()
        val lastPoint = points.last()

        for (i in 1 until points.size - 1) {
            val distance = perpendicularDistance(points[i], firstPoint, lastPoint)
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        // If max distance is greater than tolerance, recursively simplify
        return if (maxDistance > tolerance) {
            // Recursively simplify both segments
            val leftSegment = douglasPeucker(points.subList(0, maxIndex + 1), tolerance)
            val rightSegment = douglasPeucker(points.subList(maxIndex, points.size), tolerance)

            // Combine results (excluding duplicate middle point)
            leftSegment.dropLast(1) + rightSegment
        } else {
            // All points are within tolerance, keep only endpoints
            listOf(firstPoint, lastPoint)
        }
    }

    /**
     * Calculates the perpendicular distance from a point to a line segment.
     *
     * Uses the formula for distance from a point to a line in 2D space.
     * Note: This is an approximation treating lat/lon as Cartesian coordinates,
     * which is acceptable for small distances (< 100km).
     *
     * @param point The point to measure distance from
     * @param lineStart Start point of the line segment
     * @param lineEnd End point of the line segment
     * @return Perpendicular distance in degrees (approximate)
     */
    private fun perpendicularDistance(
        point: Point,
        lineStart: Point,
        lineEnd: Point
    ): Double {
        val x = point.longitude
        val y = point.latitude
        val x1 = lineStart.longitude
        val y1 = lineStart.latitude
        val x2 = lineEnd.longitude
        val y2 = lineEnd.latitude

        // Calculate line segment length squared
        val lineLength = (x2 - x1).pow(2) + (y2 - y1).pow(2)

        if (lineLength == 0.0) {
            // Line segment is actually a point, return distance to that point
            return sqrt((x - x1).pow(2) + (y - y1).pow(2))
        }

        // Calculate perpendicular distance using cross product formula
        val numerator = abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1)
        val denominator = sqrt(lineLength)

        return numerator / denominator
    }

    /**
     * Analyzes and reports simplification statistics.
     * Useful for debugging and choosing optimal tolerance values.
     *
     * @param originalGeoJson Original GeoJSON LineString
     * @param simplifiedGeoJson Simplified GeoJSON LineString
     * @return SimplificationStats with point counts and reduction percentage
     */
    fun analyzeSimplification(originalGeoJson: String, simplifiedGeoJson: String): SimplificationStats {
        return try {
            val originalCoords = json.parseToJsonElement(originalGeoJson)
                .jsonObject["coordinates"]?.jsonArray?.size ?: 0
            val simplifiedCoords = json.parseToJsonElement(simplifiedGeoJson)
                .jsonObject["coordinates"]?.jsonArray?.size ?: 0

            val reductionPercentage = if (originalCoords > 0) {
                ((originalCoords - simplifiedCoords).toDouble() / originalCoords * 100)
            } else {
                0.0
            }

            SimplificationStats(
                originalPoints = originalCoords,
                simplifiedPoints = simplifiedCoords,
                reductionPercentage = reductionPercentage
            )
        } catch (e: Exception) {
            SimplificationStats(0, 0, 0.0)
        }
    }

    /**
     * Simplifies all routes in a list and returns statistics.
     *
     * @param routes List of routes to simplify
     * @param tolerance Simplification tolerance
     * @return Pair of (simplified routes list, total stats)
     */
    fun simplifyRoutes(
        routes: List<com.followmemobile.camidecavalls.domain.model.Route>,
        tolerance: Double = 0.0001
    ): SimplificationResult {
        var totalOriginal = 0
        var totalSimplified = 0

        val simplifiedRoutes = routes.map { route ->
            if (route.gpxData != null) {
                val simplified = simplifyGeoJson(route.gpxData, tolerance)
                val stats = analyzeSimplification(route.gpxData, simplified)

                totalOriginal += stats.originalPoints
                totalSimplified += stats.simplifiedPoints

                route.copy(gpxData = simplified)
            } else {
                route
            }
        }

        val totalReduction = if (totalOriginal > 0) {
            ((totalOriginal - totalSimplified).toDouble() / totalOriginal * 100)
        } else {
            0.0
        }

        return SimplificationResult(
            routes = simplifiedRoutes,
            stats = SimplificationStats(
                originalPoints = totalOriginal,
                simplifiedPoints = totalSimplified,
                reductionPercentage = totalReduction
            )
        )
    }

    /**
     * Simple data class representing a geographic point.
     */
    private data class Point(
        val longitude: Double,
        val latitude: Double
    )
}

/**
 * Statistics about route simplification.
 */
data class SimplificationStats(
    val originalPoints: Int,
    val simplifiedPoints: Int,
    val reductionPercentage: Double
) {
    override fun toString(): String {
        val reductionFormatted = (reductionPercentage * 10).toInt() / 10.0
        return "SimplificationStats(original=$originalPoints, simplified=$simplifiedPoints, " +
                "reduction=$reductionFormatted%)"
    }
}

/**
 * Result of simplifying multiple routes.
 */
data class SimplificationResult(
    val routes: List<com.followmemobile.camidecavalls.domain.model.Route>,
    val stats: SimplificationStats
)
