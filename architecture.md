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
  location      : GeoLocation     // from EXIF of first valid photo; never null
  status        : ReportStatus
  createdAt     : Long            // epoch millis
  // address removed by FEAT-011 — reverse-geocoded on demand, never persisted

ReportPhoto
  localPath     : String          // file path after capture
  exifLocation  : GeoLocation?    // null if EXIF unavailable at capture time

GeoLocation
  latitude      : Double
  longitude     : Double
  // formatCoordinates() extension added by FEAT-011

LocationDisplay (sealed interface — presentation use only, never persisted)
  Address(text: String)
  Coordinates(latitude: Double, longitude: Double)
  Loading

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
| 11 | FEAT-011 | Offline-First Location | `feature/feat-011-offline-first-location` |

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

---

## FEAT-011 — Offline-First Location Refactor

### Context and Problem Statement

The current implementation has three interconnected problems that this refactor addresses as a single coherent unit:

1. **Address persisted in the database.** `ReportEntity` contains an `address TEXT NOT NULL` column. `ReportRepositoryImpl` writes `report.address` on save and reads it back on load. `ReportDetailScreen` displays `report.address` directly from the domain model. This means a stale, potentially wrong string is stored permanently.

2. **Address embedded in the domain model.** `CitizenReport` carries `val address: String`. This couples a UI-formatting concern (human-readable address) to the persistence-layer domain model. The domain model should only carry the coordinates that are the true source of truth.

3. **Coordinates can silently be null/zero.** `ReportFormViewModel.submit()` falls back to `GeoLocation(0.0, 0.0)` when no EXIF location is available (`photos.firstOrNull()?.exifLocation ?: GeoLocation(0.0, 0.0)`). `CapturedPhoto.exifLocation` is nullable (`GeoLocation?`). `ReportFormState.address` defaults to `"Location unavailable"` — but this does not block submission. A report with coordinates `(0.0, 0.0)` or a stale address can currently be saved and displayed.

### Requirements Summary

| Requirement | Rule |
|---|---|
| Coordinates must never be null at submission | Block or warn the user if no GPS/WiFi fix is available before form submission is allowed |
| DB stores coordinates only | Remove `address` column from `ReportEntity`; store only `latitude` and `longitude` |
| Address is never persisted | Reverse-geocoding happens on-demand in the UI layer and is discarded after display |
| Online display | If network is reachable, show reverse-geocoded human-readable address |
| Offline display | If no network, show formatted coordinate string (e.g., "18.4861° N, 69.9312° W") |

---

### Domain Model Changes

#### `CitizenReport` — remove `address`

The `address: String` field is removed. It does not belong in the domain model because it is a derived, on-demand, ephemeral value.

```
CitizenReport
  id          : String
  title       : String
  description : String
  photos      : List<ReportPhoto>
  location    : GeoLocation          // coordinates are non-negotiable
  status      : ReportStatus
  createdAt   : Long
  // address field REMOVED
```

`GeoLocation` itself is unchanged — it remains a pure data class with `latitude: Double` and `longitude: Double`.

#### `CapturedPhoto` — `exifLocation` becomes non-nullable at the submission boundary

`CapturedPhoto.exifLocation: GeoLocation?` in `NavDestination.kt` remains nullable at the capture layer (EXIF read can fail). However, the transition to `ReportFormScreen` must be gated: the caller (`CameraScreen` / `PhotoReviewScreen`) must verify that at least one photo carries a non-null `exifLocation` before navigating forward. If none of the captured photos have a location, a blocking message is shown and the user cannot proceed to the form.

This gate is a navigation-level concern, not a form-level concern — it belongs in the screen that triggers the navigation to `ReportForm`, not in the ViewModel of the form itself.

#### New domain model: `LocationDisplay` (sealed interface, in `domain/model/`)

To carry the on-demand address state into the UI, a new sealed interface is introduced:

```kotlin
sealed interface LocationDisplay {
    data class Address(val text: String) : LocationDisplay
    data class Coordinates(val latitude: Double, val longitude: Double) : LocationDisplay
    data object Loading : LocationDisplay
}
```

This type is used only in `ReportFormState` and `ReportDetailState` — it is never persisted or passed through repositories.

---

### Database Migration Plan

#### Current schema (version N)

```sql
CREATE TABLE ReportEntity (
    id          TEXT    NOT NULL PRIMARY KEY,
    title       TEXT    NOT NULL,
    description TEXT    NOT NULL,
    latitude    REAL    NOT NULL,
    longitude   REAL    NOT NULL,
    address     TEXT    NOT NULL,          -- TO BE REMOVED
    status      TEXT    NOT NULL DEFAULT 'SENT',
    created_at  INTEGER NOT NULL
);
```

#### Target schema (version N+1)

```sql
CREATE TABLE ReportEntity (
    id          TEXT    NOT NULL PRIMARY KEY,
    title       TEXT    NOT NULL,
    description TEXT    NOT NULL,
    latitude    REAL    NOT NULL,
    longitude   REAL    NOT NULL,
    status      TEXT    NOT NULL DEFAULT 'SENT',
    created_at  INTEGER NOT NULL
);
```

#### SQLDelight migration file

SQLDelight migrations live alongside the `.sq` schema file. A new migration file is required:

```
shared/src/commonMain/sqldelight/com/espert/reeporteciudadano/database/
  AppDatabase.sq          — update schema: remove address column from CREATE TABLE
  1.sqm                   — migration script from version 1 → 2
```

The migration script uses SQLite's column-drop workaround (SQLite prior to 3.35 does not support `ALTER TABLE DROP COLUMN` — check the SQLite version bundled in Android API levels in use):

```sql
-- 1.sqm  (migration from schema version 1 to 2)
CREATE TABLE ReportEntity_new (
    id          TEXT    NOT NULL PRIMARY KEY,
    title       TEXT    NOT NULL,
    description TEXT    NOT NULL,
    latitude    REAL    NOT NULL,
    longitude   REAL    NOT NULL,
    status      TEXT    NOT NULL DEFAULT 'SENT',
    created_at  INTEGER NOT NULL
);

INSERT INTO ReportEntity_new (id, title, description, latitude, longitude, status, created_at)
    SELECT id, title, description, latitude, longitude, status, created_at
    FROM ReportEntity;

DROP TABLE ReportEntity;

ALTER TABLE ReportEntity_new RENAME TO ReportEntity;
```

The `AppDatabase.sq` `insertReport` statement must also be updated — the `address` parameter is removed:

```sql
insertReport:
INSERT OR REPLACE INTO ReportEntity VALUES (?, ?, ?, ?, ?, ?, ?);
```

`ReportRepositoryImpl` must be updated accordingly: remove the `address = report.address` parameter from `db.appDatabaseQueries.insertReport(...)`, and remove the `address = entity.address` mapping in `getAll()` and `getById()`.

The SQLDelight `schemaVersion` in `shared/build.gradle.kts` must be incremented from `1` to `2`.

---

### Connectivity Detection

#### New `expect fun` in `commonMain/platform/`

A new file `shared/src/commonMain/kotlin/com/espert/reporteciudadano/platform/NetworkStatus.kt` introduces:

```kotlin
expect fun isNetworkAvailable(): Boolean
```

This is a synchronous check, context-free in the `commonMain` `expect` declaration, with the same injection pattern as `isLocationEnabled()`.

Platform actuals:

| Platform | Implementation |
|---|---|
| **Android** (`androidMain`) | `ConnectivityManager.activeNetworkInfo?.isConnected` (API < 23) or `ConnectivityManager.getNetworkCapabilities(activeNetwork)?.hasCapability(NET_CAPABILITY_INTERNET)` (API 23+). Injected `Context` via Koin `androidContext()`. |
| **iOS** (`iosMain`) | `NWPathMonitor` or `SCNetworkReachability` via Kotlin/Native bindings. For simplicity, the `actual` can use `NWPathMonitor` synchronously by checking the current path on demand: `NWPathMonitor().currentPath.status == .satisfied`. |
| **JS** (`jsMain`) | `navigator.onLine` — evaluates to `true` if the browser reports connectivity. |
| **WasmJS** (`wasmJsMain`) | Same as JS: `js("navigator.onLine")` cast to `Boolean`. |

The Developer must decide whether to use `isNetworkAvailable()` as a one-shot synchronous read in the ViewModel, or to expose a `Flow<Boolean>` via a `NetworkMonitor` expect class (for reactive updates). Given the current MVI approach in the codebase — where ViewModels are not reactive to system events — the simpler one-shot approach is recommended: call `isNetworkAvailable()` at the moment the location display section is about to render, and trigger a re-check when the screen is resumed via `LifecycleEventEffect(Lifecycle.Event.ON_RESUME)`.

---

### Affected Feature Flows

#### Capture flow (`CameraScreen` / `CameraCapture.android.kt`)

No changes to `CameraScreen` or `CameraViewModel`. The location capture in `CameraCapture.android.kt` already implements a two-stage approach: read EXIF first, fall back to `getCurrentDeviceLocation()`. The current implementation allows `null` to propagate. The architectural change is at the navigation gate (see next section).

#### Navigation gate (`PhotoReviewScreen` or `AppViewModel`)

The navigation from photo review to the report form must be gated on coordinate availability. The gate logic:

```
val location = photos.firstOrNull { it.exifLocation != null }?.exifLocation
if (location == null) → show blocking warning to user; do not navigate to ReportForm
if (location != null) → navigate to ReportForm
```

This check belongs in the screen that triggers the navigation — currently this is `PhotoReviewScreen` when the user taps "Continue". The ViewModel backing `PhotoReviewScreen` (currently `CameraViewModel`) needs a new intent/state for "no location available on any photo".

The BA must decide whether this is shown as an inline error on `PhotoReviewScreen` or as a blocking dialog. The architect recommends an inline error message shown below the photo strip, with the Continue button disabled, rather than a dialog — dialogs for non-recoverable states on this screen are already complex.

#### Report form (`ReportFormViewModel` / `ReportFormScreen`)

Major changes:

1. Remove `address: String` from `ReportFormState`. Replace with `locationDisplay: LocationDisplay = LocationDisplay.Loading`.
2. `ReportFormViewModel.init()` receives the `GeoLocation` directly (derived from `capturedPhotos`), not the photo list, since coordinates are now guaranteed non-null at this point. The ViewModel no longer needs to extract and null-check `exifLocation`.
3. In `init()`, after obtaining the `GeoLocation`:
   - Call `isNetworkAvailable()`.
   - If online: launch `ReverseGeocodeUseCase(location)` and update state with `LocationDisplay.Address(result)` on success, or `LocationDisplay.Coordinates(lat, lon)` on failure.
   - If offline: immediately set state to `LocationDisplay.Coordinates(lat, lon)`.
4. `submit()` removes `address = s.address` from `CitizenReport` construction. The `location` is taken from the `GeoLocation` that was set during `init()`, not from photos — store it as a private val in the ViewModel.
5. `canSubmit` remains unchanged: `title.isNotBlank() && description.isNotBlank() && !isSubmitting`. There is no longer a dependency on address loading to enable the Submit button — coordinates are guaranteed.

#### Report detail (`ReportDetailViewModel` / `ReportDetailState` / `ReportDetailScreen`)

Major changes:

1. `ReportDetailState` gains `val locationDisplay: LocationDisplay = LocationDisplay.Loading`.
2. `ReportDetailViewModel.init {}` block: after loading the report, launch a second coroutine that:
   - Calls `isNetworkAvailable()`.
   - If online: calls `ReverseGeocodeUseCase(report.location)` and updates `locationDisplay` with `LocationDisplay.Address(result)` on success, or falls back to `LocationDisplay.Coordinates`.
   - If offline: sets `LocationDisplay.Coordinates(report.location.latitude, report.location.longitude)`.
3. `ReportDetailViewModel` now depends on `ReverseGeocodeUseCase` in addition to `GetReportByIdUseCase`. The Koin binding `viewModel { (reportId: String) -> ReportDetailViewModel(reportId, get()) }` becomes `viewModel { (reportId: String) -> ReportDetailViewModel(reportId, get(), get()) }`.
4. `ReportDetailScreen`: the address row `Text(report.address, ...)` is replaced with a `when (state.locationDisplay)` expression:
   - `LocationDisplay.Loading` → `CircularProgressIndicator(Modifier.size(16.dp))` inline
   - `LocationDisplay.Address(text)` → `Text(text, ...)`
   - `LocationDisplay.Coordinates(lat, lon)` → `Text(formatCoordinates(lat, lon), ...)`

The coordinate formatting helper `formatCoordinates(latitude: Double, longitude: Double): String` is a pure Kotlin function in `commonMain`, no `expect/actual` needed:

```kotlin
fun formatCoordinates(latitude: Double, longitude: Double): String {
    val latDir = if (latitude >= 0) "N" else "S"
    val lonDir = if (longitude >= 0) "E" else "W"
    return "%.4f° %s, %.4f° %s".format(kotlin.math.abs(latitude), latDir, kotlin.math.abs(longitude), lonDir)
}
```

Note: `String.format` is available in `commonMain` since Kotlin 1.9 via `kotlin.text` on all KMP targets. If the project's Kotlin version does not support it, the Developer should use `buildString` with manual decimal formatting or the `kotlin-stdlib` `formatTo` alternatives. The architect recommends placing this function in `domain/model/GeoLocation.kt` as an extension function.

---

### New String Keys Required

| Key | English | Spanish |
|---|---|---|
| `no_location_on_photos_title` | No Location Data | Sin datos de ubicación |
| `no_location_on_photos_body` | None of your photos have GPS coordinates. Please retake photos with location services enabled. | Ninguna de tus fotos tiene coordenadas GPS. Vuelve a tomar las fotos con los servicios de ubicación activados. |
| `location_loading` | Resolving location... | Resolviendo ubicación... |

The disclaimer dialog keys (`address_dialog_title`, `address_dialog_body`) remain. Their text should be reviewed by the BA — "approximate address" language may need updating to reflect the offline-coordinates fallback.

---

### Koin Module Changes (`AppModule.kt`)

The only Koin change is in the `ReportDetailViewModel` binding. No new modules or singletons are required. `ReverseGeocodeUseCase` is already a `factory` binding.

`isNetworkAvailable()` is a plain `expect fun` — no Koin injection needed, consistent with `isLocationEnabled()`.

---

### Module Placement

No new Gradle submodule. All changes are within `:shared` `commonMain` and platform source sets, consistent with the initial module strategy documented in `architecture.md`. The affected file set:

| File | Change type |
|---|---|
| `domain/model/CitizenReport.kt` | Remove `address` field |
| `domain/model/LocationDisplay.kt` | New sealed interface |
| `domain/model/GeoLocation.kt` | Add `formatCoordinates` extension function |
| `domain/repository/GeocodingRepository.kt` | No change |
| `domain/usecase/ReverseGeocodeUseCase.kt` | No change |
| `data/repository/ReportRepositoryImpl.kt` | Remove `address` from save/load |
| `AppDatabase.sq` | Remove `address` column; update `insertReport` statement |
| `1.sqm` (new) | Migration script version 1 → 2 |
| `shared/build.gradle.kts` | Increment SQLDelight `schemaVersion` to 2 |
| `platform/NetworkStatus.kt` (new, commonMain) | `expect fun isNetworkAvailable(): Boolean` |
| `platform/NetworkStatus.android.kt` (new) | `ConnectivityManager` actual |
| `platform/NetworkStatus.ios.kt` (new) | `NWPathMonitor` or `SCNetworkReachability` actual |
| `platform/NetworkStatus.js.kt` (new) | `navigator.onLine` actual |
| `platform/NetworkStatus.wasmJs.kt` (new) | `navigator.onLine` actual |
| `feature/camera/CameraState.kt` | Add `noLocationOnPhotos: Boolean = false` |
| `feature/camera/CameraIntent.kt` | Add `object NoLocationOnPhotos : CameraIntent()` |
| `feature/camera/CameraViewModel.kt` | Handle `NoLocationOnPhotos` intent |
| `feature/camera/PhotoReviewScreen.kt` | Gate "Continue" on location availability; show inline error |
| `feature/reportform/ReportFormState.kt` | Replace `address: String` + `isLoadingAddress` with `locationDisplay: LocationDisplay` |
| `feature/reportform/ReportFormIntent.kt` | No change expected |
| `feature/reportform/ReportFormViewModel.kt` | Remove address logic; add network check + geocode-or-coordinates logic |
| `feature/reportform/ReportFormScreen.kt` | Replace address card with `LocationDisplay` renderer |
| `feature/reportdetail/ReportDetailState.kt` | Add `locationDisplay: LocationDisplay` |
| `feature/reportdetail/ReportDetailViewModel.kt` | Add `ReverseGeocodeUseCase` dependency; add network check + geocode-or-coordinates logic |
| `feature/reportdetail/ReportDetailScreen.kt` | Replace `report.address` with `LocationDisplay` renderer |
| `di/AppModule.kt` | Update `ReportDetailViewModel` binding to inject `ReverseGeocodeUseCase` |
| `navigation/NavDestination.kt` | `CapturedPhoto.exifLocation` stays nullable (changed semantically, not structurally) |
| `values/strings.xml` | Add new string keys |
| `values-es/strings.xml` | Add Spanish translations |

---

### Platform-Specific Notes

#### Android

- `CameraCapture.android.kt` already performs the two-stage location fetch (EXIF → `getCurrentDeviceLocation`). The result can still be `null` if no provider delivers a fix within the camera session. This is the source of the current nullable `exifLocation`. No change to `CameraCapture.android.kt` is required — the gate is in `PhotoReviewScreen`.
- `isNetworkAvailable()` Android actual: use `ConnectivityManager`. Inject `Context` via `KoinComponent` or `androidContext()` binding, exactly as `LocationStatusHelper` does in `LocationStatus.android.kt`.
- The SQLDelight migration runs automatically when the `AppDatabase` is opened if the `SqlDriver` is created with `androidSqliteDriver` and migrations are registered. The Developer must ensure the migration file is picked up by the SQLDelight Gradle plugin (`schemaVersion = 2`, migration file `1.sqm` present).

#### iOS

- `CameraCapture.ios.kt` is currently a placeholder. When a real iOS camera implementation is built, it must follow the same pattern as Android: attempt EXIF read, fall back to `CLLocationManager.location` for the current fix. This is a future concern — this feature's iOS scope is only the `NetworkStatus.ios.kt` actual.
- `isNetworkAvailable()` iOS actual: use `Network.framework`'s `NWPathMonitor`. The Kotlin/Native bindings expose `Network` via the Apple platform umbrella. Alternatively, `SystemConfiguration.framework`'s `SCNetworkReachabilityCreateWithName` is available. The architect recommends `NWPathMonitor` for correctness on modern iOS.

#### Web (JS / WasmJS)

- `CameraCapture.js.kt` / `CameraCapture.wasmJs.kt` are placeholders. When a real Web camera implementation is built, coordinates must be obtained from the Geolocation API (`navigator.geolocation.getCurrentPosition`), not from EXIF (browser-captured images rarely embed GPS in EXIF due to browser privacy policies).
- `isNetworkAvailable()` JS/WasmJS: `navigator.onLine` is a synchronous boolean — straightforward.

---

### Design Checklist

- [x] Layers defined: domain model (`CitizenReport`, `LocationDisplay`), data layer (DB migration, `ReportRepositoryImpl`), platform layer (`NetworkStatus` expect/actual), presentation layer (form and detail screens + ViewModels)
- [x] `LocationDisplay` sealed interface defined in `domain/model/` — no platform imports
- [x] `address` removed from `CitizenReport` domain model
- [x] DB migration plan: `1.sqm`, new `AppDatabase.sq` schema, `schemaVersion = 2`
- [x] Navigation gate on coordinate availability identified — placed in `PhotoReviewScreen` / `CameraViewModel`
- [x] `isNetworkAvailable()` — `expect` in `commonMain/platform/`, four `actual` implementations
- [x] Reverse geocoding remains on-demand via existing `ReverseGeocodeUseCase` + `GeocodingRepository` + `GeocodingApi` (Nominatim) — no new repository or data source
- [x] `formatCoordinates()` — pure Kotlin extension in `commonMain`, no `expect/actual`
- [x] Module placement: all changes within `:shared`; no new Gradle submodule
- [x] No new library dependencies
- [x] No module dependency cycles — `ReportDetailViewModel` now depends on `ReverseGeocodeUseCase` which already exists in the DI graph
- [x] `AppModule.kt` update: one binding change (`ReportDetailViewModel`); no new modules
