package com.followmemobile.camidecavalls.domain.repository

import com.followmemobile.camidecavalls.domain.model.POIType
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Point of Interest operations.
 */
interface POIRepository {

    /**
     * Get all POIs
     */
    fun getAllPOIs(): Flow<List<PointOfInterest>>

    /**
     * Get POIs by type
     */
    fun getPOIsByType(type: POIType): Flow<List<PointOfInterest>>

    /**
     * Get POIs near a specific location within a radius (meters)
     */
    suspend fun getPOIsNearLocation(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): List<PointOfInterest>

    /**
     * Get POIs along a specific route
     */
    fun getPOIsByRoute(routeId: Int): Flow<List<PointOfInterest>>

    /**
     * Save or update POIs (used for initial data seeding)
     */
    suspend fun savePOIs(pois: List<PointOfInterest>)
}
