package com.followmemobile.camidecavalls.domain.usecase.route

import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.RouteRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving all routes of Camí de Cavalls.
 * Returns a Flow of route list for reactive updates.
 */
class GetAllRoutesUseCase(
    private val routeRepository: RouteRepository
) {
    operator fun invoke(): Flow<List<Route>> {
        return routeRepository.getAllRoutes()
    }
}
