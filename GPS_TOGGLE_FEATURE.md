# GPS Toggle Button Feature - Implementation Documentation

## Overview

The GPS toggle button is a critical feature in the tracking screen that allows users to control whether the map camera automatically follows their GPS position during a hiking session. When enabled (default), the map smoothly centers on the user's current location. When disabled, users can freely pan and zoom the map without the camera automatically recentering.

## User Experience

### Expected Behavior

1. **Default State**: GPS following is **enabled** when tracking starts
2. **Manual Disable**: User can tap the toggle button to disable GPS following
3. **Automatic Disable**: GPS following automatically disables when the user:
   - Drags/pans the map
   - Pinches to zoom in/out
   - Uses zoom buttons (if available)
4. **Re-enable**: User can tap the toggle button at any time to re-enable GPS following
5. **Visual Feedback**: Button icon changes between `GpsFixed` (enabled) and `GpsNotFixed` (disabled)

### Platform Consistency

The feature must work identically on both Android and iOS platforms, providing a seamless cross-platform experience.

## Technical Implementation

### Architecture

The implementation follows the **expect/actual** pattern for platform-specific map integration:

```
commonMain/
├── presentation/
│   ├── map/
│   │   └── MapLayerController.kt (expect declaration)
│   └── tracking/
│       └── TrackingScreen.kt (UI with toggle button)

androidMain/
├── presentation/
│   └── map/
│       └── MapLayerController.android.kt (actual implementation)

iosMain/
├── presentation/
│   └── map/
│       └── MapLayerController.ios.kt (actual implementation)
```

### Common Interface

**MapLayerController.kt** (commonMain)
```kotlin
expect class MapLayerController {
    fun updateCamera(latitude: Double, longitude: Double, zoom: Double?, animated: Boolean)
    fun getCurrentZoom(): Double
    fun addRoutePath(routeId: String, geoJsonLineString: String, color: String, width: Float)
    fun addMarker(markerId: String, latitude: Double, longitude: Double, color: String, radius: Float)
    fun removeLayer(layerId: String)
    fun clearAll()
}

@Composable
expect fun MapWithLayers(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Double,
    styleUrl: String,
    onMapReady: (MapLayerController) -> Unit,
    onCameraMoved: (() -> Unit)? = null,
    onZoomChanged: ((Double) -> Unit)? = null
)
```

### Android Implementation

**MapLayerController.android.kt**

Android uses MapLibre's `addOnCameraMoveStartedListener` to detect user-initiated camera movements:

```kotlin
map.addOnCameraMoveStartedListener { reason ->
    // REASON_API_GESTURE = 1 means user initiated the movement
    if (reason == 1 && isInitialCameraSet) {
        onCameraMoved?.invoke()
    }
}
```

Key points:
- `reason == 1` indicates user gesture (drag, pinch, etc.)
- `reason == 2` would indicate programmatic camera movement
- Simple and reliable implementation using MapLibre's built-in gesture detection

### iOS Implementation

**MapLayerController.ios.kt**

iOS uses MapLibre's `MLNMapViewDelegateProtocol` to detect camera changes:

```kotlin
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
```

Key points:
- Uses bitwise flags to detect gesture types:
  - `MLNCameraChangeReasonGesturePan` - drag gesture
  - `MLNCameraChangeReasonGesturePinch` - pinch zoom
  - `MLNCameraChangeReasonGestureZoomIn/Out` - zoom buttons
- Filters out programmatic camera changes (e.g., `MLNCameraChangeReasonProgrammatic`)
- Only triggers callback for actual user gestures

### UI Implementation

**TrackingScreen.kt** (commonMain)

The tracking screen manages the GPS following state and integrates with the map:

```kotlin
@Composable
private fun TrackingContent(
    route: Route?,
    sessionId: String,
    currentLocation: LocationData?,
    trackPoints: List<TrackPoint>,
    onStopTracking: () -> Unit
) {
    // GPS following state - enabled by default
    var followGpsLocation by remember { mutableStateOf(true) }
    var mapController by remember { mutableStateOf<MapLayerController?>(null) }

    // Stabilize callbacks to prevent recomposition
    val onMapReadyCallback = remember {
        { controller: MapLayerController ->
            mapController = controller
        }
    }

    val onCameraMovedCallback = remember {
        {
            followGpsLocation = false  // Disable GPS following on user gesture
        }
    }

    // Unique key to separate tracking map from other map instances
    key("tracking-map-$sessionId") {
        MapWithLayers(
            modifier = Modifier.fillMaxSize(),
            latitude = initialPosition.latitude,
            longitude = initialPosition.longitude,
            zoom = initialPosition.zoom,
            styleUrl = "https://tiles.openfreemap.org/styles/liberty",
            onMapReady = onMapReadyCallback,
            onCameraMoved = onCameraMovedCallback,
            onZoomChanged = onZoomChangedCallback
        )
    }

    // GPS following toggle button
    FloatingActionButton(
        onClick = {
            followGpsLocation = !followGpsLocation
        },
        containerColor = if (followGpsLocation) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Icon(
            imageVector = if (followGpsLocation) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
            contentDescription = if (followGpsLocation) "GPS Following Enabled" else "GPS Following Disabled"
        )
    }
}
```

Key implementation details:
1. **State management**: `followGpsLocation` boolean state controls the feature
2. **Callback stabilization**: Wrapped in `remember` to prevent unnecessary recompositions
3. **Unique keys**: Each map instance has a unique key to prevent conflicts
4. **Visual feedback**: Button color and icon change based on state

## The iOS Delegate Persistence Bug

### Problem Description

The GPS toggle button worked correctly on Android but had a critical bug on iOS: it would only detect the first user gesture (drag/zoom), then stop working. Subsequent gestures would not trigger the callback, making it impossible to re-enable GPS following after manual map interaction.

### Root Cause

The bug was caused by **delegate loss during Compose recomposition**:

1. **Frequent recompositions**: The TrackingContent composable recomposes every second when GPS updates arrive (new track points added)
2. **Delegate in factory**: The `MLNMapViewDelegate` was initially created inside `UIKitView`'s `factory` block
3. **Delegate lifecycle**: When the parent composable recomposes, UIKit views don't get recreated, but their configuration can be lost
4. **Lost reference**: After a few recompositions, the delegate reference was garbage collected or lost, breaking the callback chain

### Evidence from Debugging

During debugging, extensive logging revealed:
```
[MAP_DELEGATE] Map created
[MAP_DELEGATE] Style loaded
[MAP_DELEGATE] Camera will change (user gesture) - followGps: true
[GPS_FOLLOW_STATE] Setting followGps to false
[MAP_DELEGATE] Camera will change (user gesture) - followGps: false
[MAP_DELEGATE] Camera will change (user gesture) - followGps: false
// After 3-4 interactions, delegate methods stopped being called entirely
// No more [MAP_DELEGATE] logs appeared
```

This confirmed that:
- The delegate logic was correct (properly detecting gestures)
- The delegate was being lost after a few interactions
- The map itself continued working (rendering, responding to gestures)
- Only the callback mechanism was broken

### Solution

The fix involved two key changes to ensure delegate persistence:

**1. Move delegate outside factory using `remember`**

```kotlin
// Create delegate OUTSIDE factory so it persists across recompositions
val delegate = remember {
    object : NSObject(), MLNMapViewDelegateProtocol {
        override fun mapView(mapView: MLNMapView, didFinishLoadingStyle: MLNStyle) {
            controller.setMap(mapView, didFinishLoadingStyle)
            isInitialCameraSet.value = true
            onMapReady(controller)
        }

        // ... other delegate methods
    }
}
```

**2. Add update block to restore delegate on every recomposition**

```kotlin
UIKitView(
    modifier = modifier,
    factory = {
        val mapView = MLNMapView(
            frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
            styleURL = NSURL.URLWithString(styleUrl)
        )

        mapView.delegate = delegate
        mapView
    },
    update = { mapView ->
        // Restore delegate on every recomposition to prevent loss
        mapView.delegate = delegate
    }
)
```

### Why This Fix Works

1. **`remember` scope**: The delegate is created once and survives recompositions
2. **Stable reference**: The same delegate instance is reused across all recompositions
3. **Defensive restoration**: The `update` block ensures the delegate is always set, even if UIKit somehow loses it
4. **No recreation overhead**: The map view itself is only created once (in `factory`), only the delegate assignment happens on recomposition

### Additional Stabilizations

To further prevent recomposition-related issues:

**Stabilize callbacks with `remember`**
```kotlin
val onMapReadyCallback = remember {
    { controller: MapLayerController ->
        mapController = controller
    }
}

val onCameraMovedCallback = remember {
    {
        followGpsLocation = false
    }
}
```

**Use unique keys for map instances**
```kotlin
// In TrackingScreen
key("tracking-map-$sessionId") {
    MapWithLayers(...)
}

// In RouteDetailScreen
key("route-detail-map-${route.id}") {
    MapWithLayers(...)
}

// In idle state
key("idle-map-${route?.id ?: "no-route"}") {
    MapWithLayers(...)
}
```

This ensures that different map instances don't interfere with each other.

## Testing

### Manual Testing Checklist

- [x] GPS following enabled by default when tracking starts
- [x] Dragging the map automatically disables GPS following
- [x] Pinch zooming automatically disables GPS following
- [x] Toggle button correctly enables/disables GPS following
- [x] Button visual state updates correctly (icon and color)
- [x] Multiple sequential gestures all trigger the callback (iOS bug fixed)
- [x] GPS camera updates work smoothly when following is enabled
- [x] Programmatic camera updates don't trigger the callback
- [x] Feature works consistently after app backgrounding/foregrounding

### Platform Testing

**Android**: ✅ Working correctly
- FusedLocationProvider provides accurate GPS updates
- `addOnCameraMoveStartedListener` reliably detects user gestures
- No recomposition issues (different rendering pipeline)

**iOS**: ✅ Fixed and working correctly
- CoreLocation provides accurate GPS updates
- `MLNMapViewDelegateProtocol` now reliably detects all user gestures
- Delegate persistence issue resolved
- Tested with iOS Simulator using GPX file simulation

## Lessons Learned

### Compose Lifecycle with UIKit

When integrating UIKit views with Compose Multiplatform:

1. **Delegate pattern with `remember`**: Always create delegates outside the `factory` block using `remember` to ensure persistence
2. **Update block defensive coding**: Use the `update` block to restore critical properties that might be lost
3. **Callback stabilization**: Wrap callbacks in `remember` to prevent unnecessary recompositions
4. **Instance separation**: Use unique `key()` values when multiple instances of the same composable exist
5. **Recomposition awareness**: Understand what triggers recompositions and how they affect platform views

### Cross-Platform Development

1. **Platform parity**: Test features on both platforms, as rendering pipelines differ significantly
2. **Debug systematically**: Use extensive logging to understand platform-specific behavior
3. **Native APIs first**: Prefer native map APIs (like delegate methods) over custom workarounds
4. **Lifecycle differences**: Android's `AndroidView` and iOS's `UIKitView` have different lifecycle behaviors

## Future Improvements

Potential enhancements to the GPS toggle feature:

1. **Persistence**: Remember user's GPS following preference across app sessions
2. **Auto-resume**: Optionally auto-enable GPS following when user stops interacting for X seconds
3. **Animation**: Add smooth animation when camera re-centers on GPS position
4. **Distance threshold**: Only disable GPS following if user moves map beyond a certain distance
5. **Zoom-only mode**: Allow zoom gestures without disabling GPS following (only pan disables it)

## References

- MapLibre iOS Documentation: https://maplibre.org/maplibre-native/ios/latest/
- MapLibre Android Documentation: https://maplibre.org/maplibre-native/android/api/
- Compose Multiplatform UIKit Interop: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-ios-ui-integration.html
- Kotlin Expect/Actual: https://kotlinlang.org/docs/multiplatform-expect-actual.html
