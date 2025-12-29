# 📱 Network Operator Detector

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)

A production-quality Android application that detects mobile network operators and monitors internet connection states in real-time. Built with **Clean Architecture**, **MVVM**, **Jetpack Compose**, and **Hilt** for dependency injection.

## ✨ Features

- 📡 **Multi-SIM Detection** - Detects all SIM cards (SIM 1, SIM 2, etc.) with detailed operator information
- 🌐 **Active Connection Detection** - Identifies which connection (Wi-Fi or Mobile Data) is currently active
- 🔄 **Real-time Monitoring** - Automatically updates when network state changes
- 🌍 **Roaming Detection** - Shows roaming status for each SIM card
- 📊 **Network Type Display** - Shows network generation (2G, 3G, 4G LTE, 5G NR)
- 🎨 **Material Design 3** - Modern UI with Jetpack Compose
- 🔒 **Permission Handling** - Clean runtime permission flow with user-friendly explanations
- 📝 **Comprehensive Logging** - Structured Logcat output for debugging

## 🎯 Key Information Displayed

### SIM Card Details
- Carrier/Operator name (e.g., "Beeline", "Ucell", "T-Mobile")
- Slot index (SIM 1, SIM 2, etc.)
- Phone number (if available)
- Country ISO code
- Network operator code (MCC+MNC)
- Roaming status
- eSIM indicator
- Subscription ID

### Connection Information
- Active connection type (Wi-Fi or Mobile Data)
- For mobile data: Which SIM is providing internet
- Network technology (2G/3G/4G/5G)
- Roaming status with visual warning
- Real-time connection changes

## 📸 Screenshots

> Coming soon...

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│         Presentation Layer (UI)             │
│  - Jetpack Compose UI                       │
│  - ViewModels (MVVM)                        │
│  - UI State Management                      │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│            Domain Layer                     │
│  - Use Cases (Business Logic)               │
│  - Domain Models                            │
│  - Repository Interfaces                    │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│            Data Layer                       │
│  - Repository Implementations               │
│  - Data Sources (Android APIs)              │
│  - Mappers                                  │
└─────────────────────────────────────────────┘
```

### Architecture Highlights

- **MVVM Pattern** - ViewModel manages UI state, View observes via StateFlow
- **Clean Architecture** - Domain layer is independent of Android framework
- **Use Cases** - Encapsulate business logic (e.g., filtering invalid SIMs, sorting)
- **Repository Pattern** - Abstract data access from business logic
- **Dependency Injection** - Hilt for compile-time DI with testability

## 🛠️ Tech Stack

### Core
- **Kotlin** 2.0.21 - Modern, concise, and safe programming language
- **Jetpack Compose** - Declarative UI toolkit
- **Material Design 3** - Latest Material Design components

### Architecture Components
- **ViewModel** - Manage UI-related data lifecycle-aware
- **Lifecycle** - Handle Android lifecycle changes
- **StateFlow/Flow** - Reactive state management and data streams

### Dependency Injection
- **Hilt** 2.52 - Dependency injection built on Dagger

### Coroutines
- **Kotlin Coroutines** - Asynchronous programming
- **Flow** - Reactive data streams

### Testing
- **JUnit 4** - Unit testing framework
- **MockK** - Mocking library for Kotlin
- **kotlinx-coroutines-test** - Testing coroutines and Flows
- **Turbine** - Flow testing library
- **Espresso** - UI testing
- **Hilt Testing** - Testing with dependency injection

### Android APIs Used
- **TelephonyManager** - Access telephony services
- **SubscriptionManager** - Manage multiple SIM subscriptions
- **ConnectivityManager** - Monitor network connectivity
- **NetworkCapabilities** - Query network capabilities and transport

## 📁 Project Structure

```
uz.jahonov.defineoperator/
├── di/                          # Dependency Injection (Hilt modules)
│   ├── AppModule.kt
│   └── NetworkModule.kt
├── domain/                      # Domain Layer (Business Logic)
│   ├── model/                   # Domain models
│   ├── repository/              # Repository interfaces
│   └── usecase/                 # Use cases
├── data/                        # Data Layer
│   ├── source/                  # Data sources (Android APIs)
│   ├── mapper/                  # Data to domain mappers
│   └── repository/              # Repository implementations
├── presentation/                # Presentation Layer
│   ├── viewmodel/               # ViewModels
│   ├── ui/                      # Compose UI components
│   └── state/                   # UI state classes
└── util/                        # Utilities (Logger, etc.)
```

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK** 11 or later
- **Android SDK** with minimum API level 24 (Android 7.0)
- **Gradle** 8.13.2 (included in wrapper)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/DefineOperatorTest.git
   cd DefineOperatorTest
   ```

2. **Open in Android Studio**
   - File → Open → Select the project directory
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on device/emulator**
   - Connect an Android device or start an emulator
   - Click Run (▶️) in Android Studio
   - Or use command: `./gradlew installDebug`

### Required Permissions

The app requires the following permissions (requested at runtime):

- `READ_PHONE_STATE` - Required to access SIM card information
- `ACCESS_NETWORK_STATE` - Required to read network connection state
- `ACCESS_WIFI_STATE` - Required to read Wi-Fi connection details

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- **Unit Tests**: Domain layer (use cases, mappers)
- **Instrumentation Tests**: Data sources, UI components
- **Mocking**: MockK for clean unit tests
- **Coroutine Testing**: kotlinx-coroutines-test for Flow testing

## 📊 How It Works

### Multi-SIM Detection
1. Uses `SubscriptionManager.getActiveSubscriptionInfoList()` to retrieve all active SIM cards
2. Each SIM has a unique `subscriptionId` and physical `slotIndex`
3. Creates per-SIM `TelephonyManager` via `createForSubscriptionId()` for detailed info
4. Maps Android framework classes to domain models

### Active Connection Detection
1. `ConnectivityManager.getActiveNetwork()` gets the currently active network
2. `NetworkCapabilities` determines transport type (Wi-Fi or Cellular)
3. For cellular: `SubscriptionManager.getDefaultDataSubscriptionId()` identifies which SIM
4. Real-time updates via `NetworkCallback` converted to Kotlin Flow

### Roaming Detection
1. Gets subscription-specific `TelephonyManager`
2. Calls `isNetworkRoaming` which reads from cellular modem
3. Returns true if registered on a roaming network

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Write KDoc comments for all public APIs
- Include unit tests for new features
- Ensure all tests pass before submitting PR

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

```
Copyright 2024 Asilbek Jahonov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 👨‍💻 Author

**Asilbek Jahonov**

- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)
- Email: your.email@example.com

## 🙏 Acknowledgments

- Android Open Source Project for excellent documentation
- Jetpack Compose team for the modern UI toolkit
- Hilt team for dependency injection framework
- The Android developer community

## 📝 Notes

- This is a test/educational project demonstrating Android telephony and network APIs
- Tested on devices with Android 7.0 (API 24) to Android 14 (API 34)
- Dual-SIM support tested on devices with 2+ SIM slots
- eSIM detection requires Android 9.0+ (API 28+)

## 🐛 Known Issues

None currently. Please report issues in the [Issues](https://github.com/yourusername/DefineOperatorTest/issues) section.

## 🗺️ Roadmap

- [ ] Add screenshot/video demo
- [ ] Support for more network types (VPN, Ethernet, Bluetooth)
- [ ] Historical network data tracking
- [ ] Export network data to CSV/JSON
- [ ] Widget for home screen
- [ ] Dark mode enhancements
- [ ] Localization (multiple languages)

---

**Star ⭐ this repository if you found it helpful!**
