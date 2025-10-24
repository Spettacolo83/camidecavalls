package com.followmemobile.camidecavalls.domain.usecase.route

import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.RouteRepository

/**
 * Use case for retrieving a specific route by its stage number (1-20).
 */
class GetRouteByNumberUseCase(
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(stageNumber: Int): Route? {
        require(stageNumber in 1..20) {
            "Stage number must be between 1 and 20, got $stageNumber"
        }
        return routeRepository.getRouteByNumber(stageNumber)
    }
}
