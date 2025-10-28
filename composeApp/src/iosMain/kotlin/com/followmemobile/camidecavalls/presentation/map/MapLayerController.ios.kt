package com.followmemobile.camidecavalls.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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

    actual fun updateCamera(
        latitude: Double,
        longitude: Double,
        zoom: Double?,
        animated: Boolean
    ) {
        val map = mapView ?: return
        val targetZoom = zoom ?: map.zoomLevel

        map.setCenterCoordinate(
            centerCoordinate = CLLocationCoordinate2DMake(latitude, longitude),
            zoomLevel = targetZoom,
            animated = animated
        )
    }

    actual fun getCurrentZoom(): Double {
        return mapView?.zoomLevel ?: 14.0
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
            val uiColor = parseHexColor(color)
            val cleanedGeometry = geoJsonLineString.trim()
            val featureGeoJson = """{"type":"Feature","geometry":$cleanedGeometry,"properties":{}}"""

            val geoJsonData = featureGeoJson.encodeToByteArray()
            val nsData = geoJsonData.toNSData()

            nsData?.let { data ->
                val shapes = try {
                    MLNShape.shapeWithData(data, encoding = platform.Foundation.NSUTF8StringEncoding, error = null)
                } catch (e: Exception) {
                    null
                }

                shapes?.let { shape ->
                    val source = MLNShapeSource(identifier = "source-$routeId", shape = shape, options = null)
                    currentStyle.addSource(source)

                    val casingLayer = MLNLineStyleLayer(identifier = "$routeId-casing", source = source)
                    casingLayer.lineColor = NSExpression.expressionForConstantValue(UIColor.whiteColor)
                    casingLayer.lineWidth = NSExpression.expressionForConstantValue(width + 2.0)
                    casingLayer.lineCap = NSExpression.expressionForConstantValue("round")
                    casingLayer.lineJoin = NSExpression.expressionForConstantValue("round")
                    currentStyle.addLayer(casingLayer)

                    val lineLayer = MLNLineStyleLayer(identifier = routeId, source = source)
                    lineLayer.lineColor = NSExpression.expressionForConstantValue(uiColor)
                    lineLayer.lineWidth = NSExpression.expressionForConstantValue(width.toDouble())
                    lineLayer.lineCap = NSExpression.expressionForConstantValue("round")
                    lineLayer.lineJoin = NSExpression.expressionForConstantValue("round")
                    currentStyle.addLayer(lineLayer)
                }
            }
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

        // Remove existing layers and source (if any)
        removeLayer(markerId)

        try {
            val uiColor = parseHexColor(color)
            val featureGeoJson = """{"type":"Feature","geometry":{"type":"Point","coordinates":[$longitude,$latitude]},"properties":{}}"""

            val geoJsonData = featureGeoJson.encodeToByteArray()
            val nsData = geoJsonData.toNSData()

            nsData?.let { data ->
                val shape = MLNShape.shapeWithData(data, encoding = platform.Foundation.NSUTF8StringEncoding, error = null)

                shape?.let { mlnShape ->
                    val source = MLNShapeSource(identifier = "source-$markerId", shape = mlnShape, options = null)
                    currentStyle.addSource(source)

                    val outerCircle = MLNCircleStyleLayer(identifier = "$markerId-outer", source = source)
                    outerCircle.circleColor = NSExpression.expressionForConstantValue(UIColor.whiteColor)
                    outerCircle.circleRadius = NSExpression.expressionForConstantValue(radius + 2.0)
                    currentStyle.addLayer(outerCircle)

                    val innerCircle = MLNCircleStyleLayer(identifier = markerId, source = source)
                    innerCircle.circleColor = NSExpression.expressionForConstantValue(uiColor)
                    innerCircle.circleRadius = NSExpression.expressionForConstantValue(radius.toDouble())
                    currentStyle.addLayer(innerCircle)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    actual fun removeLayer(layerId: String) {
        val currentStyle = style ?: return
        try {
            // Remove all associated layers for this ID
            currentStyle.layerWithIdentifier(layerId)?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.layerWithIdentifier("$layerId-outer")?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.layerWithIdentifier("$layerId-casing")?.let {
                currentStyle.removeLayer(it)
            }
            // Remove source last
            currentStyle.sourceWithIdentifier("source-$layerId")?.let {
                currentStyle.removeSource(it)
            }
        } catch (e: Exception) {
            // Layer might not exist, ignore
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
    onMapReady: (MapLayerController) -> Unit,
    onCameraMoved: (() -> Unit)?,
    onZoomChanged: ((Double) -> Unit)?
) {
    val controller = remember { MapLayerController() }
    var isInitialCameraSet = remember { mutableStateOf(false) }

    // Create delegate OUTSIDE factory so it persists across recompositions
    val delegate = remember {
        object : NSObject(), MLNMapViewDelegateProtocol {
            override fun mapView(mapView: MLNMapView, didFinishLoadingStyle: MLNStyle) {
                controller.setMap(mapView, didFinishLoadingStyle)
                isInitialCameraSet.value = true
                onMapReady(controller)
            }

            @kotlinx.cinterop.ObjCSignatureOverride
            override fun mapView(
                mapView: MLNMapView,
                regionWillChangeWithReason: MLNCameraChangeReason,
                animated: Boolean
            ) {
                if (!isInitialCameraSet.value) return

                val isUserGesture = regionWillChangeWithReason.and(MLNCameraChangeReasonGesturePan) != 0uL ||
                                   regionWillChangeWithReason.and(MLNCameraChangeReasonGesturePinch) != 0uL ||
                                   regionWillChangeWithReason.and(MLNCameraChangeReasonGestureZoomIn) != 0uL ||
                                   regionWillChangeWithReason.and(MLNCameraChangeReasonGestureZoomOut) != 0uL

                if (isUserGesture) {
                    onCameraMoved?.invoke()
                }
            }

            @kotlinx.cinterop.ObjCSignatureOverride
            override fun mapView(mapView: MLNMapView, regionDidChangeAnimated: Boolean) {
                if (!isInitialCameraSet.value) return
                onZoomChanged?.invoke(mapView.zoomLevel)
            }
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val mapView = MLNMapView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                styleURL = NSURL.URLWithString(styleUrl)
            )

            mapView.setCenterCoordinate(
                centerCoordinate = CLLocationCoordinate2DMake(latitude, longitude),
                zoomLevel = zoom,
                animated = false
            )

            mapView.delegate = delegate
            mapView
        },
        update = { mapView ->
            // Restore delegate on every recomposition to prevent loss
            mapView.delegate = delegate
        }
    )
}
