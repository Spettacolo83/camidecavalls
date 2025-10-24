package com.followmemobile.camidecavalls.domain.usecase.route

import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.RouteRepository

/**
 * Use case for saving routes to the database.
 * Used for initial data population or updates.
 */
class SaveRoutesUseCase(
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(routes: List<Route>) {
        require(routes.isNotEmpty()) { "Cannot save empty route list" }
        routeRepository.saveRoutes(routes)
    }
}
