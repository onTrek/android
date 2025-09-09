# OnTrek

A comprehensive hiking and trekking application for Android devices and Wear OS smartwatches that helps you navigate trails, track your progress, and stay connected during outdoor adventures.

## Overview

OnTrek is a modern Android application built with Jetpack Compose that provides hiking and trekking functionality across multiple form factors:

- **Mobile App**: Full-featured Android application for smartphones
- **Wear OS App**: Companion smartwatch application for hands-free tracking
- **Shared Module**: Common business logic and data models

## Features

### Mobile App
- Authentication and user management
- Hike goups management
- Track management
- Modern Material Design 3 UI
- Real-time location services

### Wear OS App
- Track navigations
- Track start/stop controls
- Radar and proximity features
- Help requests and follow friends
- Optimized for small screens
- Standalone watch functionality

## Project Structure

```
onTrek/
├── mobile/          # Android mobile application
├── wear/            # Wear OS smartwatch application
├── shared/          # Shared code and business logic
├── build.gradle.kts # Root build configuration
└── settings.gradle.kts # Project settings
```

## Requirements

- **Android SDK**: API level 33+ (Android 13)
- **Wear OS**: API level 33+
- **Java**: Version 11 or higher
- **Gradle**: 8.14+ (included via wrapper)

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/onTrek/android.git
   cd android
   ```

2. **Open in Android Studio**
   - Import the project in Android Studio
   - Let Gradle sync complete
   - Ensure you have the latest Android SDK and Wear OS SDK components

3. **Build the project**
   ```bash
   ./gradlew build
   ```

## Running the Apps

### Mobile App
```bash
./gradlew :mobile:installDebug
```

### Wear OS App
```bash
./gradlew :wear:installDebug
```

## Development

### Architecture
- **MVVM Pattern**: Using ViewModels and LiveData/StateFlow
- **Jetpack Compose**: Modern declarative UI framework
- **Navigation Component**: Type-safe navigation
- **Room Database**: Local data persistence
- **Retrofit**: Network communication
- **Coroutines**: Asynchronous programming

### Key Technologies
- Kotlin
- Jetpack Compose
- Wear Compose
- Material Design 3
- Android Architecture Components
- Google Play Services (Location, Wearable)

### Building
The project uses Gradle with version catalogs for dependency management. All dependencies are defined in `gradle/libs.versions.toml`.

```bash
# Clean build
./gradlew clean

# Build all modules
./gradlew build

# Run tests
./gradlew test

# Generate APKs
./gradlew assembleDebug
```

## Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION` - For GPS tracking and navigation
- `ACCESS_COARSE_LOCATION` - For general location services
- `INTERNET` - For network communication
- `POST_NOTIFICATIONS` - For tracking notifications
- `VIBRATE` - For haptic feedback on Wear OS
- `WAKE_LOCK` - For keeping device awake during tracking

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly on both mobile and wear devices
5. Submit a pull request
