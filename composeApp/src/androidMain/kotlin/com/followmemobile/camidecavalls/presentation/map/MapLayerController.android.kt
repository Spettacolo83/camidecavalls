package com.followmemobile.camidecavalls.presentation.map

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    private var onMarkerClick: ((String) -> Unit)? = null
    private val managedLayerIds = mutableSetOf<String>()
    internal var mapView: MapView? = null

    internal fun setMap(map: MapLibreMap, loadedStyle: Style) {
        this.mapLibreMap = map
        this.style = loadedStyle

        // Set up click listener for map
        map.addOnMapClickListener { latLng ->
            // Query for features at the clicked point
            val pixel = map.projection.toScreenLocation(latLng)
            val features = map.queryRenderedFeatures(pixel)

            Log.d("MapLayer", "Map clicked, found ${features.size} features at point")

            // Find if a marker was clicked by checking each feature's properties
            for (feature in features) {
                try {
                    // Check if this feature has our marker properties
                    val featureType = feature.getStringProperty("type")
                    val markerId = feature.getStringProperty("markerId")

                    Log.d("MapLayer", "Feature type: $featureType, markerId: $markerId")

                    if (featureType == "poi-marker" && markerId != null) {
                        Log.d("MapLayer", "✅ POI marker clicked: $markerId")
                        onMarkerClick?.invoke(markerId)
                        return@addOnMapClickListener true
                    }
                } catch (e: Exception) {
                    Log.e("MapLayer", "Error checking feature properties", e)
                }
            }

            Log.d("MapLayer", "No POI marker clicked")
            false
        }
    }

    actual fun setOnMarkerClickListener(onClick: (String) -> Unit) {
        onMarkerClick = onClick
    }

    actual fun updateCamera(
        latitude: Double,
        longitude: Double,
        zoom: Double?,
        animated: Boolean
    ) {
        val map = mapLibreMap ?: return
        val targetZoom = zoom ?: map.cameraPosition.zoom
        val position = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(targetZoom)
            .build()

        if (animated) {
            map.animateCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(position),
                500 // 500ms smooth animation
            )
        } else {
            map.cameraPosition = position
        }
    }

    actual fun getCurrentZoom(): Double {
        return mapLibreMap?.cameraPosition?.zoom ?: 14.0
    }

    actual fun addRoutePath(
        routeId: String,
        geoJsonLineString: String,
        color: String,
        width: Float
    ) {
        val currentStyle = style ?: return

        try {
            managedLayerIds += routeId
            Log.d("MapLayer", "=== Adding/Updating Route Path ===")
            Log.d("MapLayer", "RouteId: $routeId")
            Log.d("MapLayer", "Color: $color, Width: $width")

            // Use the GPX data from the route parameter
            val cleanedGeometry = geoJsonLineString.trim()
            val featureGeoJson = """{"type":"Feature","geometry":$cleanedGeometry,"properties":{}}"""

            Log.d("MapLayer", "GeoJSON geometry length: ${cleanedGeometry.length} chars")

            // Check if source already exists
            val existingSource = currentStyle.getSource("source-$routeId") as? GeoJsonSource

            if (existingSource != null) {
                // Update existing source with new geometry
                existingSource.setGeoJson(featureGeoJson)
                Log.d("MapLayer", "Updated existing route source")
            } else {
                // Create new source and layers
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
            }
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

        try {
            managedLayerIds += markerId
            // Create point GeoJSON with properties to identify the marker
            val featureGeoJson = """{
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [$longitude, $latitude]
                },
                "properties": {
                    "markerId": "$markerId",
                    "type": "poi-marker"
                }
            }"""

            Log.d("MapLayer", "=== Adding/Updating Marker ===")
            Log.d("MapLayer", "MarkerId: $markerId, Lat: $latitude, Lon: $longitude")
            Log.d("MapLayer", "Color: $color, Radius: $radius")

            // Check if source already exists
            val existingSource = currentStyle.getSource("source-$markerId") as? GeoJsonSource

            if (existingSource != null) {
                // Update existing source with new coordinates
                existingSource.setGeoJson(featureGeoJson)
                Log.d("MapLayer", "Updated existing marker source")
            } else {
                // Create new source and layers
                val source = GeoJsonSource("source-$markerId", featureGeoJson)
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
                Log.d("MapLayer", "Created new marker source and layers")
            }
        } catch (e: Exception) {
            Log.e("MapLayer", "Error adding/updating marker", e)
            e.printStackTrace()
        }
    }

    actual fun removeLayer(layerId: String) {
        val currentStyle = style ?: return
        try {
            // Remove all associated layers for this ID
            currentStyle.getLayer(layerId)?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.getLayer("$layerId-outer")?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.getLayer("$layerId-casing")?.let {
                currentStyle.removeLayer(it)
            }
            // Remove source last
            currentStyle.getSource("source-$layerId")?.let {
                currentStyle.removeSource(it)
            }
            managedLayerIds.remove(layerId)
        } catch (e: Exception) {
            // Layer might not exist, ignore
        }
    }

    actual fun clearAll() {
        val ids = managedLayerIds.toList()
        ids.forEach { id ->
            removeLayer(id)
        }
        managedLayerIds.clear()
    }

}

@Composable
actual fun MapWithLayers(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Double,
    styleUrl: String,
    onMapReady: (MapLayerController) -> Unit,
    onCameraMoved: (() -> Unit)?,
    onZoomChanged: ((Double) -> Unit)?
) {
    val controller = remember { MapLayerController() }
    var isInitialCameraSet by remember { mutableStateOf(false) }

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
                controller.mapView = this
                onCreate(null)
                onStart()
                onResume()
                // Request parent to not intercept touch events when touching the map
                // This prevents scroll conflicts with parent scrollable containers
                @SuppressLint("ClickableViewAccessibility")
                setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            // Tell parent to not intercept touch events
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            // Allow parent to intercept again
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    // Return false to let MapView handle the event
                    false
                }

                getMapAsync { map ->
                    map.setStyle(styleUrl) { style ->
                        // Set camera position
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(latitude, longitude))
                            .zoom(zoom)
                            .build()

                        isInitialCameraSet = true

                        // Add camera change listener to detect user gestures
                        onCameraMoved?.let { callback ->
                            map.addOnCameraMoveStartedListener { reason ->
                                // REASON_API_GESTURE = 1 means user initiated the movement
                                // REASON_API_ANIMATION = 2 means programmatic animation
                                // REASON_DEVELOPER_ANIMATION = 3 means developer initiated
                                if (reason == 1 && isInitialCameraSet) {
                                    Log.d("MapLayer", "User moved camera - disabling GPS following")
                                    callback()
                                }
                            }
                        }

                        // Add zoom change listener
                        onZoomChanged?.let { callback ->
                            map.addOnCameraIdleListener {
                                if (isInitialCameraSet) {
                                    callback(map.cameraPosition.zoom)
                                }
                            }
                        }

                        // Initialize controller
                        controller.setMap(map, style)
                        onMapReady(controller)
                    }
                }
            }
        }
    )

    DisposableEffect(controller) {
        onDispose {
            controller.mapView?.let { view ->
                view.onPause()
                view.onStop()
                view.onDestroy()
            }
            controller.mapView = null
        }
    }
}
