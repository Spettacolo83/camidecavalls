package com.followmemobile.camidecavalls.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectMake
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSURL
import platform.Foundation.NSExpression
import platform.UIKit.UIColor
import platform.darwin.NSObject
import MapLibre.*

@OptIn(ExperimentalForeignApi::class)
actual class MapLayerController {
    private var mapView: MLNMapView? = null
    private var style: MLNStyle? = null

    internal fun setMap(map: MLNMapView, loadedStyle: MLNStyle) {
        this.mapView = map
        this.style = loadedStyle
    }

    actual fun addRoutePath(
        routeId: String,
        geoJsonLineString: String,
        color: String,
        width: Float
    ) {
        val currentStyle = style ?: return

        // Remove existing layers/sources
        removeLayer("$routeId-casing")
        removeLayer(routeId)

        try {
            // Parse color
            val uiColor = parseHexColor(color)

            // Add GeoJSON source
            val url = NSURL.URLWithString("data:application/json;charset=utf-8,$geoJsonLineString")
            url?.let {
                val source = MLNShapeSource(identifier = "source-$routeId", URL = it, options = null)
                currentStyle.addSource(source)

                // Add white casing (outline)
                val casingLayer = MLNLineStyleLayer(identifier = "$routeId-casing", source = source)
                casingLayer.lineColor = NSExpression.expressionForConstantValue(UIColor.whiteColor)
                casingLayer.lineWidth = NSExpression.expressionForConstantValue(width + 2.0)
                casingLayer.lineCap = NSExpression.expressionForConstantValue("round")
                casingLayer.lineJoin = NSExpression.expressionForConstantValue("round")
                currentStyle.addLayer(casingLayer)

                // Add colored route line
                val lineLayer = MLNLineStyleLayer(identifier = routeId, source = source)
                lineLayer.lineColor = NSExpression.expressionForConstantValue(uiColor)
                lineLayer.lineWidth = NSExpression.expressionForConstantValue(width.toDouble())
                lineLayer.lineCap = NSExpression.expressionForConstantValue("round")
                lineLayer.lineJoin = NSExpression.expressionForConstantValue("round")
                currentStyle.addLayer(lineLayer)
            }
        } catch (e: Exception) {
            println("Error adding route path: ${e.message}")
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
            val uiColor = parseHexColor(color)

            // Create point GeoJSON
            val geoJson = """{"type":"Point","coordinates":[$longitude,$latitude]}"""
            val url = NSURL.URLWithString("data:application/json;charset=utf-8,$geoJson")
            url?.let {
                val source = MLNShapeSource(identifier = "source-$markerId", URL = it, options = null)
                currentStyle.addSource(source)

                // Add outer white circle
                val outerCircle = MLNCircleStyleLayer(identifier = "$markerId-outer", source = source)
                outerCircle.circleColor = NSExpression.expressionForConstantValue(UIColor.whiteColor)
                outerCircle.circleRadius = NSExpression.expressionForConstantValue(radius + 2.0)
                currentStyle.addLayer(outerCircle)

                // Add inner colored circle
                val innerCircle = MLNCircleStyleLayer(identifier = markerId, source = source)
                innerCircle.circleColor = NSExpression.expressionForConstantValue(uiColor)
                innerCircle.circleRadius = NSExpression.expressionForConstantValue(radius.toDouble())
                currentStyle.addLayer(innerCircle)
            }
        } catch (e: Exception) {
            println("Error adding marker: ${e.message}")
        }
    }

    actual fun removeLayer(layerId: String) {
        val currentStyle = style ?: return
        try {
            currentStyle.layerWithIdentifier(layerId)?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.sourceWithIdentifier("source-$layerId")?.let {
                currentStyle.removeSource(it)
            }
        } catch (e: Exception) {
            // Layer might not exist
        }
    }

    actual fun clearAll() {
        // Layers will be cleared when map is disposed
    }

    private fun parseHexColor(hex: String): UIColor {
        val cleanHex = hex.removePrefix("#")
        val r = cleanHex.substring(0, 2).toInt(16) / 255.0
        val g = cleanHex.substring(2, 4).toInt(16) / 255.0
        val b = cleanHex.substring(4, 6).toInt(16) / 255.0
        return UIColor.colorWithRed(r, g, b, 1.0)
    }
}

@OptIn(ExperimentalForeignApi::class)
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

    UIKitView(
        modifier = modifier,
        factory = {
            val mapView = MLNMapView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                styleURL = NSURL.URLWithString(styleUrl)
            )

            // Set camera position
            mapView.setCenterCoordinate(
                centerCoordinate = CLLocationCoordinate2DMake(latitude, longitude),
                zoomLevel = zoom,
                animated = false
            )

            // Set up delegate to get style loaded callback
            val delegate = object : NSObject(), MLNMapViewDelegateProtocol {
                override fun mapView(mapView: MLNMapView, didFinishLoadingStyle: MLNStyle) {
                    controller.setMap(mapView, didFinishLoadingStyle)
                    onMapReady(controller)
                }
            }
            mapView.delegate = delegate

            mapView
        },
        update = { mapView ->
            mapView.setCenterCoordinate(
                centerCoordinate = CLLocationCoordinate2DMake(latitude, longitude),
                zoomLevel = zoom,
                animated = false
            )
        }
    )
}
