package com.followmemobile.camidecavalls.domain.usecase.poi

import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.POIRepository

/**
 * Use case for finding Points of Interest near a specific location.
 * Used for proximity alerts and nearby POI discovery.
 *
 * @param latitude User's current latitude
 * @param longitude User's current longitude
 * @param radiusMeters Search radius in meters (default: 500m)
 */
class GetPOIsNearLocationUseCase(
    private val poiRepository: POIRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 500.0
    ): List<PointOfInterest> {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90 and 90, got $latitude"
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180 and 180, got $longitude"
        }
        require(radiusMeters > 0) {
            "Radius must be positive, got $radiusMeters"
        }

        return poiRepository.getPOIsNearLocation(latitude, longitude, radiusMeters)
    }
}
