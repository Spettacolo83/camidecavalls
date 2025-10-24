package com.followmemobile.camidecavalls.domain.usecase.route

import com.followmemobile.camidecavalls.data.RouteData
import com.followmemobile.camidecavalls.domain.repository.RouteRepository
import kotlinx.coroutines.flow.first

/**
 * Use case to initialize the database with route data on first app launch.
 * Checks if routes already exist to avoid duplicates.
 */
class InitializeDatabaseUseCase(
    private val routeRepository: RouteRepository,
    private val saveRoutesUseCase: SaveRoutesUseCase
) {
    /**
     * Initializes the database with route data if it's empty.
     * @return true if initialization was performed, false if routes already existed
     */
    suspend operator fun invoke(): Boolean {
        // Check if routes already exist
        val existingRoutes = routeRepository.getAllRoutes().first()

        if (existingRoutes.isNotEmpty()) {
            // Database already initialized
            return false
        }

        // Database is empty, initialize with route data
        saveRoutesUseCase(RouteData.routes)

        return true
    }
}
