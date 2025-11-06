# Test Route Files for GPS Simulation

GPX test route files for simulating GPS positions on Android and iOS emulators.

## 🎯 Current Status

✅ **Android Format**: Route points (rtept) without timestamps - TESTED
✅ **iOS Format**: Waypoints (wpt) with timestamps - Xcode compatible
✅ **High Resolution**: 150-1300 points per route (from original GPX files)
✅ **Android LocationService**: Fixed callbackFlow issue with StateFlow
⚠️ **Elevation**: Currently set to 0 (script provided to add real elevation data)

## Directory Structure

- `android/` - 20 GPX files for Android emulator (route format, no timestamps)
- `ios/` - 20 GPX files for iOS simulator (waypoint format, with timestamps)
- **Different formats optimized for each platform**

## Routes (All 20 Stages)

| # | Route | Distance | Points |
|---|-------|----------|--------|
| 1 | Maó - Es Grau | 8.1 km | 651 |
| 2 | Es Grau - Favàritx | 7.9 km | 778 |
| 3 | Favàritx - Arenal d'en Castell | 10.6 km | 820 |
| 4 | Arenal d'en Castell - Son Parc | 4.8 km | 604 |
| 5 | Son Parc - Fornells | 6.7 km | 906 |
| 6 | Fornells - Cala Tirant | 5.4 km | 964 |
| 7 | Cala Tirant - Binimel·là | 11.2 km | 788 |
| 8 | Binimel·là - Els Alocs | 7.3 km | 363 |
| 9 | Els Alocs - Algaiarens | 8.8 km | 555 |
| 10 | Algaiarens - Ciutadella | 13.4 km | 463 |
| 11 | Ciutadella - Cap d'Artrutx | 9.2 km | 642 |
| 12 | Cap d'Artrutx - Cala en Turqueta | 10.8 km | 1315 |
| 13 | Cala en Turqueta - Cala Galdana | 6.9 km | 596 |
| 14 | Cala Galdana - Sant Tomàs | 9.5 km | 784 |
| 15 | Sant Tomàs - Son Bou | 7.1 km | 350 |
| 16 | Son Bou - Cala en Porter | 8.7 km | 627 |
| 17 | Cala en Porter - Binissafúller | 11.4 km | 756 |
| 18 | Binissafúller - Punta Prima | 6.3 km | 282 |
| 19 | Punta Prima - Alcalfar | 7.8 km | 563 |
| 20 | Alcalfar - Maó | 8.6 km | 154 |

## 🚀 How to Use

### Android Emulator

1. **Start Android emulator**
2. **Open Extended Controls** → Click three dots (...) in toolbar
3. **Go to Location** → Select "Location" from left panel
4. **Load GPX**:
   - Click **"Load GPX/KML"** button
   - Navigate to: `test-routes/android/route_1.gpx`
   - File will load and show route on map
5. **Start Simulation**:
   - Click **Play ▶** button
   - **Adjust Speed**: Use slider (1x, 2x, 5x, 10x, etc.)
   - **Control**: Pause/Resume/Stop as needed
6. **Your app will receive location updates automatically**

**Troubleshooting Android**:
- If location doesn't update: Check app has location permission granted
- If route loads but doesn't move: Click the Play button (not just load)
- If movement is slow: Increase speed multiplier (5x or 10x recommended for testing)

### iOS Simulator

#### Method 1: Xcode (Recommended)

1. **Run your app** in iOS Simulator
2. **While running**, in Xcode menu:
   - **Debug → Simulate Location → Add GPX File to Project...**
   - Select: `test-routes/ios/route_1.gpx`
3. **Simulator will play the route**
   - Icon should turn blue when active
   - Movement is automatic

**Note**: GPX file must be added while the app is running for it to work.

#### Method 2: Command Line (Static Location Only)

```bash
# Get device ID
xcrun simctl list devices | grep Booted

# Set single location (first point of route 1)
xcrun simctl location <device-id> 39.8975369761 4.2573604472
```

**Troubleshooting iOS**:
- Icon doesn't turn blue: Make sure app is running when you add GPX
- No movement: Try Debug → Simulate Location → None, then reload GPX
- File not found: Use absolute path to GPX file

### Testing Distance Check Feature

#### Test 1: Near Route (No Warning)
1. Load `route_1.gpx`
2. Open TrackingScreen for Route 1
3. **Expected**: Tracking starts immediately

#### Test 2: Far from Route (Warning Dialog)
1. Load `route_5.gpx` (Fornells - north)
2. Open TrackingScreen for Route 1 (Maó - east, ~25km away)
3. **Expected**: Dialog "Sei lontano dal percorso" with distance shown
4. Options: "Inizia comunque" or "Annulla"

#### Test 3: Live Movement
1. Load any GPX, start playback
2. Open TrackingScreen for that route
3. **Expected**:
   - Blue line: Route path
   - Orange marker: Current position (moving)
   - Green line: User track (appears as you move)
   - Map follows position

## 📁 File Formats

### Android GPX Structure (Route Format)

```xml
<?xml version="1.0" encoding="utf-8"?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="CamiDeCavalls">
  <rte>
    <name>Maó - Es Grau</name>
    <number>1</number>
    <rtept lat="39.8975369761" lon="4.2573604472">
      <ele>0</ele>
    </rtept>
    <!-- More route points... -->
  </rte>
</gpx>
```

**Android Details**:
- Format: `<rte>` with `<rtept>` (route format)
- No timestamps: Emulator controls playback speed with slider
- Elevation: Set to 0 (can be added later)
- High point density: 150-1300 points per route

### iOS GPX Structure (Waypoint Format)

```xml
<?xml version="1.0" encoding="utf-8"?>
<gpx version="1.1" creator="Xcode">
    <wpt lat="39.8975369761" lon="4.2573604472">
        <name>Maó - Es Grau - Start</name>
        <time>2025-10-26T22:37:25Z</time>
    </wpt>
    <wpt lat="39.8974894734" lon="4.2576030107">
        <time>2025-10-26T22:37:40Z</time>
    </wpt>
    <!-- More waypoints... -->
</gpx>
```

**iOS Details**:
- Format: `<wpt>` (waypoints, Xcode standard)
- WITH timestamps: iOS interpolates movement between waypoints
- Timing: 3 km/h hiking speed (realistic)
- Compatible with Xcode Debug → Simulate Location

## 🏔️ Adding Real Elevation Data

The files currently have elevation set to 0. To add real elevation data:

### Option 1: Using Open-Elevation API (Slow but Free)

```bash
# This may take 10-20 minutes due to API rate limits
python3 /tmp/add_elevation_to_gpx.py test-routes/android
python3 /tmp/add_elevation_to_gpx.py test-routes/ios
```

The script will:
- Fetch real elevation for each point using Open-Elevation API
- Update files in-place
- Handle rate limiting automatically
- Use elevation 0 as fallback if API fails

### Option 2: Manual Elevation (Future)

If you have access to:
- Google Elevation API (requires API key - faster)
- SRTM offline data
- Official GPX files with elevation from Camí de Cavalls

We can create a faster script.

### Why Elevation Matters

For a trail hiking app like Camí de Cavalls:
- **Realistic simulation**: Altitude affects GPS accuracy
- **Elevation profile**: You mentioned wanting to show altitude charts
- **Training data**: Essential for calculating climbing/descending stats

## 🔧 Route 11 Fix (Ciutadella - Cap d'Artrutx)

Route 11 had coordinate issues that caused visual artifacts on the map:

### Problem Identified

**Symptoms**:
- Red and green markers overlapped at the same position
- A line connecting end to start (closing an incorrect loop)
- Route appeared to start and end at the same coordinate

**Root Cause**:
1. **Duplicate coordinate**: `[3.83370108, 39.9789560747]` appeared 3 times (indices 0, 355, 641)
2. **Wrong coordinate order**: A 7.7km jump between index 354→355 indicated the route was split at the wrong point
3. **Start = End**: The first and last coordinates were identical, making it appear circular when it should be linear

### Solution Applied

**Fix Script**: `scripts/fix_test_route11_gpx_v2.py`

**Steps**:
1. **Removed duplicate**: Deleted the middle occurrence at index 355
2. **Reordered coordinates**: Applied transformation `[355→639] + [0→354]`
3. **Result**:
   - Android: 642 → 640 points
   - iOS: 642 → 641 points
   - START: `[3.83296486, 40.00149427]` (Ciutadella)
   - END: `[3.82330099, 39.93185160]` (Cap d'Artrutx)
   - Maximum jump: 163m (was 7773m)

**Database Version**: Incremented to v9 in `InitializeDatabaseUseCase.kt` to force route data reload

### Verification

```bash
# Verify the fix (should show START ≠ END with reasonable distance)
cd scripts
python3 analyze_route11_jumps.py

# Expected output:
# - Biggest jump: < 200m (not 7700m)
# - Distance START→END: ~7-8km (linear route)
```

### Files Fixed

- ✅ `composeApp/src/commonMain/kotlin/.../data/RouteData.kt` (GeoJSON in code)
- ✅ `test-routes/android/route_11.gpx` (Android emulator test file)
- ✅ `test-routes/ios/route_11.gpx` (iOS simulator test file)

**Important**: The same fix was applied to all three locations to ensure consistency across app data and test files.

## 🔄 Regenerating Files

If RouteData.kt changes or original GPX files are updated:

```bash
# Step 1: Generate Android GPX (route format, no timestamps)
python3 /tmp/convert_original_gpx_simple.py \
  "/tmp" \
  "test-routes/android" \
  "test-routes/android"  # Note: both point to android for this script

# Step 2: Generate iOS GPX (waypoint format, with timestamps) from Android files
python3 /tmp/create_ios_gpx_waypoints.py \
  "test-routes/android" \
  "test-routes/ios"

# Step 3 (Optional): Add real elevation data
python3 /tmp/add_elevation_to_gpx.py test-routes/android
# Then regenerate iOS to include elevation
python3 /tmp/create_ios_gpx_waypoints.py test-routes/android test-routes/ios
```

## 🐛 Common Issues

### "Acquiring GPS signal..." on Android App

**FIXED** - AndroidLocationService now uses StateFlow instead of callbackFlow

**If still seeing this issue, debug with Logcat**:

1. **Open Logcat** in Android Studio
2. **Filter**: `AndroidLocationService`
3. **Look for these logs**:

```
🚀 startTracking called
✅ Location permission granted
⚙️ Creating LocationRequest with interval=5000ms
📡 Requesting location updates...
✅ Location updates requested successfully
📍 Location update: 39.8975, 4.2573
```

**If you see**:
- ❌ `Location permission not granted` → Grant permission in device settings
- ❌ `SecurityException` → Permission issue, restart app
- No `📍 Location update` → Emulator GPS not working

**Quick test - Manual location**:
1. Extended Controls → Location
2. Enter: Lat `39.900365`, Lon `4.290664`
3. Click **SEND**
4. Check Logcat for `📍 Location update`

**If manual works but GPX doesn't**:
- Android emulator may not support route format
- Try different GPX (route_5.gpx has fewer points)
- Increase speed to 10x

### "GPS file not working in emulator"

**Android**:
- Make sure you clicked **Play ▶** after loading
- Try increasing speed (5x-10x)
- Check file path is correct

**iOS**:
- Make sure app is **running** when you add GPX
- Check Xcode console for errors
- Try simpler location first (Custom Location in Debug menu)

### "Movement too slow/fast"

**Android**: Use speed slider in Extended Controls (10x recommended)
**iOS**: No speed control - emulator follows timestamps (but our files have no timestamps, so it's instant)

## 📚 Additional Resources

- [Android Emulator GPS Guide](https://developer.android.com/studio/run/emulator-commandline#extended-controls)
- [iOS Simulator Location Testing](https://developer.apple.com/documentation/xcode/running-your-app-in-simulator-or-on-a-device)
- [GPX Format Specification](https://www.topografix.com/gpx.asp)
- [Open-Elevation API](https://open-elevation.com/)

## 📝 Notes

- Files generated from official Camí de Cavalls GPX data
- Route format (not track) for maximum emulator compatibility
- No timestamps = emulator controls speed
- Elevation can be added separately when needed
- All 20 stages of the complete 185km trail are included
