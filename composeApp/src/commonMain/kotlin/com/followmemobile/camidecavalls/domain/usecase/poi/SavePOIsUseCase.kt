package com.followmemobile.camidecavalls.domain.usecase.poi

import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.POIRepository

/**
 * Use case for saving Points of Interest to the database.
 * Used for initial data population or updates.
 */
class SavePOIsUseCase(
    private val poiRepository: POIRepository
) {
    suspend operator fun invoke(pois: List<PointOfInterest>) {
        require(pois.isNotEmpty()) { "Cannot save empty POI list" }
        poiRepository.savePOIs(pois)
    }
}
