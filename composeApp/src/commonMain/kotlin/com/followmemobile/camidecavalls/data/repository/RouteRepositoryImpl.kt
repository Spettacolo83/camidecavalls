package com.followmemobile.camidecavalls.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.followmemobile.camidecavalls.data.local.CamiDatabaseWrapper
import com.followmemobile.camidecavalls.domain.model.Difficulty
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementation of RouteRepository using SQLDelight.
 */
class RouteRepositoryImpl(
    private val database: CamiDatabaseWrapper
) : RouteRepository {

    override fun getAllRoutes(): Flow<List<Route>> {
        return database.routeQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun getRouteById(id: Int): Route? = withContext(Dispatchers.IO) {
        database.routeQueries
            .selectById(id.toLong())
            .executeAsOneOrNull()
            ?.toDomain()
    }

    override suspend fun getRouteByNumber(number: Int): Route? = withContext(Dispatchers.IO) {
        database.routeQueries
            .selectByNumber(number.toLong())
            .executeAsOneOrNull()
            ?.toDomain()
    }

    override suspend fun saveRoutes(routes: List<Route>) = withContext(Dispatchers.IO) {
        database.routeQueries.transaction {
            routes.forEach { route ->
                database.routeQueries.insert(
                    id = route.id.toLong(),
                    number = route.number.toLong(),
                    name = route.name,
                    startPoint = route.startPoint,
                    endPoint = route.endPoint,
                    distanceKm = route.distanceKm,
                    elevationGainMeters = route.elevationGainMeters.toLong(),
                    elevationLossMeters = route.elevationLossMeters.toLong(),
                    maxAltitudeMeters = route.maxAltitudeMeters.toLong(),
                    minAltitudeMeters = route.minAltitudeMeters.toLong(),
                    asphaltPercentage = route.asphaltPercentage.toLong(),
                    difficulty = route.difficulty.name,
                    estimatedDurationMinutes = route.estimatedDurationMinutes.toLong(),
                    description = route.description,
                    descriptionCa = route.descriptionCa,
                    descriptionEs = route.descriptionEs,
                    descriptionEn = route.descriptionEn,
                    descriptionDe = route.descriptionDe,
                    descriptionFr = route.descriptionFr,
                    descriptionIt = route.descriptionIt,
                    gpxData = route.gpxData,
                    imageUrl = route.imageUrl
                )
            }
        }
    }

    override suspend fun getRouteGpxData(routeId: Int): String? = withContext(Dispatchers.IO) {
        database.routeQueries
            .selectById(routeId.toLong())
            .executeAsOneOrNull()
            ?.gpxData
    }

    override suspend fun recreateRouteTable(): Unit = withContext(Dispatchers.IO) {
        val driver = database.driver
        driver.execute(
            null,
            "DROP TABLE IF EXISTS RouteEntity",
            0
        )

        driver.execute(
            null,
            """
            CREATE TABLE RouteEntity (
                id INTEGER PRIMARY KEY NOT NULL,
                number INTEGER NOT NULL UNIQUE,
                name TEXT NOT NULL,
                startPoint TEXT NOT NULL,
                endPoint TEXT NOT NULL,
                distanceKm REAL NOT NULL,
                elevationGainMeters INTEGER NOT NULL,
                elevationLossMeters INTEGER NOT NULL,
                maxAltitudeMeters INTEGER NOT NULL,
                minAltitudeMeters INTEGER NOT NULL,
                asphaltPercentage INTEGER NOT NULL,
                difficulty TEXT NOT NULL,
                estimatedDurationMinutes INTEGER NOT NULL,
                description TEXT NOT NULL,
                descriptionCa TEXT,
                descriptionEs TEXT,
                descriptionEn TEXT,
                descriptionDe TEXT,
                descriptionFr TEXT,
                descriptionIt TEXT,
                gpxData TEXT,
                imageUrl TEXT
            )
            """.trimIndent(),
            0
        )
    }

    // Extension function to map database entity to domain model
    private fun com.followmemobile.camidecavalls.database.RouteEntity.toDomain(): Route {
        return Route(
            id = id.toInt(),
            number = number.toInt(),
            name = name,
            startPoint = startPoint,
            endPoint = endPoint,
            distanceKm = distanceKm,
            elevationGainMeters = elevationGainMeters.toInt(),
            elevationLossMeters = elevationLossMeters.toInt(),
            maxAltitudeMeters = maxAltitudeMeters.toInt(),
            minAltitudeMeters = minAltitudeMeters.toInt(),
            asphaltPercentage = asphaltPercentage.toInt(),
            difficulty = Difficulty.valueOf(difficulty),
            estimatedDurationMinutes = estimatedDurationMinutes.toInt(),
            description = description,
            descriptionCa = descriptionCa,
            descriptionEs = descriptionEs,
            descriptionEn = descriptionEn,
            descriptionDe = descriptionDe,
            descriptionFr = descriptionFr,
            descriptionIt = descriptionIt,
            gpxData = gpxData,
            imageUrl = imageUrl
        )
    }
}
