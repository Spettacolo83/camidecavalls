package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving all tracking sessions.
 * Returns sessions ordered by start time (newest first).
 */
class GetAllSessionsUseCase(
    private val trackingRepository: TrackingRepository
) {
    operator fun invoke(): Flow<List<TrackingSession>> {
        return trackingRepository.getAllSessions()
    }
}
