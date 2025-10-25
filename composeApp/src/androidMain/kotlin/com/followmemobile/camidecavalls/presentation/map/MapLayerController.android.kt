package com.followmemobile.camidecavalls.presentation.map

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource

actual class MapLayerController {
    private var mapLibreMap: MapLibreMap? = null
    private var style: Style? = null

    internal fun setMap(map: MapLibreMap, loadedStyle: Style) {
        this.mapLibreMap = map
        this.style = loadedStyle
    }

    actual fun addRoutePath(
        routeId: String,
        geoJsonLineString: String,
        color: String,
        width: Float
    ) {
        val currentStyle = style ?: return

        // Remove existing layers/sources if they exist
        removeLayer("$routeId-casing")
        removeLayer(routeId)

        try {
            // Add GeoJSON source
            val source = GeoJsonSource("source-$routeId", geoJsonLineString)
            currentStyle.addSource(source)

            // Add white casing (outline)
            val casingLayer = LineLayer("$routeId-casing", "source-$routeId")
                .withProperties(
                    lineColor(Color.WHITE),
                    lineWidth(width + 2f),
                    lineCap("round"),
                    lineJoin("round")
                )
            currentStyle.addLayer(casingLayer)

            // Add colored route line
            val lineLayer = LineLayer(routeId, "source-$routeId")
                .withProperties(
                    lineColor(Color.parseColor(color)),
                    lineWidth(width),
                    lineCap("round"),
                    lineJoin("round")
                )
            currentStyle.addLayer(lineLayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun addMarker(
        markerId: String,
        latitude: Double,
        longitude: Double,
        color: String,
        radius: Float
    ) {
        val currentStyle = style ?: return

        removeLayer(markerId)
        removeLayer("$markerId-outer")

        try {
            // Create point GeoJSON
            val geoJson = """{"type":"Point","coordinates":[$longitude,$latitude]}"""
            val source = GeoJsonSource("source-$markerId", geoJson)
            currentStyle.addSource(source)

            // Add outer white circle
            val outerCircle = CircleLayer("$markerId-outer", "source-$markerId")
                .withProperties(
                    circleColor(Color.WHITE),
                    circleRadius(radius + 2f)
                )
            currentStyle.addLayer(outerCircle)

            // Add inner colored circle
            val innerCircle = CircleLayer(markerId, "source-$markerId")
                .withProperties(
                    circleColor(Color.parseColor(color)),
                    circleRadius(radius)
                )
            currentStyle.addLayer(innerCircle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun removeLayer(layerId: String) {
        val currentStyle = style ?: return
        try {
            currentStyle.getLayer(layerId)?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.getSource("source-$layerId")?.let {
                currentStyle.removeSource(it)
            }
        } catch (e: Exception) {
            // Layer might not exist
        }
    }

    actual fun clearAll() {
        // Layers will be cleared when map is disposed
    }
}

@Composable
actual fun MapWithLayers(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Double,
    styleUrl: String,
    onMapReady: (MapLayerController) -> Unit
) {
    val context = LocalContext.current
    val controller = remember { MapLayerController() }

    // Initialize MapLibre with custom tile server (no API key needed)
    DisposableEffect(Unit) {
        try {
            MapLibre.getInstance(context, null, null)
        } catch (e: Exception) {
            // Already initialized, ignore
        }
        onDispose { }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                getMapAsync { map ->
                    map.setStyle(styleUrl) { style ->
                        // Set camera position
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(latitude, longitude))
                            .zoom(zoom)
                            .build()

                        // Initialize controller
                        controller.setMap(map, style)
                        onMapReady(controller)
                    }
                }
            }
        },
        update = { mapView ->
            mapView.getMapAsync { map ->
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(latitude, longitude))
                    .zoom(zoom)
                    .build()
            }
        }
    )
}
