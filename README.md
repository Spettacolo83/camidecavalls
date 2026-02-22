# Camí de Cavalls

A modern Kotlin Multiplatform trekking/hiking application for exploring the legendary **Camí de Cavalls** trail in Menorca, Spain. This 185km coastal path is divided into 20 distinct stages, offering hikers an unforgettable journey around the island.

## 📱 Project Overview

**Camí de Cavalls** is a production-ready mobile application designed for both Android and iOS platforms, showcasing best practices in Kotlin Multiplatform development. The app serves as both a **portfolio project** and a **commercial application** targeting local partnerships with Menorcan businesses.

### Key Features

**Implemented:**
- 📍 **GPS Tracking**: Real-time location tracking with high accuracy (5s intervals, 5m minimum distance)
- 📡 **Background Tracking**: Reliable GPS tracking that continues when the app is in the background
  - Android: Foreground service with persistent notification, wake lock, and START_STICKY restart recovery
  - iOS: CoreLocation background mode with blue status bar indicator
  - Process death recovery: tracking state persisted to SharedPreferences, automatic session restore on app reopen
- ⏱️ **Live Duration**: Real-time duration counter during tracking, independent from GPS updates
- 📴 **Offline Mode**: Complete offline support - all data stored locally in SQLDelight, GPS works without internet
- 📝 **Notebook**: Track hiking sessions with automatic statistics calculation (distance via Haversine formula, duration, elevation, speed)
- 📄 **Session Details**: View recorded sessions with gradient-colored track visualization (green→yellow→red based on altitude)
- 📤 **GPX Export**: Share recorded sessions as GPX files via system share sheet (Android & iOS)
- 🔐 **Location Permissions**: Smart permission handling with native dialogs for Android and iOS
- 🗺️ **Route Database**: Complete data for all 20 Camí de Cavalls stages with accurate KML data (~185km, ~2,480m elevation gain, ~55 hours)
- 📊 **Session Statistics**: Automatic calculation of distance, speed, elevation gain/loss during tracking
- 🗺️ **Interactive Maps**: MapLibre integration with route visualization, markers, and smooth map interaction on both platforms
- 🎯 **Accurate Route Data**: All 20 routes converted from official KML source with ~130 optimized points per route
- 🧭 **GPS Toggle Button**: Smart GPS following with automatic disable on map gestures (pan, zoom) - works consistently on both Android and iOS
- 🌍 **Multilingual**: Full support for 6 languages (Catalan, Spanish, English, French, German, Italian) with custom localization system, multilingual route descriptions, and localized settings screen
- 📊 **Elevation Charts**: Interactive elevation profiles with tap/drag gestures, centered tooltips, map synchronization, and smooth curve rendering
- ⚙️ **Settings Hub**: Dedicated settings screen with language selection, about page, and contact options
- 🏛️ **Points of Interest**: Complete database of 190 POIs with multilingual content (6 languages) - coastal zones, natural areas, and historic sites with accurate coordinates from official map
- 🧭 **Bottom Navigation Bar**: WeWard-style dark bottom bar with 5 tabs (Map, Routes, POI, Diary, Settings) replacing the drawer menu
- 🗺️ **Complete Route**: Virtual "Complete Route" (185km full loop Maó–Maó) combining all 20 stages with aggregated statistics, elevation profile, and combined GeoJSON track
- 📊 **Session Summary Overlay**: Post-tracking session summary displayed as a dark card overlay on the map with key statistics
- 📱 **Android Immersive Mode**: Full-screen experience hiding system navigation bar with swipe-to-reveal

**Planned:**
- 🔔 **Proximity Alerts**: Get notified when approaching important points

### Business Model

- **V1**: Free app with essential features
- **V2**: Integration with local businesses (restaurants, hotels, shops) for commercial partnerships
- **Future**: Firebase backend for user accounts, social features, and premium content

### Data Strategy

The application is designed to be **independent from external sources**:
- Route data, GPX files, and POI information are stored locally in the app
- Optional backend integration for dynamic content updates
- No dependencies on third-party websites for core functionality
- Data sourced from official Camí de Cavalls resources (camidecavalls.com) but stored and managed independently

## 🏗️ Architecture

The project follows **Clean Architecture** principles with clear separation of concerns:

```
composeApp/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   ├── domain/           # Business logic layer
│   │   │   │   ├── model/        # Domain entities
│   │   │   │   │   ├── Route.kt
│   │   │   │   │   ├── PointOfInterest.kt
│   │   │   │   │   ├── TrackingSession.kt
│   │   │   │   │   └── TrackPoint.kt
│   │   │   │   └── repository/   # Repository interfaces
│   │   │   ├── data/             # Data layer
│   │   │   │   ├── local/        # Database (SQLDelight)
│   │   │   │   └── repository/   # Repository implementations
│   │   │   ├── di/               # Dependency injection (Koin)
│   │   │   └── presentation/     # UI layer (Future)
│   │   └── sqldelight/           # Database schema
│   │       └── com/followmemobile/camidecavalls/database/
│   │           ├── Route.sq
│   │           ├── PointOfInterest.sq
│   │           ├── TrackingSession.sq
│   │           └── TrackPoint.sq
│   ├── androidMain/              # Android-specific code
│   │   └── kotlin/
│   │       ├── data/local/       # Android SQLite driver
│   │       └── di/               # Android DI module
│   └── iosMain/                  # iOS-specific code
│       └── kotlin/
│           ├── data/local/       # iOS SQLite driver
│           └── di/               # iOS DI module
└── iosApp/                       # iOS application wrapper
```

### Layers

1. **Domain Layer** (Pure Kotlin)
   - Business entities and models (Route, POI, TrackingSession, TrackPoint)
   - Repository interfaces
   - Use cases (18 implemented):
     - Route management: GetAllRoutes, GetRouteById, GetRouteByNumber, SaveRoutes, InitializeDatabase
     - POI management: GetAllPOIs, GetPOIsByType, GetPOIsNearLocation, GetPOIsByRoute, SavePOIs
     - Tracking: StartSession, StopSession, AddTrackPoint, CalculateStats, GetActiveSession, GetAllSessions, GetSessionById, DeleteSession
   - Services: LocationService, PermissionHandler

2. **Data Layer**
   - Repository implementations with Flow-based reactive queries
   - Local database (SQLDelight) with 4 tables and optimized indexes
   - LocationService implementations (Android: FusedLocationProvider, iOS: CoreLocation)
   - PermissionHandler implementations (platform-specific dialogs)
   - Platform-specific implementations (expect/actual)

3. **Presentation Layer**
   - MainScreen with bottom navigation bar (5 tabs: Map, Routes, POI, Notebook, Settings)
   - ScreenModels (Voyager): RoutesScreenModel, RouteDetailScreenModel, TrackingScreenModel, NotebookScreenModel, POIsScreenModel, SettingsScreenModel, AboutScreenModel
   - Tab content: MapTabContent (GPS + routes), RoutesTabContent (route list + complete route), POIsTabContent (POI map + filters), NotebookTabContent (session diary), SettingsHubContent (settings hub)
   - Detail screens: RouteDetailScreen, SessionDetailScreen, LanguageSettingsScreen, AboutCamiScreen
   - Interactive components: ElevationChart with Canvas API, WeWardBottomBar, CompletedOverlay
   - RouteSelectionManager for cross-tab route communication
   - Navigation with Voyager (detail screens pushed on top of MainScreen)
   - Material 3 UI components

## 🛠️ Tech Stack

### Core
- **Kotlin 2.1.0**: Programming language
- **Kotlin Multiplatform**: Code sharing across platforms
- **Compose Multiplatform 1.7.3**: Declarative UI framework

### Architecture & Patterns
- **Clean Architecture**: Separation of concerns
- **MVVM Pattern**: Model-View-ViewModel
- **Repository Pattern**: Data access abstraction
- **Expect/Actual**: Platform-specific implementations

### Libraries

#### Dependency Injection
- **Koin 4.0.0**: Lightweight DI framework

#### Database
- **SQLDelight 2.0.2**: Type-safe SQL database
  - 4 tables: Routes, POIs, Tracking Sessions, Track Points
  - Optimized indexes for location queries
  - Foreign keys with cascade deletes

#### Networking (Future)
- **Ktor 3.0.1**: HTTP client for backend API

#### Navigation
- **Voyager 1.1.0-beta02**: Type-safe navigation

#### Async & Reactive
- **Kotlinx Coroutines 1.9.0**: Asynchronous programming
- **Kotlinx Flow**: Reactive streams

#### Serialization
- **Kotlinx Serialization 1.7.3**: JSON serialization
- **Kotlinx DateTime 0.6.1**: Date/time handling

#### Logging
- **Napier 2.7.1**: Multiplatform logging

#### Maps
- **MapLibre Compose 0.0.7**: Interactive maps with native rendering
  - Android: MapLibre Native Android with AndroidView integration
  - iOS: MapLibre Native iOS with UIKitView integration
  - OpenFreeMap tiles for offline-capable map rendering
  - Route path rendering with LineLayer
  - Marker support with CircleLayer
  - Platform-specific MapLayerController with expect/actual pattern

#### Location
- **Google Play Services Location 21.3.0**: GPS tracking (Android)
- **Core Location**: GPS tracking (iOS)

#### Settings
- **Multiplatform Settings 1.2.0**: Key-value storage

## 📊 Database Schema

### RouteEntity
Stores the 20 stages of Camí de Cavalls.

```sql
- id: INTEGER PRIMARY KEY
- number: INTEGER UNIQUE (1-20)
- name: TEXT
- startPoint: TEXT
- endPoint: TEXT
- distanceKm: REAL
- elevationGainMeters: INTEGER
- elevationLossMeters: INTEGER
- maxAltitudeMeters: INTEGER
- minAltitudeMeters: INTEGER
- asphaltPercentage: INTEGER
- difficulty: TEXT (LOW, MEDIUM, HIGH)
- estimatedDurationMinutes: INTEGER
- description: TEXT
- gpxData: TEXT (nullable)
- imageUrl: TEXT (nullable)
```

### PointOfInterestEntity
Stores natural, historic, and commercial POIs.

```sql
- id: INTEGER PRIMARY KEY
- name: TEXT
- type: TEXT (NATURAL, HISTORIC, BEACH, VIEWPOINT, etc.)
- latitude: REAL
- longitude: REAL
- description: TEXT
- images: TEXT (JSON array)
- routeId: INTEGER (nullable, FK to Route)
- isAdvertisement: INTEGER (0/1)
```

Indexes:
- `poi_location_idx` on (latitude, longitude)
- `poi_type_idx` on (type)

### TrackingSessionEntity
Stores user hiking sessions (Notebook feature).

```sql
- id: TEXT PRIMARY KEY
- routeId: INTEGER (nullable, FK to Route)
- startTime: INTEGER (epoch milliseconds)
- endTime: INTEGER (nullable)
- distanceMeters: REAL
- durationSeconds: INTEGER
- averageSpeedKmh: REAL
- maxSpeedKmh: REAL
- elevationGainMeters: INTEGER
- elevationLossMeters: INTEGER
- isCompleted: INTEGER (0/1)
- notes: TEXT
```

### TrackPointEntity
Stores GPS points for each tracking session.

```sql
- id: INTEGER PRIMARY KEY AUTOINCREMENT
- sessionId: TEXT (FK to TrackingSession, CASCADE DELETE)
- latitude: REAL
- longitude: REAL
- altitude: REAL (nullable)
- timestamp: INTEGER (epoch milliseconds)
- speedKmh: REAL (nullable)
```

Indexes:
- `track_point_session_idx` on (sessionId)
- `track_point_timestamp_idx` on (timestamp)

## 🚀 Getting Started

### Requirements

- **Android Studio** Ladybug (2024.2.1) or later
- **Xcode** 15.0 or later (for iOS development)
- **JDK** 11 or later
- **Gradle** 8.10.2 (included via wrapper)

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd CamíDeCavalls
   ```

2. **Open in Android Studio**
   - File → Open → Select project directory
   - Wait for Gradle sync to complete

3. **For iOS development (macOS only)**
   - Ensure Xcode is installed
   - Open `iosApp/iosApp.xcodeproj` in Xcode
   - **Important**: Add `libsqlite3.tbd` to "Link Binary With Libraries" in Build Phases
   - Build the project

### Running the Application

#### Android
1. Open project in Android Studio
2. Select `composeApp` run configuration
3. Click Run (or press ⌃R)

#### iOS
**Option 1: From Xcode**
1. Build Kotlin framework:
   ```bash
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Select simulator or device
4. Click Run (or press ⌘R)

**Option 2: From Terminal**
```bash
# For simulator
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# For device
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

### Building for Production

#### Android
```bash
./gradlew :composeApp:assembleRelease
```
APK location: `composeApp/build/outputs/apk/release/`

#### iOS
Build in Xcode with Release configuration for App Store distribution.

## 🗓️ Development Roadmap

### ✅ Milestone 1: Foundation & Architecture (COMPLETED)
- [x] Project setup and configuration
- [x] Clean Architecture structure
- [x] Domain models (Route, POI, TrackingSession, TrackPoint)
- [x] SQLDelight database implementation with 4 tables
- [x] Repository pattern implementation with Flow
- [x] Dependency injection with Koin 4.0.0
- [x] Android build working
- [x] iOS build working

### ✅ Milestone 2: Business Logic & Data (COMPLETED)
- [x] Use Cases implementation (18 total)
  - [x] Route management: GetAll, GetById, GetByNumber, Save, InitializeDatabase
  - [x] POI queries: GetAll, ByType, NearLocation, ByRoute, Save
  - [x] Tracking: Start, Stop, AddPoint, CalculateStats, GetActive, GetAll, GetById, Delete
- [x] Route data preparation
  - [x] All 20 Camí de Cavalls stages with realistic data
  - [x] Database initialization on first app launch
  - [x] Offline-first data strategy
- [x] TrackingManager with battery optimization

### ✅ Milestone 3: Core UI & Navigation (COMPLETED)
- [x] Navigation setup with Voyager
- [x] HomeScreen with route list (Material 3 cards)
- [x] RouteDetailScreen with complete route information
- [x] TrackingScreen (Notebook) with real-time GPS
- [x] Location permission handling (Android & iOS)

### ✅ Milestone 4: GPS Tracking & Permissions (COMPLETED)
- [x] GPS tracking implementation
  - [x] Android LocationService (FusedLocationProvider)
  - [x] iOS LocationService (CoreLocation)
  - [x] Battery optimization (5s intervals, 5m min distance, balanced accuracy)
  - [x] Offline tracking (no internet required)
- [x] Permission handling
  - [x] Android runtime permissions with ActivityResultContracts
  - [x] iOS authorization dialogs
  - [x] Smart permission flow in UI
- [x] TrackingManager
  - [x] Automatic track point recording
  - [x] Real-time statistics calculation (Haversine formula)
  - [x] Session state management (Idle, Tracking, Completed, Error)

### ✅ Milestone 5: Interactive Maps & Route Visualization (COMPLETED)
- [x] MapLibre integration
  - [x] MapLibre Compose integration on both platforms
  - [x] Platform-specific implementations (AndroidView, UIKitView)
  - [x] OpenFreeMap tile provider for offline-capable maps
  - [x] MapLayerController with expect/actual pattern
- [x] Route visualization
  - [x] GeoJSON LineString rendering with LineLayer
  - [x] Route path with colored lines and white casing
  - [x] Start/end markers with CircleLayer (green for start, red for end)
  - [x] Smart camera positioning with automatic zoom calculation
  - [x] Bounding box calculation to fit entire route
  - [x] Aspect-ratio aware zoom for balanced framing
- [x] Accurate route data
  - [x] All 20 routes converted from official KML source
  - [x] Coordinate simplification (~130 points per route)
  - [x] Fixed Route 11 (Ciutadella - Cap d'Artrutx) with correct segment order
  - [x] Database versioning with AppPreferences for automatic data updates
- [x] Map interactions
  - [x] Pan, zoom, and rotate gestures on both platforms
  - [x] Fixed Android scroll conflict with requestDisallowInterceptTouchEvent
  - [x] Smooth map interaction without parent scroll interference
  - [x] Map preview in RouteDetailScreen (350dp height, rounded corners)
  - [x] Equal margins for both vertical (N-S) and horizontal (E-W) routes

### ✅ Milestone 7: POI System (COMPLETED)
- [x] POI data collection (190 POIs from official website)
- [x] Multilingual scraping (6 languages: CA, ES, EN, DE, FR, IT)
- [x] Coordinate correction (UTM to WGS84 conversion)
- [x] POI database with versioning system
- [x] POI categories: Coastal zones, Natural areas, Historic sites

### ✅ Milestone 6: Elevation Charts & Settings (COMPLETED)
- [x] Interactive elevation charts
  - [x] ElevationChart component with Canvas API
  - [x] GeoJSON parsing and elevation data extraction
  - [x] Haversine distance calculation for cumulative distance
  - [x] Smooth curve rendering with quadratic Bezier curves
  - [x] Interactive tap and drag gestures for point selection
  - [x] Centered tooltip with elevation and distance info
  - [x] Transparent overlay for chart visibility
  - [x] Map marker synchronization
- [x] Settings screen
  - [x] In-app language selection (6 languages)
  - [x] Settings localization with custom LocalizedStrings
  - [x] Language persistence across app launches

### ✅ Milestone 8: Bottom Navigation & UI Restructuring (COMPLETED)
- [x] Bottom navigation bar (WeWard-style dark theme)
  - [x] 5 tabs: Map, Routes, POI, Diary, Settings
  - [x] Dark background (#1C1C2E) with rounded top corners
  - [x] Blue accent for selected tab, gray for unselected
  - [x] Variable weight layout for proportional label sizing
  - [x] Auto-hide during active GPS tracking
- [x] MainScreen as root with tab-based navigation
  - [x] RouteSelectionManager for cross-tab route communication
  - [x] Detail screens pushed on top (bottom bar hidden)
- [x] Complete Route feature
  - [x] Virtual 185km full loop (Maó–Maó) combining all 20 stages
  - [x] Aggregated statistics (elevation, duration, difficulty)
  - [x] Combined GeoJSON track from all route segments
  - [x] Multilingual descriptions in 6 languages
- [x] Settings hub with language, about, and contact options
- [x] Session summary overlay (dark card on map after tracking)
- [x] Android immersive mode (hidden system navigation bar)
- [x] Drawer menu fully replaced and removed

### 🔔 Future: Advanced Features
- [ ] Proximity alerts for POIs
- [ ] Route navigation with turn-by-turn
- [ ] Statistics and achievements
- [ ] Photo gallery for routes

### 🌐 Future: Backend Integration (V2)
- [ ] Custom backend API (optional)
- [ ] Firebase setup
- [ ] User authentication
- [ ] Cloud data sync
- [ ] Social features
- [ ] Commercial POI integration
- [ ] Analytics

## 📝 Current Status

**Milestones 1-8 Completed** ✅

The app is now fully functional with core trekking features, interactive maps, elevation charts, settings, complete POI database, and bottom navigation bar:

**Architecture & Foundation:**
- Clean Architecture fully implemented across 3 layers
- SQLDelight database with 4 tables and optimized indexes
- Repository pattern with Flow-based reactive queries
- Koin 4.0.0 dependency injection (platform-specific modules)
- Cross-platform compilation verified (Android + iOS)
- AppPreferences system for database versioning

**Business Logic:**
- 18 use cases implemented and tested
- Accurate route data for all 20 Camí de Cavalls stages (~185km)
- All routes converted from official KML source
- TrackingManager with battery-optimized GPS tracking
- Haversine formula for accurate distance calculation
- Automatic statistics calculation (distance, speed, elevation)

**User Interface:**
- Material 3 design system
- Voyager navigation (type-safe, without voyager-koin)
- Bottom navigation bar with 5 tabs (Map, Routes, POI, Diary, Settings)
  - WeWard-style dark theme with blue accent for selected tab
  - Variable weight layout for proportional label sizing
  - Auto-hides during active GPS tracking for maximum map space
- MainScreen as root with tab-based content switching
- RoutesTabContent with route list cards + complete route (185km Maó–Maó)
- RouteDetailScreen with complete stage information, map preview, and elevation chart
- MapTabContent (formerly TrackingScreen) with real-time GPS display
- Settings hub with language selection, about page, and contact options
- Session summary overlay: dark card on map after tracking with key statistics
- ElevationChart component with interactive visualization
- RouteSelectionManager for cross-tab route communication
- Android immersive mode hiding system navigation bar
- Smart location permission handling

**GPS Tracking:**
- Android: FusedLocationProviderClient with high accuracy in foreground service
- iOS: CoreLocation with CLActivityTypeFitness and background mode
- Background tracking: Foreground service (Android) / background location updates (iOS)
- Process death recovery: SharedPreferences persistence + START_STICKY + automatic session restore
- GPS accuracy filtering: first-fix (30m threshold) + ongoing (50m threshold)
- Elevation dead band: 3m threshold to filter GPS altitude noise in statistics
- Offline-first: All data saved locally in SQLDelight
- Configurable intervals (5s default) and accuracy (high)
- Pause detection and session management
- GPS toggle button: Automatic disable on map gestures (drag, zoom), manual toggle support
- Platform-specific gesture detection (MLNMapViewDelegate on iOS, CameraMoveListener on Android)
- Emulator/simulator support with relaxed filters for development testing

**Interactive Maps:**
- MapLibre integration with native rendering on both platforms
- OpenFreeMap tiles for offline-capable map display
- Route visualization with colored LineLayer and white casing
- Start/end markers with CircleLayer (green/red)
- Smart camera positioning with bounding box calculation
- Aspect-ratio aware zoom for balanced route framing
- Equal margins for vertical (N-S) and horizontal (E-W) routes
- Smooth pan, zoom, and rotate gestures
- Fixed scroll conflicts on Android with requestDisallowInterceptTouchEvent
- Map preview in RouteDetailScreen (350dp height, rounded corners)
- Platform-specific implementations (AndroidView, UIKitView)

**Elevation Charts:**
- Interactive elevation profile component with Compose Canvas API
- GeoJSON parsing for elevation data extraction
- Haversine formula for accurate cumulative distance calculation
- Smooth curve rendering using quadratic Bezier curves
- Interactive tap and drag gestures for point selection
- Centered tooltip with elevation and distance information
- Transparent overlay (70% opacity) for chart visibility
- Real-time map marker synchronization on point selection
- Positioned below map for optimal user experience

**Settings & Localization:**
- Settings screen with Material 3 design
- In-app language selection (6 languages: CA, ES, EN, DE, FR, IT)
- Custom LocalizedStrings system for app-controlled localization
- Language persistence across app launches
- Full settings localization (titles, language names)
- Independent from system language settings

**Points of Interest (POI):**
- Complete database of 190 POIs from official Camí de Cavalls website
- Multilingual content in 6 languages (CA, ES, EN, DE, FR, IT)
- Three POI categories with color-coded badges:
  - Coastal zones (blue): Beaches, coves, coastal areas
  - Natural areas (green): Protected spaces, flora, fauna
  - Historic sites (red): Towers, lighthouses, prehistoric sites
- Accurate coordinates corrected from official map (UTM Zone 31N → WGS84)
- POI versioning system for automatic updates
- Custom scraping tools for data collection and coordinate validation
- Fixed 16 POIs with coordinate errors > 800m (up to 8.3 km corrections)

**Ready for:** Proximity alerts, route navigation, and advanced features

## 🤝 Contributing

This is currently a portfolio/commercial project. Contributions are not being accepted at this time.

## 📄 License

Copyright © 2024 Stefano Russello. All rights reserved.

This project is private and proprietary. No license is granted for use, modification, or distribution.

## 👨‍💻 Author

**Stefano Russello**
- Portfolio Project
- Target: Production app + Commercial partnerships in Menorca

---

## 📚 Resources

- [Camí de Cavalls Official Website](https://camidecavalls.com)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Koin Documentation](https://insert-koin.io/)
- [MapLibre](https://maplibre.org/) - Open-source mapping platform
- [MapLibre Compose](https://github.com/Rallista/maplibre-compose-playground) - MapLibre integration for Compose Multiplatform
- [OpenFreeMap](https://openfreemap.org/) - Free tile provider

## 🐛 Known Issues

- **iOS SQLite**: Requires manual addition of `libsqlite3.tbd` in Xcode Build Phases → Link Binary With Libraries
  - This may need to be re-added after clean builds or Xcode updates
- **iOS Altitude Simulation**: iOS Simulator ignores elevation data from GPX files (Apple limitation)
  - Altitude-based features can only be tested on real iOS devices
  - Android emulator correctly simulates altitude from GPX files
- **Voyager-Koin**: Integration removed due to compatibility issues (using Koin directly with parametersOf)
- **iOS Map UIKitView**: Uses deprecated UIKitView API (newer API available but current version stable)

## 🔧 Fixed Issues

- **GPS Toggle Button on iOS** (Fixed): GPS following now correctly disables on all user gestures (was only working on first gesture)
  - Root cause: Delegate loss during Compose recomposition
  - Solution: Persistent delegate using `remember` + `update` block restoration
  - See GPS_TOGGLE_FEATURE.md for detailed technical documentation

## 💡 Technical Notes

### Platform-Specific Implementations

**Database Drivers**
- Android: `AndroidSqliteDriver` with Context
- iOS: `NativeSqliteDriver` (requires libsqlite3.tbd)

**Location Services**
- Android: `FusedLocationProviderClient` (Google Play Services)
  - High accuracy with FINE granularity for GPS-level precision
  - LocationRequest with 5s interval and 2s minimum interval
  - Foreground service (`LocationForegroundService`) owns GPS and writes track points directly to DB
  - SharedPreferences persistence for process death recovery via START_STICKY
  - Static StateFlow for service-to-app communication
- iOS: `CLLocationManager` (Core Location)
  - CLActivityTypeFitness for hiking optimization
  - pausesLocationUpdatesAutomatically = false (never pauses during active tracking)
  - Time-based throttling in delegate callback (CoreLocation has no native time interval)
  - desiredAccuracy and distanceFilter configuration (min 5m)

**Permission Handling**
- Android: `ActivityResultContracts.RequestMultiplePermissions()`
  - rememberLauncherForActivityResult in Composables
  - ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION
- iOS: `CLLocationManager.requestWhenInUseAuthorization()`
  - Polling-based permission status check
  - Info.plist entries for usage descriptions

**Dependency Injection**
- Shared `appModule` in commonMain
- Platform-specific modules using expect/actual pattern
- Android: androidContext() for Context injection
- iOS: KoinApplication wrapper in @Composable
- AppPreferences injection for database versioning

**Map Integration**
- MapLibre Native with platform-specific rendering
- Android: `MapView` wrapped in `AndroidView` Composable
  - `MapLibreMap` for map instance management
  - `GeoJsonSource` for route data
  - `LineLayer` for route paths with casing
  - `CircleLayer` for start/end markers
  - Color.parseColor() for hex color support
  - `addOnCameraMoveStartedListener` for gesture detection
- iOS: `MLNMapView` wrapped in `UIKitView` Composable
  - `MLNShapeSource` for route data
  - `MLNLineStyleLayer` for route paths
  - `MLNCircleStyleLayer` for markers
  - NSData conversion for GeoJSON handling
  - `MLNMapViewDelegateProtocol` with persistent delegate pattern
- MapLayerController: expect/actual pattern for platform abstraction
- OpenFreeMap tiles: Free, offline-capable tile provider
- Gesture handling: Fixed scroll conflicts with pointerInput on Android
- Delegate persistence: Using `remember` and `update` block to prevent iOS delegate loss during recomposition

### Performance Optimizations

**Database:**
- Indexes on frequently queried columns (location lat/lng, session timestamps)
- Flow-based reactive queries for efficient real-time updates
- Foreign key constraints with cascade deletes for data integrity
- Prepared statements via SQLDelight for query optimization

**GPS Tracking:**
- High-accuracy update intervals (5s default, min 2s)
- Minimum distance filter (5m) to avoid excessive updates
- GPS accuracy filtering: first-fix (30m) + ongoing (50m) thresholds
- Elevation dead band (3m) to prevent GPS altitude noise from inflating statistics
- O(1) direct track point inserts (replaced O(n) load-all/delete-all/re-insert pattern)
- Emulator/simulator detection for relaxed GPS filters during development

**Calculations:**
- Haversine formula for accurate GPS distance calculations
- Incremental statistics updates (not recalculating entire session)
- Efficient coordinate transformations for map projections
- Smart zoom calculation with logarithmic formula for route fitting
- Bounding box analysis with padding factor for optimal route visibility
- Aspect-ratio compensation for balanced map framing

### Data Management

- All route data stored locally in SQLite database
- GPX files embedded in app or downloaded to local storage
- Optional backend integration for updates (not dependent on external sites)
- Offline-first architecture for reliable operation without internet
