# Architecture — ReporteCiudadano

Authored by: KMP Architect | Last updated: 2026-05-23

## App Summary

ReporteCiudadano allows citizens to photograph and geo-locate potholes, submit them as citizen reports, review their own submissions, and see all reports on a map.

---

## Domain Model

```
CitizenReport
  id            : String          // UUID
  title         : String
  description   : String
  photos        : List<ReportPhoto>
  location      : GeoLocation     // from EXIF of first valid photo
  address       : String          // reverse-geocoded at submission time
  status        : ReportStatus
  createdAt     : Long            // epoch millis

ReportPhoto
  localPath     : String          // file path after capture
  exifLocation  : GeoLocation?    // null if EXIF unavailable

GeoLocation
  latitude      : Double
  longitude     : Double

ReportStatus (enum)
  SENT | SEEN | PENDING | IN_PROGRESS | RESOLVED | DISCARDED
```

---

## Feature Sequence

One branch per feature. Never develop two features in parallel.

| # | ID | Feature | Branch |
|---|---|---|---|
| 1 | FEAT-001 | App Shell & Navigation | `feature/feat-001-app-shell` |
| 2 | FEAT-002 | Camera & Photo Capture | `feature/feat-002-camera` |
| 3 | FEAT-003 | Report Form & Submission | `feature/feat-003-report-form` |
| 4 | FEAT-004 | Thank You Screen | `feature/feat-004-thank-you` |
| 5 | FEAT-005 | My Reports List | `feature/feat-005-my-reports` |
| 6 | FEAT-006 | Report Detail (read-only) | `feature/feat-006-report-detail` |
| 7 | FEAT-007 | Reports Map | `feature/feat-007-reports-map` |
| 8 | FEAT-008 | Spanish Translations | `feature/feat-008-spanish-translations` |
| 9 | FEAT-009 | Light/Dark Color Theme | `feature/feat-009-color-theme` |
| 10 | FEAT-010 | Location Service Gate | `feature/feat-010-location-service-gate` |

---

## Repository Interfaces (domain layer — commonMain)

```kotlin
interface ReportRepository {
    suspend fun save(report: CitizenReport): Result<Unit>
    suspend fun getAll(): Result<List<CitizenReport>>
    suspend fun getById(id: String): Result<CitizenReport>
}

interface GeocodingRepository {
    suspend fun reverseGeocode(location: GeoLocation): Result<String>
}
```

Camera and location are `expect/actual` — no repository interface; platform implementations
are injected directly into UseCases via Koin.

---

## Library Assignments

| Library | Role |
|---|---|
| **SQLDelight** | `ReportRepositoryImpl` — stores all reports locally |
| **Ktor Client** | `GeocodingRepositoryImpl` + future remote submission endpoint |
| **kotlinx.serialization** | Ktor response DTOs; never exposed past the data layer |
| **Koin** | One `Module` per feature, loaded at app startup |

---

## Platform-Specific Boundaries

| Concern | Android | iOS | Web |
|---|---|---|---|
| Camera + EXIF | CameraX / ExifInterface | AVFoundation / CLLocation | getUserMedia + exif-js |
| Reverse geocoding | `android.location.Geocoder` | `CLGeocoder` | Nominatim via Ktor |
| Maps | Google Maps Compose | MapKit (SwiftUI) | Leaflet via kotlin-wrappers |
| Location permission | FusedLocationProviderClient | CoreLocationManager | navigator.geolocation |

Each concern is wrapped behind an `expect fun` / `actual` in `commonMain` / platform source sets.

---

## Layer Structure per Feature

```
presentation/
  <Feature>Screen.kt       — stateless Composable
  <Feature>ViewModel.kt    — StateFlow<State> + processIntent()
  <Feature>State.kt        — immutable data class
  <Feature>Intent.kt       — sealed class

domain/
  model/                   — pure Kotlin (no platform imports)
  repository/              — interfaces only
  usecase/                 — one class, one suspend invoke()

data/
  repository/              — implementations (SQLDelight / Ktor)
  datasource/
    local/                 — SQLDelight queries
    remote/                — Ktor API client
```

---

## Initial Module Strategy

All features in `:shared` (commonMain) for the initial release.
Promote to `:feature:<name>` modules when the team grows or artifact splitting is needed.

---

## FEAT-008 — Spanish Translations (Localization Architecture)

### Resource System

Compose Multiplatform's built-in resource framework (`org.jetbrains.compose.components:components-resources`) is already declared in `commonMain.dependencies` in `shared/build.gradle.kts`. No new library or Gradle configuration is required.

String resources live under `shared/src/commonMain/composeResources/` using the standard Compose Multiplatform locale qualifier convention:

```
shared/src/commonMain/composeResources/
  values/
    strings.xml          ← default / English (en) fallback
  values-es/
    strings.xml          ← Spanish (es) overrides
```

The build plugin generates a type-safe `Res.string.*` accessor object at compile time. All composables access strings via:

```kotlin
import org.jetbrains.compose.resources.stringResource
import com.espert.reporteciudadano.shared.generated.resources.Res
import com.espert.reporteciudadano.shared.generated.resources.*

Text(stringResource(Res.string.tab_report))
```

No new Gradle submodule is created. This is entirely a resource + UI change within `:shared`.

### Locale Detection Strategy

The Compose Multiplatform resource framework resolves the active locale automatically:

| Platform | Locale source | Wiring needed |
|---|---|---|
| Android | System locale via `LocalConfiguration` | None — CMP picks it up |
| iOS | `NSLocale.currentLocale` via the Kotlin/Native bridge | None — CMP picks it up |
| JS / WasmJS | `navigator.language` resolved by the CMP JS resource loader | None — CMP picks it up |

No `expect/actual` or platform-specific code is needed for this feature. If a future requirement adds a user-selectable language override (overriding system locale in-app), that would require a `LocaleController expect/actual` — deferred to a future feature.

### String Key Inventory

All string keys use snake_case. The `values/strings.xml` file is the English source of truth; `values-es/strings.xml` must contain the same set of keys translated to Spanish.

#### Shell / Navigation (MainScreen)

| Key | English value |
|---|---|
| `tab_report` | Report |
| `tab_my_reports` | My Reports |
| `tab_map` | Map |
| `report_tab_headline` | Report a Pothole |
| `register_pothole_button` | Register pothole |

#### Camera (CameraScreen)

| Key | English value |
|---|---|
| `location_required_title` | Location Required |
| `location_required_body` | Location access is needed to tag photos with GPS coordinates. Please enable location in Settings. |
| `camera_required_title` | Camera Required |
| `camera_required_body` | Camera access is needed to take photos for the report. Please enable camera in Settings. |
| `cancel_button` | Cancel |
| `photo_taken_dialog_title` | Photo taken |
| `photo_taken_dialog_body` | What would you like to do? |
| `keep_taking_button` | Keep taking |
| `retake_button` | Retake |
| `complete_button` | Complete |

#### Photo Review (PhotoReviewScreen)

| Key | English value |
|---|---|
| `review_photos_title` | Review Photos |
| `no_photos_message` | No photos taken yet. |
| `continue_button` | Continue |
| `cancel_content_description` | Cancel |
| `photo_content_description` | Photo |

#### Report Form (ReportFormScreen)

| Key | English value |
|---|---|
| `new_report_title` | New Report |
| `address_dialog_title` | About the address |
| `address_dialog_body` | The address shown may be approximate based on the photo's GPS coordinates. |
| `got_it_button` | Got it |
| `title_field_label` | Title * |
| `description_field_label` | Description * |
| `submit_report_button` | Submit Report |
| `cancel_content_description` | Cancel |

Note: `cancel_button` declared in CameraScreen covers the Cancel button here too — same key, one declaration.

#### Thank You (ThankYouScreen)

| Key | English value |
|---|---|
| `thank_you_headline` | Thank you for your report! |
| `thank_you_body` | Your contribution helps improve our city. Public officials have been notified and will consider your report. |

#### My Reports (MyReportsScreen)

| Key | English value |
|---|---|
| `my_reports_title` | My Reports |
| `no_reports_message` | No reports yet. Tap 'Report' to submit your first one. |
| `status_sent` | SENT |
| `status_seen` | SEEN |
| `status_pending` | PENDING |
| `status_in_progress` | IN PROGRESS |
| `status_resolved` | RESOLVED |
| `status_discarded` | DISCARDED |

Note: `ReportStatus.name.replace("_", " ")` in `StatusChip` must be replaced with a `when` expression mapping each `ReportStatus` enum value to its corresponding `Res.string.*` key, so status labels are also translated.

#### Report Detail (ReportDetailScreen)

| Key | English value |
|---|---|
| `report_detail_title` | Report Detail |
| `could_not_load_report` | Could not load report |
| `back_content_description` | Back |
| `back_button` | Back |

#### Reports Map (ReportsMapScreen)

| Key | English value |
|---|---|
| `reports_map_title` | Reports Map |

### Replacement Pattern

Every hardcoded string in a `@Composable` context is replaced following this pattern:

```kotlin
// Before
Text("Submit Report")
Icon(..., contentDescription = "Cancel")

// After
Text(stringResource(Res.string.submit_report_button))
Icon(..., contentDescription = stringResource(Res.string.cancel_content_description))
```

For `StatusChip`, the `status.name.replace("_", " ")` call is replaced with an explicit `when` expression:

```kotlin
@Composable
fun statusLabel(status: ReportStatus): String = when (status) {
    ReportStatus.SENT        -> stringResource(Res.string.status_sent)
    ReportStatus.SEEN        -> stringResource(Res.string.status_seen)
    ReportStatus.PENDING     -> stringResource(Res.string.status_pending)
    ReportStatus.IN_PROGRESS -> stringResource(Res.string.status_in_progress)
    ReportStatus.RESOLVED    -> stringResource(Res.string.status_resolved)
    ReportStatus.DISCARDED   -> stringResource(Res.string.status_discarded)
}
```

### Design Checklist

- [x] No new layers required — this feature has no domain or data layer impact
- [x] No new repository interfaces
- [x] No platform-specific boundaries beyond what CMP already handles
- [x] No new Gradle submodule — change is entirely within `:shared` commonMain
- [x] No new library entries — `compose.components.resources` already present
- [x] No module dependency cycles introduced
- [x] `ReportStatus` display strings moved from runtime `name` manipulation to compile-time resource keys

---

## FEAT-009 — Light/Dark Color Theme

### Context

`App.kt` currently wraps its content in `MaterialTheme { }` with no custom color scheme, falling back to the system default Material3 palette. `isSystemInDarkTheme()` is available in CMP `commonMain` via `androidx.compose.foundation`.

### Pinterest Palette Note

The URL `https://pin.it/48pzpEuur` could not be read — Pinterest requires authentication to render pin content. The fetch chain confirmed the pin title references a **Guanajuato, Mexico city colour palette**. Guanajuato is known for highly saturated colonial architecture: terracotta reds, ochre yellows, vivid blues, and warm earth tones. However, those hues are not appropriate as a civic reporting app primary palette (low contrast, poor accessibility for status indicators). The following palette is designed for a **civic/municipal app** with strong accessibility contrast ratios and Material3 role compatibility, taking only warm-earth inspiration from the Guanajuato reference for secondary and tertiary roles. The architect has flagged this to the designer: if the user can share a screenshot or extracted hex codes from the Pinterest board, the palette can be revised before development.

### Color Palette Design

All values are ARGB hex. Both schemes must meet WCAG AA contrast (4.5:1 text on background) when combined.

#### Light Scheme

| M3 Role | Hex | Rationale |
|---|---|---|
| `primary` | `#1A6B4A` | Deep civic green — trust, action |
| `onPrimary` | `#FFFFFF` | White on green — AA compliant |
| `primaryContainer` | `#AAEBC7` | Soft mint for chips and cards |
| `onPrimaryContainer` | `#002114` | Near-black on mint |
| `secondary` | `#4A6741` | Muted olive — secondary actions |
| `onSecondary` | `#FFFFFF` | |
| `secondaryContainer` | `#C9EDBB` | Pale sage |
| `onSecondaryContainer` | `#0C2006` | |
| `tertiary` | `#8B5E3C` | Warm ochre — Guanajuato earth tone accent |
| `onTertiary` | `#FFFFFF` | |
| `tertiaryContainer` | `#FFD8B8` | Warm peach container |
| `onTertiaryContainer` | `#311200` | |
| `error` | `#BA1A1A` | Standard M3 error red |
| `onError` | `#FFFFFF` | |
| `errorContainer` | `#FFDAD6` | |
| `onErrorContainer` | `#410002` | |
| `background` | `#F8FAF7` | Off-white with green tint |
| `onBackground` | `#1A1C1A` | Near-black |
| `surface` | `#F8FAF7` | Same as background |
| `onSurface` | `#1A1C1A` | |
| `surfaceVariant` | `#DCE5D8` | Muted green-gray |
| `onSurfaceVariant` | `#404940` | |
| `outline` | `#707970` | |
| `outlineVariant` | `#BFC9BB` | |
| `scrim` | `#000000` | |
| `inverseSurface` | `#2F312E` | |
| `inverseOnSurface` | `#F0F1EC` | |
| `inversePrimary` | `#8FCFAC` | |

#### Dark Scheme

| M3 Role | Hex | Rationale |
|---|---|---|
| `primary` | `#8FCFAC` | Light mint — primary actions on dark |
| `onPrimary` | `#003824` | Dark green on mint |
| `primaryContainer` | `#005237` | Deep green container |
| `onPrimaryContainer` | `#AAEBC7` | |
| `secondary` | `#AEDAD0` | Soft cyan-green |
| `onSecondary` | `#1A3729` | |
| `secondaryContainer` | `#314E3E` | |
| `onSecondaryContainer` | `#C9EDBB` | |
| `tertiary` | `#F4BA87` | Warm gold — Guanajuato on dark |
| `onTertiary` | `#4C2900` | |
| `tertiaryContainer` | `#6C3F1F` | |
| `onTertiaryContainer` | `#FFD8B8` | |
| `error` | `#FFB4AB` | |
| `onError` | `#690005` | |
| `errorContainer` | `#93000A` | |
| `onErrorContainer` | `#FFDAD6` | |
| `background` | `#1A1C1A` | Near-black |
| `onBackground` | `#E2E3DE` | |
| `surface` | `#1A1C1A` | |
| `onSurface` | `#E2E3DE` | |
| `surfaceVariant` | `#404940` | |
| `onSurfaceVariant` | `#BFC9BB` | |
| `outline` | `#8A9389` | |
| `outlineVariant` | `#404940` | |
| `scrim` | `#000000` | |
| `inverseSurface` | `#E2E3DE` | |
| `inverseOnSurface` | `#1A1C1A` | |
| `inversePrimary` | `#1A6B4A` | |

### New File

One new file is introduced in `commonMain`:

```
shared/src/commonMain/kotlin/com/espert/reporteciudadano/AppTheme.kt
```

It declares:
- `private val LightColorScheme: ColorScheme` — built with `lightColorScheme(...)`
- `private val DarkColorScheme: ColorScheme` — built with `darkColorScheme(...)`
- `@Composable fun AppTheme(content: @Composable () -> Unit)` — calls `isSystemInDarkTheme()` to select the scheme, then delegates to `MaterialTheme(colorScheme = ..., content = content)`

### Change to App.kt

The call site changes from:

```kotlin
MaterialTheme {
```

to:

```kotlin
AppTheme {
```

No other files change. No new imports are needed beyond `AppTheme` itself.

### Module and Dependency Impact

- No new Gradle submodule.
- No new library dependencies. `material3` is already in `commonMain.dependencies`; `isSystemInDarkTheme` is in `androidx.compose.foundation` which is already a transitive dependency of material3.
- No domain or data layer impact.
- No Koin module changes.

### Design Checklist

- [x] No new layers — purely presentation concern
- [x] No new repository interfaces
- [x] No platform-specific boundaries — `isSystemInDarkTheme()` works in CMP commonMain
- [x] No new Gradle submodule
- [x] No new library dependencies
- [x] No module dependency cycles introduced
- [x] Palette note documented for designer review

---

## FEAT-008 — Spanish Translations (status note)

Translations are already complete and marked Done in `features.md`; no localization architecture work is required.

---

## FEAT-010 — Location Service Gate

### Problem Statement

`CameraScreen` currently gates on location *permission* (granted by the user in the OS dialog) but not on whether the device's **location service** (GPS / network provider) is actually **enabled**. A user can grant the permission yet have location turned off in device settings, causing `getLastKnownLocation()` to return `null` silently and the report to be created without coordinates.

### New expect/actual Functions

Two new `expect fun` declarations are added to `commonMain` in the existing `platform/` package. They are grouped in a new file to keep `LocationPermission.kt` focused on the composable permission flow:

**File**: `shared/src/commonMain/kotlin/com/espert/reporteciudadano/platform/LocationStatus.kt`

```kotlin
// Returns true if the device location service (GPS or network) is active.
expect fun isLocationEnabled(): Boolean

// Opens the device location settings screen as a platform side-effect.
expect fun openLocationSettings()
```

Both are plain (non-composable) functions. `isLocationEnabled()` is called synchronously from `CameraViewModel.processIntent()` after the location permission grant event arrives — keeping the composable layer free of platform logic.

### Platform Implementations

| Platform | `isLocationEnabled()` | `openLocationSettings()` |
|---|---|---|
| **Android** (`androidMain`) | `LocationManager.isProviderEnabled(GPS_PROVIDER) \|\| LocationManager.isProviderEnabled(NETWORK_PROVIDER)` — obtain `LocationManager` via `context.getSystemService(LOCATION_SERVICE)` | Launch `Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)` via `context.startActivity(...)` |
| **iOS** (`iosMain`) | `CLLocationManager.locationServicesEnabled()` | `UIApplication.sharedApplication.openURL(NSURL("App-Prefs:root=LOCATION_SERVICES"))` |
| **JS** (`jsMain`) | `return true` — the browser Geolocation API returns an error code itself when location is unavailable; no pre-check is possible | No-op — browser provides its own blocked-location UI |
| **WasmJS** (`wasmJsMain`) | `return true` — same rationale as JS | No-op |

**Android platform note**: `LocationManager` requires a `Context`. The Android actual must obtain it through a `Context` parameter or by reading it from a Koin-injected `Application` context. The preferred approach is to pass `Context` as a parameter or inject `ApplicationContext` via Koin into a thin wrapper — the Developer will decide the injection mechanism. The expect signature must remain context-free in `commonMain`; Android actual can access context via a module-level `lateinit var` populated by the Koin `androidContext()` binding, consistent with the existing pattern in the project.

### CameraState Changes

`CameraState` gains one new flag:

```kotlin
val locationDisabled: Boolean = false
```

### CameraIntent Changes

`CameraIntent` gains one new object:

```kotlin
object LocationServiceDisabled : CameraIntent()
```

### CameraViewModel Changes

After the location *permission* is granted (i.e., when `CameraIntent.LocationDenied` is not dispatched), the ViewModel checks the service status:

```kotlin
CameraIntent.LocationGranted -> {
    if (!isLocationEnabled()) {
        _state.update { it.copy(locationDisabled = true) }
    }
    // else proceed normally — cameraChecked flag in the composable advances
}
```

Note: the current flow uses a composable-local `locationChecked` boolean rather than a ViewModel state flag. The Developer must decide whether to lift `locationChecked` into the ViewModel as a `LocationGranted` intent or to call `isLocationEnabled()` inside the composable after `onGranted` is triggered. Both approaches are valid; the cleaner MVI approach is to emit `LocationGranted` as an intent so the ViewModel holds all logic. The architect recommends this lift as part of this feature's implementation.

### CameraScreen Changes

When `state.locationDisabled` is true, `CameraScreen` renders a `LocationDisabledContent` composable (analogous to the existing `LocationDeniedContent`) before any permission or camera composable runs. This content shows:

- `LocationOn` icon (48dp)
- Title: `location_service_disabled_title` (new string key)
- Body: `location_service_disabled_body` (new string key)
- Primary button: `go_to_settings_button` → calls `openLocationSettings()` then does nothing (user leaves app)
- Outlined button: `cancel_button` → calls `onCancel()`

The "Go to Settings" button is a platform side-effect call (`openLocationSettings()`), not a navigation event managed by the ViewModel — the app stays at this screen; the user returns from Settings and must re-enter the camera flow. This avoids complex app-resume lifecycle handling.

### New String Keys

Two new string keys are required in the existing `values/strings.xml` and `values-es/strings.xml`:

| Key | English | Spanish |
|---|---|---|
| `location_service_disabled_title` | Location Service Off | Servicio de ubicación desactivado |
| `location_service_disabled_body` | Location services are turned off on your device. Please enable them in Settings to continue. | Los servicios de ubicación están desactivados en tu dispositivo. Actívalos en Configuración para continuar. |
| `go_to_settings_button` | Go to Settings | Ir a Configuración |

### Module and Dependency Impact

- No new Gradle submodule.
- No new library dependencies.
- Android actual uses `android.location.LocationManager` and `android.content.Intent` + `android.provider.Settings` — all in the Android SDK, no additional imports.
- iOS actual uses `CoreLocation` (`CLLocationManager`) and `UIKit` (`UIApplication`) — both already available to `iosMain` via the Kotlin/Native Apple platform bindings.
- Koin module impact: if `Context` is needed in the Android actual of `isLocationEnabled()`, it is injected via the existing `androidContext()` binding. No new Koin module is required.

### Design Checklist

- [x] Layers defined: new `expect` functions in platform layer; state/intent changes in presentation; no domain or data layer changes
- [x] `CameraState.locationDisabled: Boolean` added
- [x] `CameraIntent.LocationServiceDisabled` added (for completeness; actual trigger is from `LocationGranted` path)
- [x] `isLocationEnabled(): Boolean` — expect in commonMain, four actuals
- [x] `openLocationSettings()` — expect in commonMain, four actuals (JS/WasmJS are no-ops)
- [x] Platform boundaries identified: Android uses `LocationManager` + `Settings` intent; iOS uses `CLLocationManager` + `UIApplication.openURL`
- [x] No new Gradle submodule
- [x] No new library dependencies
- [x] No module dependency cycles
