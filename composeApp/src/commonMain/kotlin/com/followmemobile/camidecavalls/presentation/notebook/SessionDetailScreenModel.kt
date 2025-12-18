package com.followmemobile.camidecavalls.presentation.notebook

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.tracking.GetSessionByIdUseCase
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * ScreenModel for the session detail screen.
 * Shows recorded track on map with altitude-based gradient coloring.
 */
class SessionDetailScreenModel(
    private val sessionId: String,
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(SessionDetailUiState())
    val uiState: StateFlow<SessionDetailUiState> = _uiState.asStateFlow()

    private var mapController: MapLayerController? = null

    init {
        screenModelScope.launch {
            languageRepository.observeCurrentLanguage().collect { languageCode ->
                _uiState.update { it.copy(strings = LocalizedStrings(languageCode)) }
            }
        }
        loadSession()
    }

    private fun loadSession() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val session = getSessionByIdUseCase(sessionId)
                _uiState.update {
                    it.copy(
                        session = session,
                        isLoading = false
                    )
                }
                session?.let { drawTrackOnMap(it) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun onMapReady(controller: MapLayerController) {
        mapController = controller
        _uiState.value.session?.let { drawTrackOnMap(it) }
    }

    private fun drawTrackOnMap(session: TrackingSession) {
        val controller = mapController ?: return
        val points = session.trackPoints
        if (points.size < 2) return

        // Calculate altitude range for color interpolation
        val altitudes = points.mapNotNull { it.altitude }
        val hasAltitudeData = altitudes.isNotEmpty()

        val minAlt = altitudes.minOrNull() ?: 0.0
        val maxAlt = altitudes.maxOrNull() ?: 0.0
        val altRange = maxAlt - minAlt

        // Draw gradient-colored track segments
        // Use altitude-based gradient if we have altitude data with some range,
        // otherwise use position-based gradient (green at start, red at end)
        if (hasAltitudeData && altRange >= 1.0) {
            drawGradientTrack(controller, points, minAlt, altRange, useAltitude = true)
        } else {
            // Use position-based gradient (green to red based on track progress)
            drawGradientTrack(controller, points, 0.0, 1.0, useAltitude = false)
        }

        // Add start and end markers
        val startPoint = points.first()
        val endPoint = points.last()
        controller.addMarker(
            markerId = "session-start",
            latitude = startPoint.latitude,
            longitude = startPoint.longitude,
            color = "#4CAF50", // Green
            radius = 10f
        )
        controller.addMarker(
            markerId = "session-end",
            latitude = endPoint.latitude,
            longitude = endPoint.longitude,
            color = "#F44336", // Red
            radius = 10f
        )

        // Center on track
        centerOnTrack(controller, points)
    }

    private fun drawGradientTrack(
        controller: MapLayerController,
        points: List<TrackPoint>,
        minAlt: Double,
        altRange: Double,
        useAltitude: Boolean
    ) {
        // First, draw a single white casing line covering the entire track
        // This prevents white border overlap between segments
        val fullTrackGeoJson = createGeoJsonLineString(points)
        controller.addRoutePath(
            routeId = "session-track-casing",
            geoJsonLineString = fullTrackGeoJson,
            color = "#FFFFFF",
            width = 7f, // Width + 2 for casing effect
            withCasing = false // No nested casing
        )

        // Then draw colored segments without individual casings
        val numSegments = minOf(points.size - 1, 50)
        val pointsPerSegment = (points.size - 1) / numSegments

        for (i in 0 until numSegments) {
            val startIdx = i * pointsPerSegment
            val endIdx = if (i == numSegments - 1) points.size - 1 else (i + 1) * pointsPerSegment

            val segmentPoints = points.subList(startIdx, endIdx + 1)
            if (segmentPoints.size < 2) continue

            val normalizedValue: Double = if (useAltitude) {
                // Calculate average altitude for this segment
                val segmentAltitudes = segmentPoints.mapNotNull { it.altitude }
                val avgAlt = if (segmentAltitudes.isNotEmpty()) {
                    segmentAltitudes.average()
                } else {
                    minAlt
                }
                // Normalize altitude (green -> yellow -> red based on altitude)
                ((avgAlt - minAlt) / altRange).coerceIn(0.0, 1.0)
            } else {
                // Use position-based gradient (green at start -> yellow at middle -> red at end)
                (i.toDouble() / (numSegments - 1).coerceAtLeast(1)).coerceIn(0.0, 1.0)
            }

            val color = altitudeToColor(normalizedValue)

            val geoJson = createGeoJsonLineString(segmentPoints)
            controller.addRoutePath(
                routeId = "session-track-$i",
                geoJsonLineString = geoJson,
                color = color,
                width = 5f,
                withCasing = false // No individual casing, we have a single one
            )
        }
    }

    /**
     * Convert normalized altitude (0-1) to color (green -> yellow -> red)
     */
    private fun altitudeToColor(normalizedAlt: Double): String {
        // Green: #4CAF50 (76, 175, 80)
        // Yellow: #FFEB3B (255, 235, 59)
        // Red: #F44336 (244, 67, 54)

        return when {
            normalizedAlt < 0.5 -> {
                // Green to Yellow
                val t = normalizedAlt * 2
                val r = (76 + (255 - 76) * t).toInt()
                val g = (175 + (235 - 175) * t).toInt()
                val b = (80 + (59 - 80) * t).toInt()
                formatHexColor(r, g, b)
            }
            else -> {
                // Yellow to Red
                val t = (normalizedAlt - 0.5) * 2
                val r = (255 + (244 - 255) * t).toInt()
                val g = (235 + (67 - 235) * t).toInt()
                val b = (59 + (54 - 59) * t).toInt()
                formatHexColor(r, g, b)
            }
        }
    }

    private fun formatHexColor(r: Int, g: Int, b: Int): String {
        val rHex = r.toString(16).padStart(2, '0').uppercase()
        val gHex = g.toString(16).padStart(2, '0').uppercase()
        val bHex = b.toString(16).padStart(2, '0').uppercase()
        return "#$rHex$gHex$bHex"
    }

    private fun createGeoJsonLineString(points: List<TrackPoint>): String {
        val coordinates = JsonArray(
            points.map { point ->
                JsonArray(listOf(
                    JsonPrimitive(point.longitude),
                    JsonPrimitive(point.latitude)
                ))
            }
        )
        val geoJson = buildJsonObject {
            put("type", "LineString")
            put("coordinates", coordinates)
        }
        return geoJson.toString()
    }

    private fun centerOnTrack(controller: MapLayerController, points: List<TrackPoint>) {
        if (points.isEmpty()) return

        val lats = points.map { it.latitude }
        val lons = points.map { it.longitude }
        val minLat = lats.minOrNull()!!
        val maxLat = lats.maxOrNull()!!
        val minLon = lons.minOrNull()!!
        val maxLon = lons.maxOrNull()!!

        val centerLat = (minLat + maxLat) / 2.0
        val centerLon = (minLon + maxLon) / 2.0

        // Calculate zoom level to fit the entire track with padding
        val latSpan = maxLat - minLat
        val lonSpan = maxLon - minLon

        // Add 20% padding to ensure track doesn't touch edges
        val paddedLatSpan = latSpan * 1.2
        val paddedLonSpan = lonSpan * 1.2

        // Use the larger span to determine zoom
        // At zoom 0, the world is ~360 degrees wide
        // Each zoom level doubles the scale
        val maxSpan = maxOf(paddedLatSpan, paddedLonSpan)

        // Calculate optimal zoom based on geographic span
        // Formula: zoom ≈ log2(360 / span) - this gives roughly the right zoom level
        val zoom = when {
            maxSpan <= 0.0001 -> 18.0  // Very small area (< 10m)
            maxSpan <= 0.0005 -> 17.0  // ~50m
            maxSpan <= 0.001 -> 16.0   // ~100m
            maxSpan <= 0.002 -> 15.5   // ~200m
            maxSpan <= 0.005 -> 15.0   // ~500m
            maxSpan <= 0.01 -> 14.0    // ~1km
            maxSpan <= 0.02 -> 13.5    // ~2km
            maxSpan <= 0.05 -> 13.0    // ~5km
            maxSpan <= 0.1 -> 12.0     // ~10km
            maxSpan <= 0.2 -> 11.0     // ~20km
            maxSpan <= 0.5 -> 10.0     // ~50km
            else -> 9.0                // Larger areas
        }

        controller.updateCamera(centerLat, centerLon, zoom, animated = false)
    }

    fun toggleDetailsPanel() {
        _uiState.update { it.copy(isDetailsPanelExpanded = !it.isDetailsPanelExpanded) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Generate GPX content for export
     */
    fun generateGpxContent(): String? {
        val session = _uiState.value.session ?: return null
        if (session.trackPoints.isEmpty()) return null

        val gpxBuilder = StringBuilder()
        gpxBuilder.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        gpxBuilder.appendLine("""<gpx version="1.1" creator="Camí de Cavalls App">""")
        gpxBuilder.appendLine("""  <trk>""")
        gpxBuilder.appendLine("""    <name>${escapeXml(session.name.ifEmpty { "Tracked Route" })}</name>""")
        gpxBuilder.appendLine("""    <trkseg>""")

        for (point in session.trackPoints) {
            gpxBuilder.append("""      <trkpt lat="${point.latitude}" lon="${point.longitude}">""")
            point.altitude?.let { alt ->
                gpxBuilder.append("""<ele>$alt</ele>""")
            }
            gpxBuilder.appendLine("""</trkpt>""")
        }

        gpxBuilder.appendLine("""    </trkseg>""")
        gpxBuilder.appendLine("""  </trk>""")
        gpxBuilder.appendLine("""</gpx>""")

        return gpxBuilder.toString()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

/**
 * UI State for SessionDetailScreen
 */
data class SessionDetailUiState(
    val session: TrackingSession? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val strings: LocalizedStrings = LocalizedStrings("en"),
    val isDetailsPanelExpanded: Boolean = false
)
