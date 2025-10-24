package com.followmemobile.camidecavalls.domain.usecase.poi

import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.POIRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving Points of Interest associated with a specific route.
 * Useful for showing POIs along a particular trail stage.
 */
class GetPOIsByRouteUseCase(
    private val poiRepository: POIRepository
) {
    operator fun invoke(routeId: Int): Flow<List<PointOfInterest>> {
        require(routeId > 0) { "Route ID must be positive, got $routeId" }
        return poiRepository.getPOIsByRoute(routeId)
    }
}
