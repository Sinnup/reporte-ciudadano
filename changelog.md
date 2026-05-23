# Changelog

All notable changes to ReeporteCiudadano are documented here.
Updated per completed feature before merging to `main`.

Format: `[version] — YYYY-MM-DD`

---

## [Unreleased]

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
