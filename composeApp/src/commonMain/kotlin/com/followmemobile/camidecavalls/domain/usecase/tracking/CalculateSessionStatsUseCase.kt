package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackPoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Use case for calculating statistics from a list of track points.
 * Uses Haversine formula for distance calculation.
 */
class CalculateSessionStatsUseCase {

    data class SessionStats(
        val distanceMeters: Double,
        val maxSpeedKmh: Double,
        val elevationGainMeters: Int,
        val elevationLossMeters: Int
    )

    companion object {
        // GPS altitude noise is typically ±10-30m. A dead band of 3m filters out
        // small fluctuations while still capturing real climbs/descents on trails.
        private const val ELEVATION_DEAD_BAND_METERS = 3.0
    }

    operator fun invoke(trackPoints: List<TrackPoint>): SessionStats {
        if (trackPoints.size < 2) {
            return SessionStats(
                distanceMeters = 0.0,
                maxSpeedKmh = 0.0,
                elevationGainMeters = 0,
                elevationLossMeters = 0
            )
        }

        var totalDistance = 0.0
        var maxSpeed = 0.0
        var elevationGain = 0.0
        var elevationLoss = 0.0

        // Track the last "committed" altitude for dead band calculation.
        // We only update the reference altitude when the change exceeds the dead band,
        // preventing GPS noise from accumulating into false elevation gain/loss.
        var referenceAltitude: Double? = trackPoints.firstNotNullOfOrNull { it.altitude }

        for (i in 1 until trackPoints.size) {
            val prev = trackPoints[i - 1]
            val current = trackPoints[i]

            // Calculate distance between consecutive points
            val distance = calculateDistance(
                prev.latitude, prev.longitude,
                current.latitude, current.longitude
            )
            totalDistance += distance

            // Track max speed
            current.speedKmh?.let { speed ->
                maxSpeed = max(maxSpeed, speed)
            }

            // Calculate elevation changes with dead band to filter GPS altitude noise
            val currentAlt = current.altitude
            val refAlt = referenceAltitude
            if (currentAlt != null && refAlt != null) {
                val elevationChange = currentAlt - refAlt
                if (abs(elevationChange) >= ELEVATION_DEAD_BAND_METERS) {
                    if (elevationChange > 0) {
                        elevationGain += elevationChange
                    } else {
                        elevationLoss += (-elevationChange)
                    }
                    referenceAltitude = currentAlt
                }
            } else if (currentAlt != null) {
                referenceAltitude = currentAlt
            }
        }

        return SessionStats(
            distanceMeters = totalDistance,
            maxSpeedKmh = maxSpeed,
            elevationGainMeters = elevationGain.toInt(),
            elevationLossMeters = elevationLoss.toInt()
        )
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula.
     * Returns distance in meters.
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusMeters * c
    }
}
