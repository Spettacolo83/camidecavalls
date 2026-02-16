package com.followmemobile.camidecavalls.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.followmemobile.camidecavalls.data.local.CamiDatabaseWrapper
import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

/**
 * Implementation of TrackingRepository using SQLDelight.
 */
class TrackingRepositoryImpl(
    private val database: CamiDatabaseWrapper
) : TrackingRepository {

    override fun getAllSessions(): Flow<List<TrackingSession>> {
        return database.trackingSessionQueries
            .selectAllSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    // Load track points for each session
                    val trackPoints = database.trackPointQueries
                        .selectTrackPointsBySession(entity.id)
                        .executeAsList()
                        .map { it.toTrackPoint() }

                    entity.toDomain(trackPoints)
                }
            }
    }

    override fun getSessionsByRoute(routeId: Int): Flow<List<TrackingSession>> {
        return database.trackingSessionQueries
            .selectSessionsByRoute(routeId.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    // Load track points for each session
                    val trackPoints = database.trackPointQueries
                        .selectTrackPointsBySession(entity.id)
                        .executeAsList()
                        .map { it.toTrackPoint() }

                    entity.toDomain(trackPoints)
                }
            }
    }

    override suspend fun getSessionById(id: String): TrackingSession? = withContext(Dispatchers.IO) {
        val entity = database.trackingSessionQueries
            .selectSessionById(id)
            .executeAsOneOrNull()
            ?: return@withContext null

        val trackPoints = database.trackPointQueries
            .selectTrackPointsBySession(entity.id)
            .executeAsList()
            .map { it.toTrackPoint() }

        entity.toDomain(trackPoints)
    }

    override suspend fun saveSession(session: TrackingSession) = withContext(Dispatchers.IO) {
        database.trackingSessionQueries.transaction {
            // Insert session
            database.trackingSessionQueries.insertSession(
                id = session.id,
                routeId = session.routeId?.toLong(),
                startTime = session.startTime.toEpochMilliseconds(),
                endTime = session.endTime?.toEpochMilliseconds(),
                distanceMeters = session.distanceMeters,
                durationSeconds = session.durationSeconds,
                averageSpeedKmh = session.averageSpeedKmh,
                maxSpeedKmh = session.maxSpeedKmh,
                elevationGainMeters = session.elevationGainMeters.toLong(),
                elevationLossMeters = session.elevationLossMeters.toLong(),
                isCompleted = if (session.isCompleted) 1 else 0,
                name = session.name,
                notes = session.notes
            )

            // Insert track points
            session.trackPoints.forEach { point ->
                database.trackPointQueries.insertTrackPoint(
                    sessionId = session.id,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    altitude = point.altitude,
                    timestamp = point.timestamp.toEpochMilliseconds(),
                    speedKmh = point.speedKmh
                )
            }
        }
    }

    override suspend fun updateSession(session: TrackingSession) = withContext(Dispatchers.IO) {
        // Delete existing track points and re-insert
        database.trackingSessionQueries.transaction {
            database.trackPointQueries.deleteTrackPointsBySession(session.id)

            // Re-insert session
            database.trackingSessionQueries.insertSession(
                id = session.id,
                routeId = session.routeId?.toLong(),
                startTime = session.startTime.toEpochMilliseconds(),
                endTime = session.endTime?.toEpochMilliseconds(),
                distanceMeters = session.distanceMeters,
                durationSeconds = session.durationSeconds,
                averageSpeedKmh = session.averageSpeedKmh,
                maxSpeedKmh = session.maxSpeedKmh,
                elevationGainMeters = session.elevationGainMeters.toLong(),
                elevationLossMeters = session.elevationLossMeters.toLong(),
                isCompleted = if (session.isCompleted) 1 else 0,
                name = session.name,
                notes = session.notes
            )

            // Re-insert track points
            session.trackPoints.forEach { point ->
                database.trackPointQueries.insertTrackPoint(
                    sessionId = session.id,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    altitude = point.altitude,
                    timestamp = point.timestamp.toEpochMilliseconds(),
                    speedKmh = point.speedKmh
                )
            }
        }
    }

    override suspend fun deleteSession(id: String) = withContext(Dispatchers.IO) {
        database.trackingSessionQueries.deleteSession(id)
    }

    override suspend fun insertTrackPoint(sessionId: String, trackPoint: TrackPoint) = withContext(Dispatchers.IO) {
        database.trackPointQueries.insertTrackPoint(
            sessionId = sessionId,
            latitude = trackPoint.latitude,
            longitude = trackPoint.longitude,
            altitude = trackPoint.altitude,
            timestamp = trackPoint.timestamp.toEpochMilliseconds(),
            speedKmh = trackPoint.speedKmh
        )
    }

    override fun getActiveSession(): Flow<TrackingSession?> {
        return database.trackingSessionQueries
            .selectActiveSession()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let {
                    // Load track points for the session
                    val trackPoints = database.trackPointQueries
                        .selectTrackPointsBySession(it.id)
                        .executeAsList()
                        .map { point -> point.toTrackPoint() }

                    it.toDomain(trackPoints)
                }
            }
    }

    // Extension function to map database entity to domain model
    private fun com.followmemobile.camidecavalls.database.TrackingSessionEntity.toDomain(
        trackPoints: List<TrackPoint>
    ): TrackingSession {
        return TrackingSession(
            id = id,
            routeId = routeId?.toInt(),
            startTime = Instant.fromEpochMilliseconds(startTime),
            endTime = endTime?.let { Instant.fromEpochMilliseconds(it) },
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            averageSpeedKmh = averageSpeedKmh,
            maxSpeedKmh = maxSpeedKmh,
            elevationGainMeters = elevationGainMeters.toInt(),
            elevationLossMeters = elevationLossMeters.toInt(),
            trackPoints = trackPoints,
            isCompleted = isCompleted == 1L,
            name = name,
            notes = notes
        )
    }

    private fun com.followmemobile.camidecavalls.database.TrackPointEntity.toTrackPoint(): TrackPoint {
        return TrackPoint(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            timestamp = Instant.fromEpochMilliseconds(timestamp),
            speedKmh = speedKmh
        )
    }
}
