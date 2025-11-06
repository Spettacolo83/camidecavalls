package com.followmemobile.camidecavalls.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.followmemobile.camidecavalls.data.local.CamiDatabaseWrapper
import com.followmemobile.camidecavalls.domain.model.POIType
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.POIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Implementation of POIRepository using SQLDelight.
 */
class POIRepositoryImpl(
    private val database: CamiDatabaseWrapper
) : POIRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllPOIs(): Flow<List<PointOfInterest>> {
        return database.poiQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun getPOIsByType(type: POIType): Flow<List<PointOfInterest>> {
        return database.poiQueries
            .selectByType(type.name)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun getPOIsNearLocation(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): List<PointOfInterest> = withContext(Dispatchers.IO) {
        // Calculate bounding box
        val latDelta = radiusMeters / 111000.0 // ~111km per degree latitude
        val lonDelta = radiusMeters / (111000.0 * cos(latitude * PI / 180.0))

        val minLat = latitude - latDelta
        val maxLat = latitude + latDelta
        val minLon = longitude - lonDelta
        val maxLon = longitude + lonDelta

        database.poiQueries
            .selectNearLocation(minLat, maxLat, minLon, maxLon)
            .executeAsList()
            .map { it.toDomain() }
            .filter { poi ->
                // Filter by actual distance using Haversine formula
                calculateDistance(latitude, longitude, poi.latitude, poi.longitude) <= radiusMeters
            }
    }

    override fun getPOIsByRoute(routeId: Int): Flow<List<PointOfInterest>> {
        return database.poiQueries
            .selectByRoute(routeId.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun savePOIs(pois: List<PointOfInterest>) = withContext(Dispatchers.IO) {
        database.poiQueries.transaction {
            pois.forEach { poi ->
                database.poiQueries.insert(
                    id = poi.id.toLong(),
                    type = poi.type.name,
                    latitude = poi.latitude,
                    longitude = poi.longitude,
                    // Multilingual names
                    nameCa = poi.nameCa,
                    nameEs = poi.nameEs,
                    nameEn = poi.nameEn,
                    nameFr = poi.nameFr,
                    nameDe = poi.nameDe,
                    nameIt = poi.nameIt,
                    // Multilingual descriptions
                    descriptionCa = poi.descriptionCa,
                    descriptionEs = poi.descriptionEs,
                    descriptionEn = poi.descriptionEn,
                    descriptionFr = poi.descriptionFr,
                    descriptionDe = poi.descriptionDe,
                    descriptionIt = poi.descriptionIt,
                    // Image and metadata
                    imageUrl = poi.imageUrl,
                    routeId = poi.routeId?.toLong(),
                    isAdvertisement = if (poi.isAdvertisement) 1 else 0
                )
            }
        }
    }

    // Extension function to map database entity to domain model
    private fun com.followmemobile.camidecavalls.database.PointOfInterestEntity.toDomain(): PointOfInterest {
        return PointOfInterest(
            id = id.toInt(),
            type = POIType.valueOf(type),
            latitude = latitude,
            longitude = longitude,
            // Multilingual names
            nameCa = nameCa,
            nameEs = nameEs,
            nameEn = nameEn,
            nameFr = nameFr,
            nameDe = nameDe,
            nameIt = nameIt,
            // Multilingual descriptions
            descriptionCa = descriptionCa,
            descriptionEs = descriptionEs,
            descriptionEn = descriptionEn,
            descriptionFr = descriptionFr,
            descriptionDe = descriptionDe,
            descriptionIt = descriptionIt,
            // Image and metadata
            imageUrl = imageUrl,
            routeId = routeId?.toInt(),
            isAdvertisement = isAdvertisement == 1L
        )
    }

    // Calculate distance between two points using Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
