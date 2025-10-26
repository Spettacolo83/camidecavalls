package com.followmemobile.camidecavalls.presentation.map

import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
            Log.d("MapLayer", "=== Adding Route Path ===")
            Log.d("MapLayer", "RouteId: $routeId")
            Log.d("MapLayer", "Style: $currentStyle")
            Log.d("MapLayer", "Color: $color, Width: $width")

            // Use the GPX data from the route parameter
            val cleanedGeometry = geoJsonLineString.trim()
            val featureGeoJson = """{"type":"Feature","geometry":$cleanedGeometry,"properties":{}}"""

            Log.d("MapLayer", "=== Adding Route Path ===")
            Log.d("MapLayer", "RouteId: $routeId")
            Log.d("MapLayer", "GeoJSON geometry length: ${cleanedGeometry.length} chars")

            // Try passing the Feature to GeoJsonSource
            val source = GeoJsonSource("source-$routeId", featureGeoJson)
            currentStyle.addSource(source)
            Log.d("MapLayer", "Source added successfully")

            // Add white casing (outline)
            val casingLayer = LineLayer("$routeId-casing", "source-$routeId")
                .withProperties(
                    lineColor(Color.WHITE),
                    lineWidth(width + 2f),
                    lineCap("round"),
                    lineJoin("round")
                )
            currentStyle.addLayer(casingLayer)
            Log.d("MapLayer", "Casing layer added")

            // Add colored route line
            val lineLayer = LineLayer(routeId, "source-$routeId")
                .withProperties(
                    lineColor(Color.parseColor(color)),
                    lineWidth(width),
                    lineCap("round"),
                    lineJoin("round")
                )
            currentStyle.addLayer(lineLayer)
            Log.d("MapLayer", "Line layer added - Route rendering complete!")
        } catch (e: Exception) {
            Log.e("MapLayer", "Error adding route path", e)
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
            // Create point GeoJSON directly
            val pointGeometry = """{"type":"Point","coordinates":[$longitude,$latitude]}"""

            Log.d("MapLayer", "=== Adding Marker ===")
            Log.d("MapLayer", "MarkerId: $markerId, Lat: $latitude, Lon: $longitude")
            Log.d("MapLayer", "Color: $color, Radius: $radius")

            val source = GeoJsonSource("source-$markerId", pointGeometry)
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
    val controller = remember { MapLayerController() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Initialize MapLibre before creating MapView
            try {
                MapLibre.getInstance(ctx)
            } catch (e: Exception) {
                // Already initialized, ignore
            }

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
