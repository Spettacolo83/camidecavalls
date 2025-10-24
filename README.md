# Camí de Cavalls

A modern Kotlin Multiplatform trekking/hiking application for exploring the legendary **Camí de Cavalls** trail in Menorca, Spain. This 185km coastal path is divided into 20 distinct stages, offering hikers an unforgettable journey around the island.

## 📱 Project Overview

**Camí de Cavalls** is a production-ready mobile application designed for both Android and iOS platforms, showcasing best practices in Kotlin Multiplatform development. The app serves as both a **portfolio project** and a **commercial application** targeting local partnerships with Menorcan businesses.

### Key Features (Planned)

- 🗺️ **Interactive Route Maps**: Detailed maps for all 20 stages using Mapbox
- 📍 **GPS Tracking**: Real-time location tracking with background support
- 📴 **Offline Mode**: Download maps and route data for offline use
- 📝 **Notebook**: Track your hiking sessions with detailed statistics (distance, duration, elevation, speed)
- 🏛️ **Points of Interest**: Discover natural, historic, and commercial POIs along the trail
- 🔔 **Proximity Alerts**: Get notified when approaching important points
- 📊 **Statistics**: Comprehensive tracking of your hiking activities
- 🌍 **Multilingual**: Support for multiple languages

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
   - Business entities and models
   - Repository interfaces
   - Use cases (Future)

2. **Data Layer**
   - Repository implementations
   - Local database (SQLDelight)
   - Remote API client (Ktor - Future)
   - Platform-specific implementations (expect/actual)

3. **Presentation Layer** (Future)
   - ViewModels/ScreenModels
   - UI components (Compose Multiplatform)
   - Navigation (Voyager)

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

#### Maps (Android)
- **Mapbox 11.7.1**: Interactive maps (To be configured)

#### Location (Android)
- **Google Play Services Location 21.3.0**: GPS tracking

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

### ✅ Phase 1: Foundation (Current - COMPLETED)
- [x] Project setup and configuration
- [x] Clean Architecture structure
- [x] Domain models (Route, POI, TrackingSession, TrackPoint)
- [x] SQLDelight database implementation
- [x] Repository pattern implementation
- [x] Dependency injection with Koin
- [x] Android build working
- [x] iOS build working

### 📋 Phase 2: Business Logic (Next)
- [ ] Use Cases implementation
  - [ ] Route management (get all, get by ID, get by number)
  - [ ] POI queries (by type, by location, by route)
  - [ ] Tracking session management
  - [ ] Distance calculation (Haversine formula)
- [ ] Data preparation
  - [ ] Create route data with all 20 stages
  - [ ] POI data collection
  - [ ] GPX files preparation (stored locally)

### 🎨 Phase 3: UI/UX
- [ ] Navigation setup with Voyager
- [ ] Home screen with route list
- [ ] Route detail screen
- [ ] Map view with Mapbox
- [ ] Tracking screen (Notebook)
- [ ] POI list and detail screens
- [ ] Settings screen

### 📍 Phase 4: Location & Maps
- [ ] GPS tracking implementation
  - [ ] Android location services
  - [ ] iOS location services
  - [ ] Background tracking
  - [ ] Battery optimization
- [ ] Mapbox integration
  - [ ] Display routes on map
  - [ ] Current location marker
  - [ ] Route polyline rendering
  - [ ] POI markers
- [ ] Offline maps support

### 🔔 Phase 5: Advanced Features
- [ ] Proximity alerts for POIs
- [ ] Route navigation with turn-by-turn
- [ ] Download route data for offline use
- [ ] Statistics and achievements
- [ ] Photo gallery for routes

### 🌐 Phase 6: Backend Integration (V2)
- [ ] Custom backend API (optional)
- [ ] Firebase setup
- [ ] User authentication
- [ ] Cloud data sync
- [ ] Social features
- [ ] Commercial POI integration
- [ ] Analytics

## 📝 Current Status

**Milestone 1 Completed** ✅

- Clean Architecture fully implemented
- Database schema designed and working
- Repository layer complete with reactive Flow
- Dependency injection configured
- Cross-platform compilation successful (Android + iOS)
- Ready for business logic and UI development

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

## 🐛 Known Issues

- Mapbox dependency temporarily commented out (requires Maven repository configuration)
- iOS requires manual addition of `libsqlite3.tbd` in Xcode Build Phases
- Voyager-Koin integration removed due to compatibility issues (using Koin directly)

## 💡 Technical Notes

### Platform-Specific Implementations

**Database Drivers**
- Android: `AndroidSqliteDriver`
- iOS: `NativeSqliteDriver`

**Location Services**
- Android: Google Play Services Location API
- iOS: Core Location Framework (Future)

**Dependency Injection**
- Shared `appModule` in commonMain
- Platform-specific modules using expect/actual pattern

### Performance Optimizations

- Database indexes on frequently queried columns
- Flow-based reactive queries for efficient updates
- Haversine formula for accurate distance calculations
- Foreign key constraints with cascade deletes for data integrity

### Data Management

- All route data stored locally in SQLite database
- GPX files embedded in app or downloaded to local storage
- Optional backend integration for updates (not dependent on external sites)
- Offline-first architecture for reliable operation without internet
