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

        // If database version changed (migration needed), recreate the route table FIRST
        if (currentDbVersion > 0 && currentDbVersion < DATABASE_VERSION) {
            routeRepository.recreateRouteTable()
            // Re-seed database with updated route data
            saveRoutesUseCase(RouteData.routes)
            appPreferences.setDatabaseVersion(DATABASE_VERSION)
            return true
        }

        // Check if we need to initialize (new database or empty database)
        if (currentDbVersion == 0) {
            // New database - SQLDelight will create tables automatically
            saveRoutesUseCase(RouteData.routes)
            appPreferences.setDatabaseVersion(DATABASE_VERSION)
            return true
        }

        // Database is up to date - check if it has data
        val existingRoutes = routeRepository.getAllRoutes().first()
        if (existingRoutes.isNotEmpty()) {
            return false // All good
        }

        // Database version is correct but no data - re-seed
        saveRoutesUseCase(RouteData.routes)
        return true
    }

    companion object {
        // Increment this version when RouteData changes to force re-seed
        // Version 8: Fixed Route 11 duplicate coordinate issue
        // Version 9: Reordered Route 11 coordinates to fix 7.7km jump (start/end were swapped)
        // Version 10: Added multilingual route descriptions (CA, ES, EN, DE, FR, IT)
        // Version 11: Added database schema columns for multilingual descriptions
        // Version 12: Added GPX data with elevation for all 20 routes
        private const val DATABASE_VERSION = 12
    }
}
