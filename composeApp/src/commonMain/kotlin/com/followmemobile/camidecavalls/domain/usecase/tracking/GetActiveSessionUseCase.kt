package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving the currently active tracking session.
 * Returns a Flow that updates when the session changes.
 */
class GetActiveSessionUseCase(
    private val trackingRepository: TrackingRepository
) {
    operator fun invoke(): Flow<TrackingSession?> {
        return trackingRepository.getActiveSession()
    }
}
