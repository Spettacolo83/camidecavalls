package com.followmemobile.camidecavalls.domain.usecase.poi

import camidecavalls.composeapp.generated.resources.Res
import com.followmemobile.camidecavalls.data.local.AppPreferences
import com.followmemobile.camidecavalls.domain.model.POIType
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.POIRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Use case to initialize POIs from JSON file on first app launch
 */
class InitializePOIsUseCase(
    private val poiRepository: POIRepository,
    private val savePOIsUseCase: SavePOIsUseCase,
    private val appPreferences: AppPreferences
) {
    @OptIn(ExperimentalResourceApi::class)
    suspend operator fun invoke(): Boolean {
        val currentPoiVersion = appPreferences.getPOIVersion()

        // Check if we need to initialize or update POI data
        if (currentPoiVersion < POI_VERSION) {
            // Load POI data from JSON file
            val jsonBytes = Res.readBytes("files/pois.json")
            val jsonString = jsonBytes.decodeToString()
            val poiData = parsePOIJson(jsonString)

            // Save to database (will replace existing data)
            savePOIsUseCase(poiData)
            appPreferences.setPOIVersion(POI_VERSION)
            return true
        }

        // Check if data exists
        val existingPOIs = poiRepository.getAllPOIs().first()
        if (existingPOIs.isNotEmpty()) {
            return false // All good
        }

        // POI version is correct but no data - re-seed
        val jsonBytes = Res.readBytes("files/pois.json")
        val jsonString = jsonBytes.decodeToString()
        val poiData = parsePOIJson(jsonString)
        savePOIsUseCase(poiData)
        return true
    }

    private fun parsePOIJson(jsonString: String): List<PointOfInterest> {
        val json = Json { ignoreUnknownKeys = true }
        val poiJsonList = json.decodeFromString<List<POIJson>>(jsonString)

        return poiJsonList.map { poiJson ->
            PointOfInterest(
                id = poiJson.id.toInt(),
                type = POIType.valueOf(poiJson.type),
                latitude = poiJson.latitude,
                longitude = poiJson.longitude,
                nameCa = poiJson.names.ca,
                nameEs = poiJson.names.es,
                nameEn = poiJson.names.en,
                nameDe = poiJson.names.de,
                nameFr = poiJson.names.fr,
                nameIt = poiJson.names.it,
                descriptionCa = poiJson.descriptions.ca,
                descriptionEs = poiJson.descriptions.es,
                descriptionEn = poiJson.descriptions.en,
                descriptionDe = poiJson.descriptions.de,
                descriptionFr = poiJson.descriptions.fr,
                descriptionIt = poiJson.descriptions.it,
                imageUrl = poiJson.image_url,
                routeId = null,
                isAdvertisement = false
            )
        }
    }

    companion object {
        private const val POI_VERSION = 3  // Updated: fixed coordinates from official map
    }
}

@Serializable
private data class POIJson(
    val id: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val image_url: String,
    val names: NamesJson,
    val descriptions: DescriptionsJson
)

@Serializable
private data class NamesJson(
    val ca: String = "",
    val es: String = "",
    val en: String = "",
    val de: String = "",
    val fr: String = "",
    val it: String = ""
)

@Serializable
private data class DescriptionsJson(
    val ca: String = "",
    val es: String = "",
    val en: String = "",
    val de: String = "",
    val fr: String = "",
    val it: String = ""
)
