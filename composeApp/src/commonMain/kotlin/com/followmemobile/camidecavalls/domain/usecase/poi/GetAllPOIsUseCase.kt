package com.followmemobile.camidecavalls.domain.usecase.poi

import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.POIRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving all Points of Interest.
 * Returns a Flow for reactive updates.
 */
class GetAllPOIsUseCase(
    private val poiRepository: POIRepository
) {
    operator fun invoke(): Flow<List<PointOfInterest>> {
        return poiRepository.getAllPOIs()
    }
}
