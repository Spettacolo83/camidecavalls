package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import kotlinx.datetime.Clock
import kotlin.math.max

/**
 * Use case for stopping an active tracking session.
 * Calculates final statistics and marks the session as completed.
 */
class StopTrackingSessionUseCase(
    private val trackingRepository: TrackingRepository,
    private val calculateSessionStatsUseCase: CalculateSessionStatsUseCase
) {
    suspend operator fun invoke(sessionId: String, notes: String = ""): TrackingSession? {
        val session = trackingRepository.getSessionById(sessionId) ?: return null

        if (session.isCompleted) {
            return session // Already completed
        }

        val endTime = Clock.System.now()
        val durationSeconds = (endTime - session.startTime).inWholeSeconds

        // Calculate statistics from track points
        val stats = calculateSessionStatsUseCase(session.trackPoints)

        val updatedSession = session.copy(
            endTime = endTime,
            durationSeconds = durationSeconds,
            distanceMeters = stats.distanceMeters,
            averageSpeedKmh = if (durationSeconds > 0) {
                (stats.distanceMeters / 1000.0) / (durationSeconds / 3600.0)
            } else 0.0,
            maxSpeedKmh = stats.maxSpeedKmh,
            elevationGainMeters = stats.elevationGainMeters,
            elevationLossMeters = stats.elevationLossMeters,
            isCompleted = true,
            notes = notes
        )

        trackingRepository.updateSession(updatedSession)
        return updatedSession
    }
}
