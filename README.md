# Camí de Cavalls

A Kotlin Multiplatform application targeting Android and iOS.

## Tech Stack

- **Kotlin Multiplatform**: Shared business logic across platforms
- **Compose Multiplatform**: UI framework for Android and iOS
- **Gradle**: Build system with version catalogs

## Requirements

- Android Studio Ladybug or later
- Xcode 15.0 or later (for iOS development)
- JDK 11 or later
- Kotlin 2.1.0

## Project Structure

```
├── composeApp/              # Shared code and UI
│   ├── src/
│   │   ├── androidMain/     # Android-specific code
│   │   ├── iosMain/         # iOS-specific code
│   │   └── commonMain/      # Shared code
├── iosApp/                  # iOS application wrapper
└── gradle/                  # Gradle configuration
```

## Running the Application

### Android
Open the project in Android Studio and run the `composeApp` configuration.

### iOS
1. Build the Kotlin framework: `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Run the app on simulator or device

## Author

Stefano Russello
