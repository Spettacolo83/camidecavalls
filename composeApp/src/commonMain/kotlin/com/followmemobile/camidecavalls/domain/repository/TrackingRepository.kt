package com.followmemobile.camidecavalls.domain.repository

import com.followmemobile.camidecavalls.domain.model.TrackingSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for tracking session operations (Notebook).
 * All data stored locally.
 */
interface TrackingRepository {

    /**
     * Get all tracking sessions (Notebook history)
     */
    fun getAllSessions(): Flow<List<TrackingSession>>

    /**
     * Get sessions for a specific route
     */
    fun getSessionsByRoute(routeId: Int): Flow<List<TrackingSession>>

    /**
     * Get a specific session by ID
     */
    suspend fun getSessionById(id: String): TrackingSession?

    /**
     * Save a new tracking session
     */
    suspend fun saveSession(session: TrackingSession)

    /**
     * Update an existing session
     */
    suspend fun updateSession(session: TrackingSession)

    /**
     * Delete a session
     */
    suspend fun deleteSession(id: String)

    /**
     * Get currently active session (if any)
     */
    fun getActiveSession(): Flow<TrackingSession?>
}
