package com.followmemobile.camidecavalls.domain.usecase.poi

import com.followmemobile.camidecavalls.data.remote.PoiApiService
import com.followmemobile.camidecavalls.data.repository.RemotePOIRepositoryImpl
import com.followmemobile.camidecavalls.util.DebugConfig
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class SyncRemotePOIsUseCase(
    private val poiApiService: PoiApiService,
    private val remotePOIRepository: RemotePOIRepositoryImpl,
    private val settings: Settings
) {
    suspend operator fun invoke(force: Boolean = false): SyncResult {
        // Debug: clear sync cache to force full re-download
        if (DebugConfig.CLEAR_SYNC_CACHE) {
            Napier.d("DEBUG: Clearing POI sync cache")
            settings.remove(KEY_LAST_SYNC_TIMESTAMP)
            settings.remove(KEY_LAST_SYNC_CHECK)
        }

        // Check if sync is needed (once per day unless forced or debug flag)
        val shouldForce = force || DebugConfig.FORCE_POI_SYNC
        if (!shouldForce && !isSyncNeeded()) {
            Napier.d("POI sync not needed yet")
            return SyncResult.NotNeeded
        }

        if (shouldForce) {
            Napier.d("POI sync forced (force=$force, debugFlag=${DebugConfig.FORCE_POI_SYNC})")
        }

        return try {
            // Check server for updates
            val lastSyncTimestamp = settings.getStringOrNull(KEY_LAST_SYNC_TIMESTAMP)
            val syncStatus = poiApiService.getSyncStatus()

            Napier.d("POI sync status: server lastUpdated=${syncStatus.lastUpdated}, local lastSync=$lastSyncTimestamp")

            // If server has no data, nothing to sync
            if (syncStatus.lastUpdated == null) {
                updateLastSyncCheck()
                return SyncResult.NoUpdates
            }

            // If we have a last sync and server hasn't changed, skip
            if (!shouldForce && lastSyncTimestamp != null && syncStatus.lastUpdated == lastSyncTimestamp) {
                updateLastSyncCheck()
                return SyncResult.NoUpdates
            }

            // Fetch POIs (incremental if we have a previous sync, full otherwise)
            val sinceParam = if (shouldForce) null else lastSyncTimestamp
            val response = poiApiService.getPois(since = sinceParam)

            Napier.d("POI sync: fetched ${response.pois.size} POIs from server")

            if (response.pois.isEmpty()) {
                updateLastSyncCheck()
                return SyncResult.NoUpdates
            }

            // Save to local database (pass baseUrl to resolve relative image URLs)
            remotePOIRepository.saveRemotePois(response.pois, poiApiService.getBaseUrl())

            // Update sync timestamps
            settings.putString(KEY_LAST_SYNC_TIMESTAMP, syncStatus.lastUpdated)
            updateLastSyncCheck()

            Napier.d("POI sync completed: ${response.pois.size} POIs synced")
            SyncResult.Success(response.pois.size)
        } catch (e: Exception) {
            Napier.e("POI sync failed", e)
            // Don't update last sync check on error — allow retry on next app start
            SyncResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun isSyncNeeded(): Boolean {
        val lastCheckStr = settings.getStringOrNull(KEY_LAST_SYNC_CHECK) ?: return true
        return try {
            val lastCheck = Instant.parse(lastCheckStr)
            val now = Clock.System.now()
            (now - lastCheck).inWholeHours >= SYNC_INTERVAL_HOURS
        } catch (_: Exception) {
            true
        }
    }

    private fun updateLastSyncCheck() {
        settings.putString(KEY_LAST_SYNC_CHECK, Clock.System.now().toString())
    }

    companion object {
        private const val KEY_LAST_SYNC_TIMESTAMP = "poi_remote_last_sync_timestamp"
        private const val KEY_LAST_SYNC_CHECK = "poi_remote_last_sync_check"
        private const val SYNC_INTERVAL_HOURS = 24
    }
}

sealed class SyncResult {
    data object NotNeeded : SyncResult()
    data object NoUpdates : SyncResult()
    data class Success(val count: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
