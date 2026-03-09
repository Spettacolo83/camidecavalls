package com.followmemobile.camidecavalls.data.repository

import com.followmemobile.camidecavalls.data.local.CamiDatabaseWrapper
import com.followmemobile.camidecavalls.data.remote.RemotePoiDto
import com.followmemobile.camidecavalls.domain.model.POIType
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class RemotePOIRepositoryImpl(
    private val database: CamiDatabaseWrapper
) {
    suspend fun getAllRemotePois(): List<PointOfInterest> = withContext(Dispatchers.IO) {
        database.remotePoiQueries.selectAll().executeAsList().map { it.toDomain() }
    }

    suspend fun getOverrides(): Map<Int, PointOfInterest> = withContext(Dispatchers.IO) {
        database.remotePoiQueries.selectOverrides().executeAsList()
            .filter { it.hardcodedPoiId != null }
            .associate { it.hardcodedPoiId!!.toInt() to it.toDomain() }
    }

    suspend fun saveRemotePois(dtos: List<RemotePoiDto>, baseUrl: String) = withContext(Dispatchers.IO) {
        database.remotePoiQueries.transaction {
            for (dto in dtos) {
                val nameCa = dto.translations["ca"]?.name ?: ""
                val nameEs = dto.translations["es"]?.name ?: ""
                val nameEn = dto.translations["en"]?.name ?: ""
                val nameFr = dto.translations["fr"]?.name ?: ""
                val nameDe = dto.translations["de"]?.name ?: ""
                val nameIt = dto.translations["it"]?.name ?: ""
                val descCa = dto.translations["ca"]?.description ?: ""
                val descEs = dto.translations["es"]?.description ?: ""
                val descEn = dto.translations["en"]?.description ?: ""
                val descFr = dto.translations["fr"]?.description ?: ""
                val descDe = dto.translations["de"]?.description ?: ""
                val descIt = dto.translations["it"]?.description ?: ""
                val btnCa = dto.translations["ca"]?.actionButtonText ?: ""
                val btnEs = dto.translations["es"]?.actionButtonText ?: ""
                val btnEn = dto.translations["en"]?.actionButtonText ?: ""
                val btnFr = dto.translations["fr"]?.actionButtonText ?: ""
                val btnDe = dto.translations["de"]?.actionButtonText ?: ""
                val btnIt = dto.translations["it"]?.actionButtonText ?: ""

                // Resolve relative image URLs to absolute
                val resolvedImageUrl = resolveUrl(dto.imageUrl, baseUrl)

                database.remotePoiQueries.insert(
                    id = dto.id,
                    type = dto.type,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    nameCa = nameCa,
                    nameEs = nameEs,
                    nameEn = nameEn,
                    nameFr = nameFr,
                    nameDe = nameDe,
                    nameIt = nameIt,
                    descriptionCa = descCa,
                    descriptionEs = descEs,
                    descriptionEn = descEn,
                    descriptionFr = descFr,
                    descriptionDe = descDe,
                    descriptionIt = descIt,
                    imageUrl = resolvedImageUrl,
                    actionUrl = dto.actionUrl ?: "",
                    actionButtonTextCa = btnCa,
                    actionButtonTextEs = btnEs,
                    actionButtonTextEn = btnEn,
                    actionButtonTextFr = btnFr,
                    actionButtonTextDe = btnDe,
                    actionButtonTextIt = btnIt,
                    routeId = dto.routeId?.toLong(),
                    isAdvertisement = if (dto.isAdvertisement) 1 else 0,
                    source = dto.source,
                    hardcodedPoiId = dto.hardcodedPoiId?.toLong(),
                    priority = dto.priority.toLong(),
                    updatedAt = dto.updatedAt
                )
            }
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        database.remotePoiQueries.deleteAll()
    }

    suspend fun count(): Long = withContext(Dispatchers.IO) {
        database.remotePoiQueries.count().executeAsOne()
    }

    private fun resolveUrl(url: String?, baseUrl: String): String {
        if (url.isNullOrBlank()) return ""
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        // Relative path like /uploads/xxx.png → prepend base URL
        return "$baseUrl$url"
    }

    private fun com.followmemobile.camidecavalls.database.RemotePoiEntity.toDomain(): PointOfInterest {
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
}
