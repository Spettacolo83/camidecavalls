package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import kotlinx.datetime.Clock

/**
 * Use case for adding a GPS track point to an active tracking session.
 * Called periodically during tracking to record the user's position.
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

        val session = trackingRepository.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session not found: $sessionId")

        if (session.isCompleted) {
            throw IllegalStateException("Cannot add track points to completed session")
        }

        val trackPoint = TrackPoint(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            timestamp = Clock.System.now(),
            speedKmh = speed?.let { it * 3.6 } // Convert m/s to km/h
        )

        val updatedSession = session.copy(
            trackPoints = session.trackPoints + trackPoint
        )

        trackingRepository.updateSession(updatedSession)
    }
}
