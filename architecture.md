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
