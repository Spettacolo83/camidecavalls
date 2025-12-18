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
        // Version 13: Fixed SQLDelight migrations to handle POI table creation properly
        // Version 14: Updated Route 1 elevation data from official camidecavalls.com graph
        // Version 15: Pixel-accurate elevation profile (96 points) from official graph
        // Version 16: High-resolution pixel-extracted elevation (200+ points) from official graph
        // Version 17: Corrected elevation profile with proper Sa Mesquida valley (stays low 3.5-4.5km)
        // Version 18: Pixel-by-pixel extraction (151 points) with correct graph calibration
        // Version 19: Fixed graph boundaries (X=10-4100) to capture full elevation range from start to end
        // Version 20: Added Route 2 (Es Grau - Favàritx) pixel-extracted elevation profile
        // Version 21: Fixed Route 2 end boundary to capture full descent (7.9m at end)
        // Version 22: Fixed Route 2 profile scaling to match GPX distance (8.63km vs 7.9km)
        // Version 23: Added Route 3 (Favàritx - Arenal d'en Castell) elevation profile (0-78m, 13.60km)
        // Version 24: Fixed Route 3 double peak artifact - smoothed descent after main peak
        // Version 25: Added Route 4 (Arenal d'en Castell - Cala Tirant) elevation profile (3-38m, 10.77km)
        // Version 26: Fixed Route 4 graph boundaries (GRAPH_LEFT=150, GRAPH_RIGHT=4000) for correct feature positioning
        // Version 27: Added Route 5 (Son Parc - Fornells) elevation profile (0-52m, 9.59km)
        // Version 28: Fixed Route 4 end elevation (corrected final descent from 18m to 11.6m)
        // Version 29: Updated Route 5 elevation profile with pixel-extracted data (1-47m)
        // Version 30: Added Route 6 (Fornells - Cala Tirant) elevation profile (2-118m, 8.61km)
        // Version 31: Fixed Route 6 peak detection (extended Y scan range to capture 118m peak)
        // Version 32: Complete elevation profile update for all 20 routes from official camidecavalls.com images
        // Version 33: Added name field to TrackingSessionEntity for session naming in notebook
        private const val DATABASE_VERSION = 33
    }
}
