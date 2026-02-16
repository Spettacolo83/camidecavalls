package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import kotlinx.datetime.Clock

/**
 * Use case for adding a GPS track point to an active tracking session.
 * Uses direct O(1) insert — does NOT reload or rewrite existing points.
 */
class AddTrackPointUseCase(
    private val trackingRepository: TrackingRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        latitude: Double,
        longitude: Double,
        altitude: Double? = null,
        accuracy: Float? = null,
        speed: Float? = null,
        bearing: Float? = null
    ) {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90 and 90, got $latitude"
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180 and 180, got $longitude"
        }

        val trackPoint = TrackPoint(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            timestamp = Clock.System.now(),
            speedKmh = speed?.let { it * 3.6 } // Convert m/s to km/h
        )

        trackingRepository.insertTrackPoint(sessionId, trackPoint)
    }
}
