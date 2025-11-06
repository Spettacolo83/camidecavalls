package com.followmemobile.camidecavalls.domain.usecase

import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.RouteRepository
import com.followmemobile.camidecavalls.domain.util.RouteSimplifier
import com.followmemobile.camidecavalls.domain.util.SimplificationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for getting simplified routes optimized for full map display.
 *
 * This use case applies the Douglas-Peucker algorithm to reduce the number of GPS points
 * in routes while maintaining their visual shape. This is essential for performance when
 * displaying all 20 routes simultaneously on the full map screen.
 *
 * Benefits:
 * - Reduces rendering load (~82.7% fewer points with default tolerance)
 * - Maintains visual accuracy (≈11m precision)
 * - Improves map interaction performance (pan, zoom, etc.)
 *
 * Usage:
 * ```kotlin
 * val simplifiedRoutes = getSimplifiedRoutesUseCase(
 *     tolerance = GetSimplifiedRoutesUseCase.TOLERANCE_FULL_MAP
 * )
 * ```
 */
class GetSimplifiedRoutesUseCase(
    private val routeRepository: RouteRepository
) {

    companion object {
        /**
         * Recommended tolerance for full map view (all 20 routes).
         * Value: 0.0001 degrees ≈ 11 meters
         * Achieves ~82.7% point reduction while maintaining visual accuracy.
         */
        const val TOLERANCE_FULL_MAP = 0.0001

        /**
         * Recommended tolerance for individual route view.
         * Value: 0.00005 degrees ≈ 5.5 meters
         * Higher detail for single route focus.
         */
        const val TOLERANCE_SINGLE_ROUTE = 0.00005

        /**
         * Recommended tolerance for performance-critical situations.
         * Value: 0.0002 degrees ≈ 22 meters
         * Maximum simplification for low-end devices.
         */
        const val TOLERANCE_PERFORMANCE = 0.0002
    }

    /**
     * Get all routes with simplified GPS data.
     *
     * @param tolerance Simplification tolerance (see constants for recommended values)
     * @return Flow of simplified routes
     */
    operator fun invoke(tolerance: Double = TOLERANCE_FULL_MAP): Flow<SimplificationResult> {
        return routeRepository.getAllRoutes().map { routes ->
            val result = RouteSimplifier.simplifyRoutes(routes, tolerance)

            // Log simplification stats for debugging
            println("📍 Route Simplification: ${result.stats}")

            result
        }
    }

    /**
     * Get a single simplified route.
     *
     * @param routeId Route ID
     * @param tolerance Simplification tolerance
     * @return Simplified route, or null if not found
     */
    suspend fun getSimplifiedRoute(
        routeId: Int,
        tolerance: Double = TOLERANCE_SINGLE_ROUTE
    ): Route? {
        val route = routeRepository.getRouteById(routeId)
        return route?.let { r ->
            if (r.gpxData != null) {
                val simplified = RouteSimplifier.simplifyGeoJson(r.gpxData, tolerance)
                r.copy(gpxData = simplified)
            } else {
                r
            }
        }
    }
}
