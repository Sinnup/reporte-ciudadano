# Changelog

All notable changes to ReeporteCiudadano are documented here.
Updated per completed feature before merging to `main`.

Format: `[version] — YYYY-MM-DD`

---

## [Unreleased]

### Added — 2026-05-23

- **FEAT-009** — Light/Dark Color Theme: introduced `AppTheme.kt` in `commonMain` with a full 24-role Material3 palette (civic green primary `#1A6B4A`, warm ochre tertiary `#8B5E3C`, off-white background `#F8FAF7` for light; mint primary `#8FCFAC`, warm gold tertiary `#F4BA87`, near-black background `#1A1C1A` for dark); `LightColorScheme` and `DarkColorScheme` are selected at runtime via `isSystemInDarkTheme()`; `App.kt` switches `MaterialTheme {}` to `AppTheme {}` and wraps the root in `Surface` to eliminate the dark-mode white flash; all WCAG AA contrast pairs pass at ≥4.5:1 in both schemes; zero hardcoded color values remain in the codebase.

- **FEAT-010** — Location Service Gate: added `isLocationEnabled()`/`openLocationSettings()` expect/actual across Android, iOS, JS, and WasmJS; `CameraState` gains `locationDisabled: Boolean`; `CameraIntent` adds `LocationServiceDisabled` and `LocationServiceEnabled`; `CameraViewModel` calls `isLocationEnabled()` after permission is granted and sets the flag; `CameraScreen` renders `LocationDisabledContent` (full-screen, `LocationOff` icon in error tint, "Go to Settings" filled `Button`, "Cancel" `OutlinedButton`) when the flag is true; `LifecycleEventEffect(ON_RESUME)` auto-clears the gate when the user returns from device settings; three new string keys added to `values/strings.xml` and `values-es/strings.xml` (`location_service_disabled_title`, `location_service_disabled_body`, `go_to_settings_button`).

### Fixed — 2026-05-23

- **Android** — Camera location reliability: replaced the fragile `getLastKnownLocation()` call in `CameraCapture.android.kt` with `getCurrentDeviceLocation()`, a coroutine-based suspend function that runs in parallel with the camera session, tries all cached providers first, then registers `requestLocationUpdates` for a fresh fix; EXIF GPS still takes precedence and device location is the fallback.

### Added — 2026-05-23

- **FEAT-008** — Spanish translations: added `values/strings.xml` (43 English keys) and `values-es/strings.xml` (43 Spanish translations) using the Compose Multiplatform resource system; replaced every hardcoded string across 8 screens (`MainScreen`, `CameraScreen`, `PhotoReviewScreen`, `ReportFormScreen`, `ThankYouScreen`, `MyReportsScreen`, `ReportDetailScreen`, `ReportsMapScreen`) with `stringResource(Res.string.*)`; replaced `StatusChip`'s runtime `status.name.replace("_"," ")` with an exhaustive `when` expression so status labels are also translated; added `RequestCameraPermission` expect/actual for all platforms and a `CameraDenied` state to the camera flow; added `ReportStatusTest` (4 tests) verifying enum integrity.

### Fixed — 2026-05-23

- **Android** — osmdroid tile cache: set `osmdroidBasePath` and `osmdroidTileCache` to the app's private internal storage so the map renders correctly instead of showing the blue ocean background.
- **Android** — camera location fallback: after `readExifLocation()` returns null (system camera does not embed GPS in EXIF by default), fall back to `LocationManager` (GPS_PROVIDER then NETWORK_PROVIDER) so the address field is populated.
- **All platforms** — system bar overlap: wrapped the root composable in `Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing))` so the app content is padded away from the status bar, navigation bar, and display cutouts under `targetSdk=36` + `enableEdgeToEdge()`.

### Added
- Project scaffolding: KMP (Android, iOS, Web) with Compose Multiplatform
- CLAUDE.md with architecture and build guidance
- `features.md` template for user story tracking
- `.claude/agents/` with specialized agents (versioning, architect, developer, QA, platform experts, UX/UI designer, business analyst)
- Infrastructure: Ktor 3.1.3, SQLDelight 2.0.2, Koin 4.0.4, Coil3 3.1.0, OSMDroid 6.1.20, kotlinx-serialization 1.8.1, kotlinx-coroutines 1.10.2, kotlinx-datetime 0.6.2
- SQLDelight schema for `ReportEntity` and `ReportPhotoEntity` tables
- Android `FileProvider` configuration for camera photo URIs
- `ReporteCiudadanoApp` Application class with Koin initialization
- Domain layer: `CitizenReport`, `GeoLocation`, `ReportStatus`, `ReportPhoto` models
- Domain repositories: `ReportRepository`, `GeocodingRepository` interfaces
- Domain use cases: `SaveReportUseCase`, `GetAllReportsUseCase`, `GetReportByIdUseCase`, `ReverseGeocodeUseCase`
- Navigation: `AppViewModel` with back-stack, `NavDestination` sealed class, `BottomTab` enum
- Platform expects/actuals: `DatabaseDriverFactory`, `CameraCapture`, `MapView`, `RequestLocationPermission`, `generateUuid`
- Data layer: `GeocodingApi` (Nominatim/Ktor), `GeocodingRepositoryImpl`, `ReportRepositoryImpl` (SQLDelight)
- DI: `appModule` Koin module wiring all dependencies and ViewModels
- FEAT-001: App shell with three-tab `NavigationBar` (Report, My Reports, Map)
- FEAT-002: Camera capture flow — location permission check, up to 4 photos, EXIF GPS extraction, photo review screen
- FEAT-003: Report form — reverse geocoding address, title/description fields with char counters, address disclaimer, submit with SQLDelight persistence
- FEAT-004: Thank you screen with animated 4-second progress bar and auto-dismiss
- FEAT-005: My Reports list — `LazyColumn` of reports with color-coded `StatusChip` badges
- FEAT-006: Report Detail read-only screen — photos, title, description, address, status
- FEAT-007: Reports Map — OSMDroid integration on Android with tap-to-detail markers
- Unit tests: `SaveReportUseCaseTest`, `GetAllReportsUseCaseTest`, `GetReportByIdUseCaseTest` using `FakeReportRepository`

---
