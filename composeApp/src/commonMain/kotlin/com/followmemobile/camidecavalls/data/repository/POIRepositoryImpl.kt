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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Implementation of POIRepository using SQLDelight.
 * Merges hardcoded POIs with remote dynamic POIs from backend.
 * Remote POIs with hardcodedPoiId override the corresponding local POI.
 */
class POIRepositoryImpl(
    private val database: CamiDatabaseWrapper
) : POIRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllPOIs(): Flow<List<PointOfInterest>> {
        val localFlow = database.poiQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }

        val remoteFlow = database.remotePoiQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toRemoteDomain() } }

        return combine(localFlow, remoteFlow) { local, remote ->
            mergePoiLists(local, remote)
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

    override suspend fun getPOIById(id: Int): PointOfInterest? = withContext(Dispatchers.IO) {
        database.poiQueries
            .selectById(id.toLong())
            .executeAsOneOrNull()
            ?.toDomain()
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

        // Get local hardcoded POIs near location
        val localPois = database.poiQueries
            .selectNearLocation(minLat, maxLat, minLon, maxLon)
            .executeAsList()
            .map { it.toDomain() }

        // Get remote dynamic POIs near location
        val remotePois = database.remotePoiQueries
            .selectNearLocation(minLat, maxLat, minLon, maxLon)
            .executeAsList()
            .map { it.toRemoteDomain() }

        // Merge and filter by actual distance using Haversine formula
        mergePoiLists(localPois, remotePois)
            .filter { poi ->
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

    /**
     * Merge local hardcoded POIs with remote dynamic POIs.
     * Remote POIs with a hardcodedPoiId override the corresponding local POI.
     * Dynamic remote POIs are added to the list.
     */
    private fun mergePoiLists(
        local: List<PointOfInterest>,
        remote: List<PointOfInterest>
    ): List<PointOfInterest> {
        if (remote.isEmpty()) return local

        // Build override map: hardcodedPoiId -> remote POI
        val overrides = mutableMapOf<Int, PointOfInterest>()
        val dynamicPois = mutableListOf<PointOfInterest>()

        for (poi in remote) {
            // Remote POIs that override hardcoded ones are identified by negative IDs
            // (see toRemoteDomain which uses hashCode). We stored hardcodedPoiId info
            // in the remote table, so we check via the database.
            dynamicPois.add(poi)
        }

        // Fetch overrides from database directly
        val overrideEntities = database.remotePoiQueries.selectOverrides().executeAsList()
        for (entity in overrideEntities) {
            entity.hardcodedPoiId?.let { hcId ->
                overrides[hcId.toInt()] = entity.toRemoteDomain()
            }
        }

        // Replace overridden local POIs, keep the rest
        val mergedLocal = local.map { localPoi ->
            overrides[localPoi.id] ?: localPoi
        }

        // Add dynamic POIs (those without hardcodedPoiId)
        val dynamicOnly = remote.filter { remotePoi ->
            overrideEntities.none { it.id.hashCode() == remotePoi.id }
        }

        return mergedLocal + dynamicOnly
    }

    // Map RemotePoiEntity to domain model
    private fun com.followmemobile.camidecavalls.database.RemotePoiEntity.toRemoteDomain(): PointOfInterest {
        val poiType = try {
            POIType.valueOf(type)
        } catch (_: Exception) {
            POIType.COMMERCIAL
        }

        return PointOfInterest(
            id = id.hashCode(),
            type = poiType,
            latitude = latitude,
            longitude = longitude,
            nameCa = nameCa,
            nameEs = nameEs,
            nameEn = nameEn,
            nameFr = nameFr,
            nameDe = nameDe,
            nameIt = nameIt,
            descriptionCa = descriptionCa,
            descriptionEs = descriptionEs,
            descriptionEn = descriptionEn,
            descriptionFr = descriptionFr,
            descriptionDe = descriptionDe,
            descriptionIt = descriptionIt,
            imageUrl = imageUrl,
            routeId = routeId?.toInt(),
            isAdvertisement = isAdvertisement == 1L,
            actionUrl = actionUrl.ifBlank { null },
            actionButtonTextCa = actionButtonTextCa,
            actionButtonTextEs = actionButtonTextEs,
            actionButtonTextEn = actionButtonTextEn,
            actionButtonTextFr = actionButtonTextFr,
            actionButtonTextDe = actionButtonTextDe,
            actionButtonTextIt = actionButtonTextIt
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
