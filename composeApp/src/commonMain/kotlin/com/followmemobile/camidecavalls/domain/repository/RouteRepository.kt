package com.followmemobile.camidecavalls.domain.repository

import com.followmemobile.camidecavalls.domain.model.Route
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Route operations.
 * Implementations handle data source (local DB, remote API in V2).
 */
interface RouteRepository {

    /**
     * Get all 20 routes of Camí de Cavalls
     */
    fun getAllRoutes(): Flow<List<Route>>

    /**
     * Get a specific route by ID
     */
    suspend fun getRouteById(id: Int): Route?

    /**
     * Get a route by its number (1-20)
     */
    suspend fun getRouteByNumber(number: Int): Route?

    /**
     * Save or update routes (used for initial data seeding)
     */
    suspend fun saveRoutes(routes: List<Route>)

    /**
     * Get GPX data for a specific route
     */
    suspend fun getRouteGpxData(routeId: Int): String?

    /**
     * Recreate the route table with updated schema (for migrations)
     */
    suspend fun recreateRouteTable()
}
