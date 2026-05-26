# Changelog

All notable changes to ReporteCiudadano are documented here.
Updated per completed feature before merging to `main`.

Format: `[version] — YYYY-MM-DD`

---

## [Unreleased]

### Added — 2026-05-25

- **FEAT-013** — Cloud Sync: DynamoDB + S3 (BREAKING: `ReportEntity` schema v3): new Gradle submodule `:feature:cloudsync` (Android Library, KMP) containing all sync domain types and platform scheduler code. All DynamoDB and S3 calls use raw Ktor HTTP requests signed with AWS Signature Version 4 HMAC-SHA256; a platform `expect fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray` is implemented with `javax.crypto` on Android, `CommonCrypto` on iOS, and `SubtleCrypto` on JS/WasmJS — no AWS SDK dependency required. Background scheduling: Android uses a `WorkManager` `CoroutineWorker` with `NetworkType.CONNECTED` constraint, exponential backoff, and expedited task support; iOS uses `BGTaskScheduler` plus a foreground-resume coroutine launched on `ProcessLifecycleOwner.ON_START`; Web JS/WasmJS uses a `window.addEventListener("online", ...)` handler. Idempotent uploads: DynamoDB `PutItem` uses a `attribute_not_exists(id)` condition expression (a `ConditionalCheckFailedException` is treated as success); S3 issues `HeadObject` before every `PutObject` and skips the upload if the object already exists; S3 key format is `reports/<reportId>/<filename>`. `2.sqm` migration adds `synced_at INTEGER` (nullable) and `sync_failure_count INTEGER NOT NULL DEFAULT 0` to `ReportEntity`; `schemaVersion` bumped from 2 to 3 in `shared/build.gradle.kts`. New domain types: `SyncStatus` enum (`PENDING`, `SYNCED`, `FAILED`), `SyncRecord` data class, `CloudSyncRepository` interface, and five use cases: `SyncReportUseCase`, `SyncAllPendingUseCase`, `RecordSyncFailureUseCase`, `RetryFailedSyncsUseCase`, `GetSyncStatusUseCase`. After 5 consecutive failures `syncFailureCount` reaches the threshold and a device notification is dispatched; tapping the notification re-enqueues a fresh 5-attempt retry cycle (`SyncRetryReceiver` `BroadcastReceiver` on Android, `UNUserNotificationCenter` RETRY_SYNC action on iOS, `Snackbar` with retry action on Web). `SyncStatusIcon` composable added to `MyReportsScreen` list items: `CloudDone` in `primary` tint when `SYNCED`, `CloudUpload` in `onSurfaceVariant` when `PENDING`, `SyncProblem` in `error` tint when `FAILED`. AWS credentials (Access Key ID, Secret Access Key, region, DynamoDB table name, S3 bucket name) are read at runtime from a gitignored `aws.properties` file; they are never embedded in the binary. `MyReportsState` and `MyReportsViewModel` updated to expose per-report `SyncStatus`. 67 new unit tests across `commonTest` covering `SyncStatus`, `SyncRecord`, `RecordSyncFailureUseCase`, `RetryFailedSyncsUseCase`, `SyncReportUseCase`, `CloudSyncRepositoryImpl` contract, and `MyReportsViewModel` sync-state projection — all passing.

- **FEAT-012** — Greeting in test.md: added `¡Hola, ReporteCiudadano! 👋` at the top of `test.md` to validate the end-to-end feature pipeline on a non-production artifact.

### Changed — 2026-05-24

- **CI** — Rewrote `.github/workflows/kmp.yml` from a single serial build job into five independent parallel jobs (`compile-android`, `compile-ios`, `compile-js`, `compile-wasmjs`, `test-jvm`) plus an `all-green` gate job that acts as the single required branch-protection status check. JDK upgraded from 11 to 17 to satisfy AGP 9.2.1. Gradle caching via `gradle/actions/setup-gradle@v4`. `concurrency` group added to cancel stale in-flight runs on new pushes. Test results uploaded as an artifact from `test-jvm` (always, including on failure).

### Added — 2026-05-24

- **FEAT-011** — Offline-First Location (BREAKING: `ReportEntity` schema v2): removed `address TEXT NOT NULL` from the SQLDelight schema; added `latitude REAL NOT NULL` and `longitude REAL NOT NULL` as the canonical location record; `1.sqm` migration recreates `ReportEntity` and copies existing rows so no data is lost; `schemaVersion` bumped from 1 to 2 in `shared/build.gradle.kts`. `CitizenReport` domain model no longer carries `address: String` — coordinates are the single source of truth. New `LocationDisplay` sealed class (`Loading`, `Address`, `Coordinates`) drives presentation-layer location rendering without touching the database. New `isNetworkAvailable()` expect/actual across Android (`ConnectivityManager`), iOS (`NWPathMonitor`), JS, and WasmJS (`navigator.onLine`). `ReportFormViewModel` and `ReportDetailViewModel` resolve location in three states: spinner while geocoding (`Loading`), human-readable address when geocoding succeeds online (`Address`), formatted coordinate string (`18.4861° N, 69.9312° W`) when offline or geocoding fails (`Coordinates`). `PhotoReviewScreen` now gates the Continue button: if none of the captured photos carry a GPS fix the button is disabled and an inline error row with `GpsOff` icon appears below the photo strip. `LocationDisplayCard` composable encapsulates all three rendering states in `ReportFormScreen` and `ReportDetailScreen`. New string keys: `no_location_on_photos_body`, `location_loading`, `location_offline_label`; updated `address_dialog_body` wording — added in both `values/strings.xml` and `values-es/strings.xml`. 51 new unit tests across `commonTest` covering `LocationDisplay`, `GeoLocation.formatCoordinates()`, `NetworkStatus`, `CameraViewModel`, `ReportFormViewModel`, `ReportDetailViewModel`, and repository mapping.

### Fixed — 2026-05-24

- **ReportFormViewModel** — Second report session navigated instantly: the `submitted: Boolean` flag in `ReportFormState` was never reset between sessions, so opening the form a second time immediately triggered the one-way navigation event. Replaced the boolean flag with a `Channel<Unit>` one-time event so each submission fires exactly once and the channel is not replayed on re-entry.
- **CameraViewModel** — Photo list accumulated across sessions: `CameraScreen` did not dispatch a reset on entry, so photos from a previous session were still present when a new session started, causing `isFull` to fire on old photos. Added `CameraIntent.Reset` dispatched from `CameraScreen`'s `LaunchedEffect(Unit)` and handled in `CameraViewModel` by clearing `photos` and resetting all derived state.
- **CameraCapture (Android)** — GPS race condition on camera callback: the callback read `deviceLocation` which could still be null when the camera returned quickly. The callback now awaits the location `Deferred` with a 15-second timeout; the slow path registers both GPS and network providers in parallel (race) instead of only the first enabled provider, so a location fix is obtained more reliably before the timeout elapses.

- **ReportDetailScreen** — Report detail always showed the same report: `koinViewModel` was scoped to the Activity's `ViewModelStore` (custom nav stack, not Jetpack Navigation), so every navigation to a report detail reused the first ViewModel instance regardless of the report ID. Fixed by passing `key = reportId` to the `koinViewModel` call so each report ID gets its own isolated ViewModel instance.

### Added — 2026-05-23

- **`migration-expert` agent** — New Claude Code agent for dependency lifecycle management: audits `gradle/libs.versions.toml` and all `build.gradle.kts` files, looks up latest stable releases from authoritative registries, evaluates breaking changes, applies upgrades in safe order (Kotlin → KSP → AGP → Compose → kotlinx → Ktor → SQLDelight → Koin), verifies compilation and tests across all KMP targets, and hands off to `versioning` for commit. Pre-release pins trigger a user prompt before touching them.

- **App Icon** — Custom adaptive icon: white location-pin silhouette with embedded pothole and crack lines on a civic-green (`#1A6B4A`) background; ships as a full adaptive icon set (foreground/background layers) on Android and as a flat icon on iOS and Web.

- **FEAT-009** — Light/Dark Color Theme: introduced `AppTheme.kt` in `commonMain` with a full 24-role Material3 palette (civic green primary `#1A6B4A`, warm ochre tertiary `#8B5E3C`, off-white background `#F8FAF7` for light; mint primary `#8FCFAC`, warm gold tertiary `#F4BA87`, near-black background `#1A1C1A` for dark); `LightColorScheme` and `DarkColorScheme` are selected at runtime via `isSystemInDarkTheme()`; `App.kt` switches `MaterialTheme {}` to `AppTheme {}` and wraps the root in `Surface` to eliminate the dark-mode white flash; all WCAG AA contrast pairs pass at ≥4.5:1 in both schemes; zero hardcoded color values remain in the codebase.

- **FEAT-010** — Location Service Gate: added `isLocationEnabled()`/`openLocationSettings()` expect/actual across Android, iOS, JS, and WasmJS; `CameraState` gains `locationDisabled: Boolean`; `CameraIntent` adds `LocationServiceDisabled` and `LocationServiceEnabled`; `CameraViewModel` calls `isLocationEnabled()` after permission is granted and sets the flag; `CameraScreen` renders `LocationDisabledContent` (full-screen, `LocationOff` icon in error tint, "Go to Settings" filled `Button`, "Cancel" `OutlinedButton`) when the flag is true; `LifecycleEventEffect(ON_RESUME)` auto-clears the gate when the user returns from device settings; three new string keys added to `values/strings.xml` and `values-es/strings.xml` (`location_service_disabled_title`, `location_service_disabled_body`, `go_to_settings_button`).

### Fixed — 2026-05-23

- **Android** — System back button navigation: registered `OnBackPressedCallback` in `MainActivity` that delegates to `AppViewModel.back()`; when the stack is at root the callback yields to the system (exits the app) and then re-enables, so pressing back on any inner screen now navigates within the app instead of immediately closing it.
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
