package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Use case for starting a new tracking session.
 * Creates a new session with the current timestamp and optional route association.
 */
class StartTrackingSessionUseCase(
    private val trackingRepository: TrackingRepository
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(routeId: Int? = null): TrackingSession {
        val session = TrackingSession(
            id = Uuid.random().toString(),
            routeId = routeId,
            startTime = Clock.System.now(),
            endTime = null,
            distanceMeters = 0.0,
            durationSeconds = 0,
            averageSpeedKmh = 0.0,
            maxSpeedKmh = 0.0,
            elevationGainMeters = 0,
            elevationLossMeters = 0,
            trackPoints = emptyList(),
            isCompleted = false,
            notes = ""
        )

        trackingRepository.saveSession(session)
        return session
    }
}
