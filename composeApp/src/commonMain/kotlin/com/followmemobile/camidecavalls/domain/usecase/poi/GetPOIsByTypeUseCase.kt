package com.followmemobile.camidecavalls.domain.usecase.poi

import com.followmemobile.camidecavalls.domain.model.POIType
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.POIRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving Points of Interest filtered by type.
 * Useful for showing category-specific POIs (beaches, viewpoints, etc.).
 */
class GetPOIsByTypeUseCase(
    private val poiRepository: POIRepository
) {
    operator fun invoke(type: POIType): Flow<List<PointOfInterest>> {
        return poiRepository.getPOIsByType(type)
    }
}
