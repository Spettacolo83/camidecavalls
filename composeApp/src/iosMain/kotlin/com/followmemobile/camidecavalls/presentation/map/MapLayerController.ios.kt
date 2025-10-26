package com.followmemobile.camidecavalls.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf
import platform.CoreGraphics.CGRectMake
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSURL
import platform.Foundation.NSExpression
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIColor
import platform.darwin.NSObject
import MapLibre.*

// Extension function to convert ByteArray to NSData
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (this.isEmpty()) return null
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}

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

            println("=== iOS Adding Route Path ===")
            println("RouteId: $routeId")
            println("Raw GeoJSON length: ${geoJsonLineString.length} chars")
            println("Color: $color, Width: $width")

            // Wrap LineString in a Feature (like Android)
            val cleanedGeometry = geoJsonLineString.trim()
            val featureGeoJson = """{"type":"Feature","geometry":$cleanedGeometry,"properties":{}}"""

            println("iOS: Feature GeoJSON created, length: ${featureGeoJson.length}")

            // Create MLNShape directly from GeoJSON string
            val geoJsonData = featureGeoJson.encodeToByteArray()
            val nsData = geoJsonData.toNSData()

            println("iOS: NSData created: ${nsData != null}, size: ${nsData?.length}")

            nsData?.let { data ->
                val shapes = try {
                    MLNShape.shapeWithData(data, encoding = platform.Foundation.NSUTF8StringEncoding, error = null)
                } catch (e: Exception) {
                    println("iOS: Error creating shape: ${e.message}")
                    null
                }

                println("iOS: Shape created: ${shapes != null}")

                shapes?.let { shape ->
                    val source = MLNShapeSource(identifier = "source-$routeId", shape = shape, options = null)
                    currentStyle.addSource(source)
                    println("iOS: Source added")

                // Add white casing (outline)
                val casingLayer = MLNLineStyleLayer(identifier = "$routeId-casing", source = source)
                casingLayer.lineColor = NSExpression.expressionForConstantValue(UIColor.whiteColor)
                casingLayer.lineWidth = NSExpression.expressionForConstantValue(width + 2.0)
                casingLayer.lineCap = NSExpression.expressionForConstantValue("round")
                casingLayer.lineJoin = NSExpression.expressionForConstantValue("round")
                currentStyle.addLayer(casingLayer)
                println("iOS: Casing layer added")

                // Add colored route line
                val lineLayer = MLNLineStyleLayer(identifier = routeId, source = source)
                lineLayer.lineColor = NSExpression.expressionForConstantValue(uiColor)
                lineLayer.lineWidth = NSExpression.expressionForConstantValue(width.toDouble())
                    lineLayer.lineCap = NSExpression.expressionForConstantValue("round")
                    lineLayer.lineJoin = NSExpression.expressionForConstantValue("round")
                    currentStyle.addLayer(lineLayer)
                    println("iOS: Line layer added - Route rendering complete!")
                } ?: println("iOS: ERROR - Shape is null!")
            } ?: println("iOS: ERROR - NSData is null!")
        } catch (e: Exception) {
            println("iOS ERROR adding route path: ${e.message}")
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
            val uiColor = parseHexColor(color)

            // Create point GeoJSON as Feature
            val featureGeoJson = """{"type":"Feature","geometry":{"type":"Point","coordinates":[$longitude,$latitude]},"properties":{}}"""

            // Create MLNShape directly from GeoJSON string
            val geoJsonData = featureGeoJson.encodeToByteArray()
            val nsData = geoJsonData.toNSData()

            nsData?.let { data ->
                val shape = MLNShape.shapeWithData(data, encoding = platform.Foundation.NSUTF8StringEncoding, error = null)

                shape?.let { mlnShape ->
                    val source = MLNShapeSource(identifier = "source-$markerId", shape = mlnShape, options = null)
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
