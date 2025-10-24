package com.followmemobile.camidecavalls.domain.usecase.route

import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.RouteRepository

/**
 * Use case for retrieving a specific route by its ID.
 */
class GetRouteByIdUseCase(
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(routeId: Int): Route? {
        return routeRepository.getRouteById(routeId)
    }
}
