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
import com.espert.reeporteciudadano.shared.generated.resources.Res
import com.espert.reeporteciudadano.shared.generated.resources.*

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
