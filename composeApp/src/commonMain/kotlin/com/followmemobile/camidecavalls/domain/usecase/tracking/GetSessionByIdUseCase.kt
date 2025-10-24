package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository

/**
 * Use case for retrieving a specific tracking session by ID.
 */
class GetSessionByIdUseCase(
    private val trackingRepository: TrackingRepository
) {
    suspend operator fun invoke(sessionId: String): TrackingSession? {
        require(sessionId.isNotBlank()) { "Session ID cannot be blank" }
        return trackingRepository.getSessionById(sessionId)
    }
}
