package com.followmemobile.camidecavalls.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class RemotePoiResponse(
    val pois: List<RemotePoiDto>,
    val count: Int,
    val timestamp: String
)

@Serializable
data class RemotePoiDto(
    val id: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val routeId: Int? = null,
    val isAdvertisement: Boolean = false,
    val source: String = "DYNAMIC",
    val hardcodedPoiId: Int? = null,
    val priority: Int = 0,
    val updatedAt: String,
    val translations: Map<String, TranslationDto> = emptyMap()
)

@Serializable
data class TranslationDto(
    val name: String = "",
    val description: String = "",
    val actionButtonText: String? = null
)

@Serializable
data class SyncStatusResponse(
    val lastUpdated: String?,
    val activeCount: Int,
    val inactiveCount: Int,
    val serverTime: String
)

class PoiApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    /** Exposed for image URL resolution */
    fun getBaseUrl(): String = baseUrl

    suspend fun getSyncStatus(): SyncStatusResponse {
        return httpClient.get("$baseUrl/api/v1/sync-status").body()
    }

    suspend fun getPois(since: String? = null): RemotePoiResponse {
        return httpClient.get("$baseUrl/api/v1/pois") {
            if (since != null) {
                parameter("since", since)
            }
        }.body()
    }
}
