package com.followmemobile.camidecavalls.domain.usecase.route

import com.followmemobile.camidecavalls.data.RouteData
import com.followmemobile.camidecavalls.data.local.AppPreferences
import com.followmemobile.camidecavalls.domain.repository.RouteRepository
import kotlinx.coroutines.flow.first

/**
 * Use case to initialize the database with route data on first app launch
 * or when the database version changes.
 */
class InitializeDatabaseUseCase(
    private val routeRepository: RouteRepository,
    private val saveRoutesUseCase: SaveRoutesUseCase,
    private val appPreferences: AppPreferences
) {
    /**
     * Initializes the database with route data if it's empty or outdated.
     * @return true if initialization was performed, false if routes were up to date
     */
    suspend operator fun invoke(): Boolean {
        val currentDbVersion = appPreferences.getDatabaseVersion()
        val existingRoutes = routeRepository.getAllRoutes().first()

        // Re-seed database if version changed or database is empty
        val needsReseed = currentDbVersion < DATABASE_VERSION || existingRoutes.isEmpty()

        if (!needsReseed) {
            // Database is up to date
            return false
        }

        // Re-seed database with updated route data
        saveRoutesUseCase(RouteData.routes)
        appPreferences.setDatabaseVersion(DATABASE_VERSION)

        return true
    }

    companion object {
        // Increment this version when RouteData changes to force re-seed
        private const val DATABASE_VERSION = 6
    }
}
