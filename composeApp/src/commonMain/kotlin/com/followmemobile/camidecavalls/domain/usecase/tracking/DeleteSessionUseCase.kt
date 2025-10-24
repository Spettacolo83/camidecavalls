package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.repository.TrackingRepository

/**
 * Use case for deleting a tracking session.
 * Removes the session and all associated track points (cascade delete).
 */
class DeleteSessionUseCase(
    private val trackingRepository: TrackingRepository
) {
    suspend operator fun invoke(sessionId: String) {
        require(sessionId.isNotBlank()) { "Session ID cannot be blank" }
        trackingRepository.deleteSession(sessionId)
    }
}
