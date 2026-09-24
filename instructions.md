Role & Context:
You are an expert Principal Android Software Engineer specializing in Modern Android Development (MAD), Jetpack Compose, Material 3, Clean Architecture, and offline-first reactive systems. 

Project Goal:
Build a complete, production-ready Android application from scratch that provides precise prayer (Salat) times and the current Hijri date based on the user's geographical location. The application MUST use an offline-first hybrid data engine: primary data is fetched via the AlAdhan REST API, and if network requests fail, time out, or encounter errors, it seamlessly falls back to the local `com.batoulapps.adhan` Kotlin calculation library to compute times on-device.

Technical Specifications & Constraints (STRICT ADHERENCE REQUIRED):
- Target SDK: 37 | Min SDK: 26 (Android 8.0+) | Kotlin: 2.x
- UI Framework: Jetpack Compose with Material 3 (Expressive design patterns)
- Design Language & Aesthetics: Premium Islamic & Arabic visual design language.
  - Dark Theme: Deep Black (`#121212` / `#000000`) background with metallic Golden (`#D4AF37` / `#FFD700`) accents and subtle geometry.
  - Light Theme: Pure/Off-White (`#FAFAFA` / `#FFFFFF`) background with rich Golden (`#C5A059` / `#B8860B`) accents.
  - Geometry & Typography: Elegant Arabic calligraphy/geometric subtle borders and arch-inspired containers for prayer time cards.
- Architecture: Clean Architecture with Unidirectional Data Flow (UDF) using ViewModel, Coroutines, and StateFlow
- Local Persistence: Jetpack DataStore (Preferences) for app settings
- Networking: Retrofit 2 + kotlinx.serialization + OkHttp3
- Location: Google Play Services Location (`FusedLocationProviderClient`)
- Calculation Engine (Offline): `com.batoulapps.adhan:adhan2:1.1.1` (or latest stable release)

STRICT CODE QUALITY & VERIFICATION RULES:
1. Zero Deprecated Code: DO NOT use deprecated APIs or older Compose/Android primitives (e.g., avoid deprecated `MenuAnchorType` variants, old `Accompanist` libraries, or deprecated `PreferenceDataStore` patterns). Use current Android 15 / Compose standards.
2. Zero Assumptions & Zero Hallucinations: Do NOT invent API parameter names, non-existent Android SDK methods, or fake library methods. Verify that every method call exists in standard Android API 35, Jetpack Compose Material 3, Retrofit 2, or the Adhan Kotlin library.
3. Explicit Step Verification: For every file generated, double-check that import paths exist and match current artifact coordinates. Ensure all coroutine flows are non-blocking and safe for UI execution.
4. Custom Color Palette Verification: Verify that Material 3 `ColorScheme` is properly customized for both `lightColorScheme` and `darkColorScheme` to reflect the Black/White + Gold motif without breaking default Material M3 contrast standards.
5. Hardcoded Strings Banned: All user-facing strings must be referenced using resource IDs or clearly organized constant models.

---

### Key Application Features to Implement:

1. Hybrid Prayer Data Engine:
   - Request timings and Hijri calendar data from `https://api.aladhan.com/v1/timings`.
   - On network errors, HTTP errors (e.g., 429 rate limit), or timeouts, catch exceptions cleanly and fallback to `com.batoulapps.adhan` to perform on-device mathematical calculations.
   - Expose an `isOfflineFallback: Boolean` flag in the UI state to display an informative Material 3 banner/SnackBar when offline mode is active.

2. User Preferences & Dynamic Adjustments (DataStore):
   - Calculation Method: Support selecting standard methods (MWL, ISNA, Egyptian, Karachi, Umm Al-Qura). Map user selections accurately to both API method IDs and `com.batoulapps.adhan` parameter models.
   - Asr Juristic Method (Madhab): Toggle between Standard (Shafi/Maliki/Hanbali, ratio 1) and Hanafi (ratio 2).
   - Hijri Date Manual Adjustment: Provide a slider/picker for manual adjustments from -2 to +2 days. Apply this offset dynamically to both API responses and local astronomical date calculations (`java.time.chrono.HijrahDate`).

3. Core UI Screens (Islamic & Gold Aesthetics):
   - Dashboard Screen: Displays location status, formatted current Hijri date, an indicator for online/offline status, and arch-inspired card layouts with gold trims showing all 5 daily prayer times (Fajr, Dhuhr, Asr, Maghrib, Isha) highlighting the next upcoming prayer with a glowing gold state.
   - Settings Screen: Styled Material 3 UI controls (ExposedDropdownMenuBox, RadioButtons, Sliders) allowing real-time adjustment of Calculation Method, Asr Madhab, and Hijri Offset. Changes must update DataStore and immediately re-trigger calculations.

4. Location & Permissions Engine:
   - Modern Compose permission handling for `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`.
   - Provide fallback coordinates (or manual entry) if location permissions are denied.

---

### Step-by-Step Task Execution Plan:

Step 1: Project & Dependency Architecture Setup
- Generate `build.gradle.kts` setup for Android API 35 with Kotlin 2.0+, Jetpack Compose Compiler, Compose Material 3 BOM, DataStore Preferences, Retrofit with kotlinx.serialization, Play Services Location, and BatoulApps Adhan 2 library.

Step 2: Islamic Color Theme & Typography Setup
- Define custom `Theme.kt` and `Color.kt` implementing the Black + Gold (Dark Mode) and White + Gold (Light Mode) color tokens. Ensure Material 3 dynamic color defaults are overridden by this custom aesthetic.

Step 3: Verification of Data & Network Contracts
- Create strict `@Serializable` DTO models for the AlAdhan API endpoint response structure.
- Verify exact Retrofit interface definition and HTTP client configuration.

Step 4: Storage & Offline Calculation Engine Implementation
- Build `UserPreferencesRepository` using Jetpack DataStore to persist and observe user settings.
- Implement `PrayerRepository` containing the network request with fallback logic to `com.batoulapps.adhan`.
- Verify mathematical mapping for calculation methods and Madhabs in both API and offline math engines.

Step 5: State Management Layer
- Create `PrayerViewModel` exposing UI state via `StateFlow<PrayerUiState>`.
- Wire location updates and DataStore setting changes into reactive flows that instantly update prayer schedules.

Step 6: Jetpack Compose UI Construction (Islamic Design Language)
- Build `DashboardScreen` and `SettingsScreen` using modern Material 3 components styled with gold borders, card arches, and custom contrast themes.
- Set up Navigation using `androidx.navigation.compose`.

Step 7: Code Audit & Verification
- Audit every line of generated code to guarantee zero usage of deprecated functions or unverified API calls.
- Verify robust error catching so network drops or missing GPS permissions do not crash the app.

Execute this plan step-by-step and produce the complete, production-ready, non-deprecated source code for all project files.