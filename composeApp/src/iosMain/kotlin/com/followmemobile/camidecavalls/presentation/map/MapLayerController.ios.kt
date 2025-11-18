package com.followmemobile.camidecavalls.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import MapLibre.*
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSData
import platform.Foundation.NSExpression
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIColor
import platform.UIKit.UITapGestureRecognizer
import platform.darwin.NSObject
import platform.objc.sel_registerName
import kotlin.math.pow

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
    internal var mapView: MLNMapView? = null
    private var style: MLNStyle? = null
    internal var onMarkerClick: ((String) -> Unit)? = null
    // Store marker coordinates for tap detection
    internal val markerCoordinates = mutableMapOf<String, Pair<Double, Double>>()
    private val managedLayerIds = mutableSetOf<String>()
    private var highlightedMarkerId: String? = null
    private var highlightTimer: NSTimer? = null

    internal fun setMap(map: MLNMapView, loadedStyle: MLNStyle) {
        this.mapView = map
        this.style = loadedStyle
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
        val map = mapView ?: return
        val targetZoom = zoom ?: map.zoomLevel

        map.setCenterCoordinate(
            centerCoordinate = CLLocationCoordinate2DMake(latitude, longitude),
            zoomLevel = targetZoom,
            animated = animated
        )
    }

    actual fun centerLocationWithVerticalOffset(
        latitude: Double,
        longitude: Double,
        offsetPixels: Float?,
        animated: Boolean
    ) {
        val map = mapView ?: return
        val offset = offsetPixels
        if (offset == null || offset <= 0f) {
            val currentZoom = map.zoomLevel
            val baseOffset = 0.015
            val zoomFactor = 2.0.pow(currentZoom - 10.0)
            val latitudeOffset = baseOffset / zoomFactor
            map.setCenterCoordinate(
                centerCoordinate = CLLocationCoordinate2DMake(latitude - latitudeOffset, longitude),
                zoomLevel = currentZoom,
                animated = animated
            )
            return
        }

        val offsetValue = offset.toDouble()
        val currentPoint = map.convertCoordinate(
            CLLocationCoordinate2DMake(latitude, longitude),
            toPointToView = map
        )
        val targetPoint = CGPointMake(currentPoint.x, currentPoint.y + offsetValue)
        val targetCoordinate = map.convertPoint(targetPoint, toCoordinateFromView = map)
        map.setCenterCoordinate(
            centerCoordinate = targetCoordinate,
            zoomLevel = map.zoomLevel,
            animated = animated
        )
    }

    actual fun getCurrentZoom(): Double {
        return mapView?.zoomLevel ?: 14.0
    }

    actual fun setHighlightedMarker(markerId: String?, colorHex: String?) {
        val currentStyle = style ?: return
        if (highlightedMarkerId == markerId) {
            return
        }

        clearCurrentHighlight()

        if (markerId == null || colorHex == null) return

        val source = currentStyle.sourceWithIdentifier("source-$markerId") ?: return
        highlightedMarkerId = markerId

        val highlightColor = parseHexColor(colorHex)

        val rippleLayerId = "$markerId-ripple"
        val rippleLayer = MLNCircleStyleLayer(identifier = rippleLayerId, source = source as MLNSource)
        rippleLayer.circleColor = NSExpression.expressionForConstantValue(highlightColor)
        rippleLayer.circleOpacity = NSExpression.expressionForConstantValue(0.0)
        rippleLayer.circleRadius = NSExpression.expressionForConstantValue(10.0)
        rippleLayer.circleBlur = NSExpression.expressionForConstantValue(0.25)
        rippleLayer.circleSortKey = NSExpression.expressionForConstantValue(230.0)

        currentStyle.addLayerBelow(rippleLayer, currentStyle.layerWithIdentifier(markerId))

        elevateMarker(markerId, highlighted = true)

        val foregroundLayerId = "$markerId-foreground"
        val foregroundLayer = MLNCircleStyleLayer(identifier = foregroundLayerId, source = source)
        foregroundLayer.circleColor = NSExpression.expressionForConstantValue(highlightColor)
        foregroundLayer.circleRadius = NSExpression.expressionForConstantValue(10.0)
        foregroundLayer.circleOpacity = NSExpression.expressionForConstantValue(1.0)
        foregroundLayer.circleStrokeColor = NSExpression.expressionForConstantValue(UIColor.whiteColor)
        foregroundLayer.circleStrokeWidth = NSExpression.expressionForConstantValue(2.0)
        foregroundLayer.circleSortKey = NSExpression.expressionForConstantValue(260.0)
        currentStyle.addLayer(foregroundLayer)

        var phase = 0.0
        highlightTimer = NSTimer.scheduledTimerWithTimeInterval(
            timeInterval = 0.016,
            repeats = true,
            block = { _ ->
                phase += 0.016
                val progress = ((phase % 2.0) / 2.0).coerceIn(0.0, 1.0)
                val radius = 10.0 + progress * 46.0
                val fadeInThreshold = 0.12
                val opacity = when {
                    progress <= fadeInThreshold -> 0.6 * (progress / fadeInThreshold)
                    else -> 0.6 * (1.0 - progress)
                }.coerceIn(0.0, 0.6)
                (style?.layerWithIdentifier(rippleLayerId) as? MLNCircleStyleLayer)?.let { layer ->
                    layer.circleRadius = NSExpression.expressionForConstantValue(radius)
                    layer.circleOpacity = NSExpression.expressionForConstantValue(opacity)
                }
            }
        )
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
            managedLayerIds += routeId
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

        // Store coordinates for tap detection
        markerCoordinates[markerId] = Pair(latitude, longitude)

        // Remove existing layers and source (if any)
        removeLayer(markerId)

        try {
            managedLayerIds += markerId
            val uiColor = parseHexColor(color)
            // Add markerId and type as properties so we can identify it when tapped
            val featureGeoJson = """{"type":"Feature","geometry":{"type":"Point","coordinates":[$longitude,$latitude]},"properties":{"markerId":"$markerId","type":"poi-marker"}}"""

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
                    outerCircle.circleSortKey = NSExpression.expressionForConstantValue(0.5)
                    currentStyle.addLayer(outerCircle)

                    val innerCircle = MLNCircleStyleLayer(identifier = markerId, source = source)
                    innerCircle.circleColor = NSExpression.expressionForConstantValue(uiColor)
                    innerCircle.circleRadius = NSExpression.expressionForConstantValue(radius.toDouble())
                    innerCircle.circleSortKey = NSExpression.expressionForConstantValue(1.0)
                    currentStyle.addLayer(innerCircle)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    actual fun removeLayer(layerId: String) {
        val currentStyle = style ?: return
        if (highlightedMarkerId == layerId) {
            clearCurrentHighlight()
        }
        try {
            // Remove all associated layers for this ID
            currentStyle.layerWithIdentifier(layerId)?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.layerWithIdentifier("$layerId-outer")?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.layerWithIdentifier("$layerId-ripple")?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.layerWithIdentifier("$layerId-foreground")?.let {
                currentStyle.removeLayer(it)
            }
            currentStyle.layerWithIdentifier("$layerId-casing")?.let {
                currentStyle.removeLayer(it)
            }
            // Remove source last
            currentStyle.sourceWithIdentifier("source-$layerId")?.let {
                currentStyle.removeSource(it)
            }
            managedLayerIds.remove(layerId)
            markerCoordinates.remove(layerId)
        } catch (e: Exception) {
            // Layer might not exist, ignore
        }
    }

    actual fun clearAll() {
        clearCurrentHighlight()
        val ids = managedLayerIds.toList()
        ids.forEach { id ->
            removeLayer(id)
        }
        managedLayerIds.clear()
        markerCoordinates.clear()
    }

    private fun clearCurrentHighlight() {
        highlightTimer?.invalidate()
        highlightTimer = null
        highlightedMarkerId?.let { markerId ->
            (style?.layerWithIdentifier(markerId) as? MLNCircleStyleLayer)?.let { layer ->
                layer.circleRadius = NSExpression.expressionForConstantValue(8.0)
                layer.circleSortKey = NSExpression.expressionForConstantValue(1.0)
            }
            (style?.layerWithIdentifier("$markerId-outer") as? MLNCircleStyleLayer)?.let { layer ->
                layer.circleRadius = NSExpression.expressionForConstantValue(10.0)
                layer.circleSortKey = NSExpression.expressionForConstantValue(0.5)
            }
            style?.layerWithIdentifier("$markerId-ripple")?.let { ripple ->
                style?.removeLayer(ripple)
            }
            style?.layerWithIdentifier("$markerId-foreground")?.let { layer ->
                style?.removeLayer(layer)
            }
        }
        highlightedMarkerId = null
    }

    private fun elevateMarker(markerId: String, highlighted: Boolean) {
        val sortKey = if (highlighted) 220.0 else 1.0
        val innerRadius = if (highlighted) 10.0 else 8.0
        val outerRadius = if (highlighted) 12.0 else 10.0
        (style?.layerWithIdentifier(markerId) as? MLNCircleStyleLayer)?.let { layer ->
            layer.circleRadius = NSExpression.expressionForConstantValue(innerRadius)
            layer.circleSortKey = NSExpression.expressionForConstantValue(sortKey)
        }
        (style?.layerWithIdentifier("$markerId-outer") as? MLNCircleStyleLayer)?.let { layer ->
            layer.circleRadius = NSExpression.expressionForConstantValue(outerRadius)
            layer.circleSortKey = NSExpression.expressionForConstantValue(sortKey - 5.0)
        }
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

    // Tap gesture recognizer delegate to allow simultaneous gestures
    val gestureDelegate = remember {
        object : NSObject(), platform.UIKit.UIGestureRecognizerDelegateProtocol {
            override fun gestureRecognizer(
                gestureRecognizer: platform.UIKit.UIGestureRecognizer,
                shouldRecognizeSimultaneouslyWithGestureRecognizer: platform.UIKit.UIGestureRecognizer
            ): Boolean {
                // Allow this recognizer to work simultaneously with other recognizers
                return true
            }
        }
    }

    // Tap gesture recognizer handler to detect taps on markers
    val tapHandler = remember {
        object : NSObject() {
            @ObjCAction
            fun handleTap(recognizer: UITapGestureRecognizer) {
                val mapView = controller.mapView ?: return
                val tapPoint = recognizer.locationInView(mapView)

                println("🔍 Tap at screen: ${tapPoint.useContents { "x=$x, y=$y" }}")

                // Find the closest marker by converting each marker's geographic coords to screen coords
                var closestMarkerId: String? = null
                var closestDistance = Double.MAX_VALUE

                controller.markerCoordinates.forEach { (markerId, markerCoord) ->
                    // Convert marker's geographic coordinate to screen point
                    val markerGeoCoord = CLLocationCoordinate2DMake(
                        latitude = markerCoord.first,
                        longitude = markerCoord.second
                    )
                    val markerScreenPoint = mapView.convertCoordinate(
                        markerGeoCoord,
                        toPointToView = mapView
                    )

                    // Calculate screen distance in pixels
                    val dx = tapPoint.useContents { x } - markerScreenPoint.useContents { x }
                    val dy = tapPoint.useContents { y } - markerScreenPoint.useContents { y }
                    val screenDistance = dx * dx + dy * dy  // squared distance

                    if (screenDistance < closestDistance) {
                        closestDistance = screenDistance
                        closestMarkerId = markerId
                    }
                }

                // Threshold in screen pixels squared (about 40 pixels radius)
                val threshold = 40.0 * 40.0

                println("🎯 Closest: $closestMarkerId at screen distance $closestDistance pixels² (threshold: $threshold)")

                if (closestDistance < threshold && closestMarkerId != null) {
                    println("✅ iOS Marker tapped: $closestMarkerId")
                    controller.onMarkerClick?.invoke(closestMarkerId)
                }
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

            // Add tap gesture recognizer for marker clicks
            val tapGesture = platform.UIKit.UITapGestureRecognizer(
                target = tapHandler,
                action = platform.objc.sel_registerName("handleTap:")
            )
            tapGesture.delegate = gestureDelegate
            mapView.addGestureRecognizer(tapGesture)

            mapView
        },
        update = { mapView ->
            // Restore delegate on every recomposition to prevent loss
            mapView.delegate = delegate
        }
    )
}
