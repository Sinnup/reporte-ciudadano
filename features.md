# Features

Features flow through these stages before development starts:

```
Architect analysis → BA writes user story → UX/UI proposal → [iterate] → Ready → Development → QA → Versioning → Done
```

See `architecture.md` for domain model, feature sequence, and platform boundaries.

---

### [FEAT-001] App Shell & Navigation

**Status**: `Done`

**Architect Notes**
Three-tab shell. No business logic. Sets up the `NavigationBar` host and empty placeholder screens
for each tab. Foundation for all other features.

**User Story** *(Business Analyst)*
> As a citizen, I want a tab-based main screen so that I can quickly navigate between reporting
> a new pothole, viewing my submitted reports, and seeing all reports on a map.

**Acceptance Criteria**
- [x] App opens on the "Report" tab by default.
- [x] Three tabs are always visible at the bottom: "Report", "My Reports", "Reports Map".
- [x] Switching tabs preserves each tab's scroll/state position.
- [x] The active tab is visually distinct from inactive ones.
- [x] Each tab shows its dedicated screen (content delivered by subsequent features).

**UX/UI Proposal** *(Designer — approved)*

Layout: `Scaffold` with a `NavigationBar` at the bottom. No top app bar on the shell itself —
each tab's content screen manages its own.

| Tab | Icon | Label |
|---|---|---|
| Report | `AddCircleOutline` | Report |
| My Reports | `ListAlt` | My Reports |
| Reports Map | `Map` | Map |

States: shell has no loading/error/empty state — it is always visible.
Back navigation: tabs are peers; pressing back from any tab exits the app (no tab back-stack).

Material3: `NavigationBar` + `NavigationBarItem`. Content area swaps via `when (selectedTab)`.

---

### [FEAT-002] Camera & Photo Capture

**Status**: `Done`

**Architect Notes**
Platform-specific concern behind `expect/actual`. Location permission must be granted before
the camera opens — if denied, camera is blocked with an explanation. Captures up to 4 photos.
Each capture embeds GPS coordinates in EXIF. Preview screen allows retake or delete per photo.
Entire session can be cancelled at any time.

**User Story** *(Business Analyst)*
> As a citizen, I want to take up to 4 photos of a pothole using my phone camera so that officials
> have clear visual evidence of the issue, with GPS coordinates automatically recorded from EXIF data.

**Acceptance Criteria**
- [x] Location permission is requested before the camera opens.
- [x] If location permission is denied, the camera is blocked and an explanation screen is shown.
- [x] The user can take up to 4 photos per report session.
- [x] After each photo, the user can choose to take another, retake the last one, or complete.
- [x] When 4 photos are captured, the session automatically moves to review.
- [x] A photo review screen shows all captured photos in a horizontal scroll.
- [x] The user can cancel the entire session at any point and return to the main screen.
- [x] GPS coordinates from the first photo's EXIF data are used as the report location.

**UX/UI Proposal** *(Designer — approved)*

Flow: Permission check → CameraCapture (platform) → Options dialog (keep taking / retake / complete) → PhotoReviewScreen.

States:
- **Permission denied**: full-screen error with location icon, explanation text, and Cancel button.
- **Camera active**: native camera UI (via platform actual).
- **Options dialog**: `AlertDialog` with "Keep taking", "Retake", "Complete" actions.
- **Review screen**: `Scaffold` with `TopAppBar` (Close icon), `LazyRow` of 160dp thumbnails, full-width Continue button at bottom.

Material3: `AlertDialog`, `TopAppBar`, `LazyRow`, `AsyncImage` (Coil3).

---

### [FEAT-003] Report Form & Submission

**Status**: `Done`

**Architect Notes**
Depends on FEAT-002 (receives photos + EXIF locations). Reverse-geocodes the location of the
first photo via `GeocodingRepository` (Ktor → Nominatim/system geocoder per platform).
Shows an approximation disclaimer popup on entry. Mandatory fields: Title and Description.
Address is non-editable. On submit: `SaveReportUseCase` persists via SQLDelight with status SENT.

**User Story** *(Business Analyst)*
> As a citizen, I want to add a title and description to my pothole report and see the automatically
> resolved address so that officials have context about the location and severity of the issue.

**Acceptance Criteria**
- [x] An address-approximation disclaimer dialog is shown on screen entry.
- [x] The Title field is mandatory, max 100 characters, with a live character counter.
- [x] The Description field is mandatory, max 500 characters, with a live character counter.
- [x] The address is displayed in a non-editable card, loaded via reverse geocoding.
- [x] While the address is loading, a spinner is shown inside the address card.
- [x] The Submit button is disabled until both Title and Description are filled.
- [x] On submit, a spinner replaces the button text while saving.
- [x] After successful save, the app navigates to the Thank You screen.
- [x] The user can cancel at any time and return to the main screen.

**UX/UI Proposal** *(Designer — approved)*

Layout: `Scaffold` with `TopAppBar` (title "New Report", Close icon), scrollable form body, and a sticky Submit button at the bottom.

Sections (top to bottom):
1. Horizontal photo thumbnail strip (`LazyRow`, 120dp images from Coil3).
2. Title `OutlinedTextField` with character counter in supporting text.
3. Description `OutlinedTextField` (minLines = 3) with character counter.
4. Address `ElevatedCard` with `LocationOn` icon and text (or spinner).

Disclaimer: `AlertDialog` with "Got it" confirm button; appears before any other interaction.

Material3: `Scaffold`, `TopAppBar`, `OutlinedTextField`, `ElevatedCard`, `AlertDialog`, `CircularProgressIndicator`.

---

### [FEAT-004] Thank You Screen

**Status**: `Done`

**Architect Notes**
Simple success screen shown after FEAT-003 submission. Auto-dismisses after 4 seconds via a
`LaunchedEffect` countdown. No user action required — navigates back to the "Report" tab root.

**User Story** *(Business Analyst)*
> As a citizen, I want to see a confirmation screen after submitting my report so that I know
> my report was received and understand what happens next.

**Acceptance Criteria**
- [x] A success icon is displayed prominently.
- [x] A thank-you message and explanation of next steps is shown.
- [x] A linear progress indicator animates from 0% to 100% over 4 seconds.
- [x] The screen automatically navigates back to the Report tab after 4 seconds.
- [x] No user interaction is required to dismiss the screen.

**UX/UI Proposal** *(Designer — approved)*

Layout: Centered `Column` with 32dp padding.

Elements:
- Large (80dp) `CheckCircle` icon in primary color.
- `headlineMedium` thank-you text.
- `bodyLarge` explanation paragraph.
- Full-width `LinearProgressIndicator` animating with `tween(4000)`.

Auto-dismiss: `LaunchedEffect(Unit)` sets progress to 1f, then `delay(4000)` before calling `onDone`.

Material3: `LinearProgressIndicator`, `animateFloatAsState`.

---

### [FEAT-005] My Reports List

**Status**: `Done`

**Architect Notes**
Reads from `ReportRepository.getAll()`. Displays title + status badge per item. Tapping navigates
to FEAT-006 (Report Detail). Status values: SENT, SEEN, PENDING, IN_PROGRESS, RESOLVED, DISCARDED.
Initially all reports will have status SENT (remote status sync is a future enhancement).

**User Story** *(Business Analyst)*
> As a citizen, I want to see a list of all the pothole reports I have submitted so that I can
> track the status of each report and select one to view its details.

**Acceptance Criteria**
- [x] The list shows all saved reports, most recent first.
- [x] Each list item displays the report title and a color-coded status badge.
- [x] While reports are loading, a centered spinner is shown.
- [x] When no reports exist, an informative empty-state message is shown.
- [x] Tapping a report item navigates to the Report Detail screen.
- [x] Status badge colors are distinct per status value for quick visual scanning.

**UX/UI Proposal** *(Designer — approved)*

Layout: `Scaffold` with `CenterAlignedTopAppBar` ("My Reports").

List: `LazyColumn` of `ElevatedCard` items. Each card has:
- Report `title` (left, `titleMedium`, weighted to fill space).
- `StatusChip` (right).

StatusChip color mapping:
| Status | Color token |
|---|---|
| SENT | `tertiaryContainer` |
| SEEN | `secondaryContainer` |
| PENDING | `errorContainer` |
| IN_PROGRESS | `primaryContainer` |
| RESOLVED | `surfaceVariant` |
| DISCARDED | `surfaceVariant` |

States: Loading (spinner), Empty (centered text), Populated (LazyColumn).

Material3: `Scaffold`, `CenterAlignedTopAppBar`, `LazyColumn`, `ElevatedCard`, `SuggestionChip`.

---

### [FEAT-006] Report Detail (read-only)

**Status**: `Done`

**Architect Notes**
Shared read-only view used by both FEAT-005 and FEAT-007. Loads a single report via
`ReportRepository.getById(id)`. Displays all fields (photos, title, description, address, status)
in non-editable form. Reuses the same layout structure as the Report Form (FEAT-003).

**User Story** *(Business Analyst)*
> As a citizen, I want to view the full details of a submitted report so that I can review
> the information I provided and check the current status assigned by officials.

**Acceptance Criteria**
- [x] All report fields are displayed read-only: photos, title, description, address, status.
- [x] Photos are shown in a horizontally scrollable strip.
- [x] The current status is shown as a color-coded badge.
- [x] While the report is loading, a centered spinner is shown.
- [x] If the report cannot be loaded, an error message and Back button are shown.
- [x] A Back button in the top app bar returns the user to the previous screen.

**UX/UI Proposal** *(Designer — approved)*

Layout: `Scaffold` with `TopAppBar` ("Report Detail", back arrow).

Body (scrollable `Column`):
1. Photo strip (`LazyRow`, 160dp images, same as PhotoReviewScreen).
2. `StatusChip` (reused from FEAT-005).
3. Title (`headlineSmall`).
4. Description (`bodyLarge`).
5. Address row: `LocationOn` icon + `bodyMedium` text.

States: Loading (spinner), Error (centered text + Back button), Loaded (full content).

Material3: `Scaffold`, `TopAppBar`, `LazyRow`, `AsyncImage`, `StatusChip`.

---

### [FEAT-007] Reports Map

**Status**: `Done`

**Architect Notes**
Platform-specific map (OSMDroid on Android, placeholder on iOS/Web) behind `expect/actual`.
Loads all reports via `ReportRepository.getAll()` and places a pin at each `GeoLocation`.
Tapping a pin navigates to FEAT-006 (Report Detail).

**User Story** *(Business Analyst)*
> As a citizen, I want to see all submitted pothole reports displayed as pins on a map so that
> I can understand the geographic distribution of issues in my city and tap a pin to view details.

**Acceptance Criteria**
- [x] All saved reports are displayed as map markers at their GPS coordinates.
- [x] While reports are loading, a centered spinner is shown.
- [x] Tapping a marker navigates to the Report Detail screen for that report.
- [x] The map centers on the first report's location when markers are loaded.
- [x] On platforms where a native map is unavailable, a placeholder message is shown.
- [x] The map title is shown in a semi-transparent top bar overlay.

**UX/UI Proposal** *(Designer — approved)*

Layout: Full-screen `Box` with the map as the base layer and a semi-transparent
`CenterAlignedTopAppBar` ("Reports Map", 85% opacity surface color) floating on top.

Map: OSMDroid (`AndroidView`) on Android with MAPNIK tile source, multi-touch enabled.
Markers: `org.osmdroid.views.overlay.Marker` with click listener navigating to detail.

States: Loading (centered spinner), Loaded (map with pins), Empty (map with no pins — no special state needed).

Material3: `CenterAlignedTopAppBar` (containerColor with alpha).

---

### [FEAT-008] Spanish Translations

**Status**: `Done`

**Architect Notes**

This feature adds full Spanish (es) localization to every user-visible string in the app, with English (en) as the default and fallback. It is a pure resource + UI change within `:shared` — no new Gradle submodule, no domain or data layer changes, no new library dependencies.

**Resource structure**

Compose Multiplatform's built-in resource system (`org.jetbrains.compose.components:components-resources`) is already wired in `shared/build.gradle.kts`. Two XML files are required:

```
shared/src/commonMain/composeResources/
  values/strings.xml          ← English (default / fallback)
  values-es/strings.xml       ← Spanish overrides (same key set)
```

The build plugin generates a type-safe `Res.string.*` accessor. All composables call `stringResource(Res.string.<key>)`.

**Locale detection**

No platform-specific code is needed. The CMP resource framework resolves the device locale automatically on all three targets (Android via system locale, iOS via `NSLocale`, JS/WasmJS via `navigator.language`).

**String keys per screen**

_Shell / Navigation (MainScreen)_: `tab_report`, `tab_my_reports`, `tab_map`, `report_tab_headline`, `register_pothole_button`

_CameraScreen_: `location_required_title`, `location_required_body`, `camera_required_title`, `camera_required_body`, `cancel_button`, `photo_taken_dialog_title`, `photo_taken_dialog_body`, `keep_taking_button`, `retake_button`, `complete_button`

_PhotoReviewScreen_: `review_photos_title`, `no_photos_message`, `continue_button`, `cancel_content_description`, `photo_content_description`

_ReportFormScreen_: `new_report_title`, `address_dialog_title`, `address_dialog_body`, `got_it_button`, `title_field_label`, `description_field_label`, `submit_report_button` (shares `cancel_button` and `cancel_content_description` from camera)

_ThankYouScreen_: `thank_you_headline`, `thank_you_body`

_MyReportsScreen_: `my_reports_title`, `no_reports_message`, `status_sent`, `status_seen`, `status_pending`, `status_in_progress`, `status_resolved`, `status_discarded`

_ReportDetailScreen_: `report_detail_title`, `could_not_load_report`, `back_content_description`, `back_button`

_ReportsMapScreen_: `reports_map_title`

**Special case — StatusChip**

`ReportStatus.name.replace("_", " ")` must be replaced with an exhaustive `when` expression that maps each enum variant to its `Res.string.*` key. This is the only non-trivial code change beyond mechanical string replacements.

**Koin module impact**: none. No new ViewModels, UseCases, or repositories.

**User Story** *(Business Analyst)*

**Acceptance Criteria**

**UX/UI Proposal** *(Designer)*

---

### [FEAT-009] Light/Dark Color Theme

**Status**: `Done`

**Architect Notes**

Layer impact: Presentation only. No domain, data, or Koin changes required.

A single new file — `AppTheme.kt` — is introduced in `commonMain` at `shared/src/commonMain/kotlin/com/espert/reporteciudadano/`. It contains two private `ColorScheme` vals (`LightColorScheme` and `DarkColorScheme`) built with Material3's `lightColorScheme()` and `darkColorScheme()` factory functions, and a `@Composable fun AppTheme(content: @Composable () -> Unit)` wrapper that calls `isSystemInDarkTheme()` to select the active scheme and delegates to `MaterialTheme(colorScheme = ..., content = content)`.

`App.kt` changes one call site: `MaterialTheme { }` becomes `AppTheme { }`. No other file changes.

**Pinterest palette note**: the URL `https://pin.it/48pzpEuur` is inaccessible without authentication. The pin title references a Guanajuato, Mexico city colour palette. A civic-appropriate palette has been designed (deep civic greens as primary, warm ochre as tertiary nod to Guanajuato, neutral grays for surface). If the user can share extracted hex codes from the board, the Designer should revise the palette before the Developer writes `AppTheme.kt`. The full color table (all 24 M3 roles, both light and dark) is documented in `architecture.md` under FEAT-009.

No new Gradle submodule. No new library dependencies — `material3` and `isSystemInDarkTheme` are already available in `commonMain`.

**New domain models**: none.
**New repository interfaces**: none.
**Platform-specific concerns**: none — `isSystemInDarkTheme()` resolves correctly on all three targets in CMP commonMain.
**Koin module plan**: none required.

**User Story** *(Business Analyst)*
> As a citizen,
> I want the app to display in a color theme that matches my device's light or dark mode setting,
> So that the interface is comfortable to read in any lighting condition and feels consistent with the rest of my device.

**Acceptance Criteria**
- [ ] Given the device is in light mode, when the app launches, then the light color scheme is applied across all screens (civic green primary `#1A6B4A`, warm ochre tertiary `#8B5E3C`, off-white background `#F8FAF7`).
- [ ] Given the device is in dark mode, when the app launches, then the dark color scheme is applied across all screens (mint primary `#8FCFAC`, warm gold tertiary `#F4BA87`, near-black background `#1A1C1A`).
- [ ] Given the user changes the system theme while the app is open, when they return to the app, then the displayed theme matches the updated system setting.
- [ ] All text and interactive element color combinations in both schemes meet the WCAG AA minimum contrast ratio of 4.5:1 (e.g., `onPrimary` on `primary`, `onBackground` on `background`, `onSurface` on `surface`).
- [ ] No composable in the codebase uses a hard-coded color value; every color reference is resolved through a Material3 theme role token (e.g., `MaterialTheme.colorScheme.primary`).
- [ ] The theme applies identically on Android, iOS, and Web — there are no platform-specific color overrides.

**UX/UI Proposal** *(Designer — approved)*

This feature has no new screens or navigation. The proposal covers palette review, component token mapping, and the color swatch reference the developer needs to write `AppTheme.kt`.

**Palette Review**

The 24-role palette from `architecture.md` is confirmed with no changes. The rationale for each decision is recorded below.

Brand fit: deep civic green (`#1A6B4A`) as `primary` signals institutional trust and environmental care — appropriate for a municipal reporting tool. The warm ochre tertiary (`#8B5E3C` light / `#F4BA87` dark) introduces Latin-American warmth without compromising legibility. The off-white background (`#F8FAF7`) has a subtle green tint that ties the surfaces back to the primary hue without making them look colored.

WCAG AA checks (4.5:1 minimum for normal text):

| Pair | Light | Dark |
|---|---|---|
| `primary` / `onPrimary` | `#1A6B4A` on `#FFFFFF` ~5.8:1 PASS | `#8FCFAC` on `#1A1C1A` ~8.6:1 PASS |
| `onBackground` / `background` | `#1A1C1A` on `#F8FAF7` ~18:1 PASS | `#E2E3DE` on `#1A1C1A` ~17:1 PASS |
| `onSurface` / `surface` | same as background pair | same as background pair |
| `onPrimaryContainer` / `primaryContainer` | `#002114` on `#AAEBC7` ~10:1 PASS | `#AAEBC7` on `#005237` ~7.4:1 PASS |
| `onSurfaceVariant` / `surfaceVariant` | `#404940` on `#DCE5D8` ~8.2:1 PASS | `#BFC9BB` on `#404940` ~5.8:1 PASS |
| `tertiary` on `background` (informational use) | `#8B5E3C` on `#F8FAF7` ~6.7:1 PASS | `#F4BA87` on `#1A1C1A` ~9.2:1 PASS |
| `error` / `onError` | `#BA1A1A` on `#FFFFFF` ~5.3:1 PASS | `#FFB4AB` on `#690005` ~7.1:1 PASS |

All checked pairs clear AA. No hex values need adjustment.

**Light scheme swatch**

| Role | Hex | Swatch |
|---|---|---|
| `primary` | `#1A6B4A` | deep civic green |
| `onPrimary` | `#FFFFFF` | white |
| `primaryContainer` | `#AAEBC7` | soft mint |
| `onPrimaryContainer` | `#002114` | near-black |
| `secondary` | `#4A6741` | muted olive |
| `onSecondary` | `#FFFFFF` | white |
| `secondaryContainer` | `#C9EDBB` | pale sage |
| `onSecondaryContainer` | `#0C2006` | dark green |
| `tertiary` | `#8B5E3C` | warm ochre |
| `onTertiary` | `#FFFFFF` | white |
| `tertiaryContainer` | `#FFD8B8` | warm peach |
| `onTertiaryContainer` | `#311200` | dark brown |
| `error` | `#BA1A1A` | standard M3 red |
| `onError` | `#FFFFFF` | white |
| `errorContainer` | `#FFDAD6` | blush |
| `onErrorContainer` | `#410002` | dark red |
| `background` | `#F8FAF7` | off-white with green tint |
| `onBackground` | `#1A1C1A` | near-black |
| `surface` | `#F8FAF7` | same as background |
| `onSurface` | `#1A1C1A` | near-black |
| `surfaceVariant` | `#DCE5D8` | muted green-gray |
| `onSurfaceVariant` | `#404940` | dark gray-green |
| `outline` | `#707970` | mid gray-green |
| `outlineVariant` | `#BFC9BB` | light gray-green |

**Dark scheme swatch**

| Role | Hex | Swatch |
|---|---|---|
| `primary` | `#8FCFAC` | light mint |
| `onPrimary` | `#003824` | dark green |
| `primaryContainer` | `#005237` | deep green container |
| `onPrimaryContainer` | `#AAEBC7` | soft mint |
| `secondary` | `#AEDAD0` | soft cyan-green |
| `onSecondary` | `#1A3729` | dark teal |
| `secondaryContainer` | `#314E3E` | deep olive container |
| `onSecondaryContainer` | `#C9EDBB` | pale sage |
| `tertiary` | `#F4BA87` | warm gold |
| `onTertiary` | `#4C2900` | dark brown |
| `tertiaryContainer` | `#6C3F1F` | medium brown |
| `onTertiaryContainer` | `#FFD8B8` | warm peach |
| `error` | `#FFB4AB` | soft red |
| `onError` | `#690005` | dark red |
| `errorContainer` | `#93000A` | deep red |
| `onErrorContainer` | `#FFDAD6` | blush |
| `background` | `#1A1C1A` | near-black |
| `onBackground` | `#E2E3DE` | off-white |
| `surface` | `#1A1C1A` | near-black |
| `onSurface` | `#E2E3DE` | off-white |
| `surfaceVariant` | `#404940` | dark gray-green |
| `onSurfaceVariant` | `#BFC9BB` | light gray-green |
| `outline` | `#8A9389` | mid gray |
| `outlineVariant` | `#404940` | same as surfaceVariant |

**Component Token Mapping**

The table below maps each existing component to the M3 color roles it will use under the new scheme. No component needs to change its variant — the mapping is accurate for the current usages.

| Component | Relevant roles | Notes |
|---|---|---|
| `NavigationBar` + `NavigationBarItem` | `navigationBarContainerColor` → `surface`; selected indicator → `secondaryContainer`; selected icon/label → `onSecondaryContainer`; unselected → `onSurfaceVariant` | No variant change needed. The default M3 `NavigationBar` container maps to `surface`, which resolves correctly under both schemes. |
| `ElevatedCard` (report list items) | container → `surfaceVariant` at 1dp elevation tint; content text → `onSurface` | No variant change. `ElevatedCard` uses surface tonal elevation by default — it will automatically adopt `surfaceVariant` as the tonal base. |
| `SuggestionChip` (StatusChip) | `tertiaryContainer` / `onTertiaryContainer` for SENT; `secondaryContainer` / `onSecondaryContainer` for SEEN; `errorContainer` / `onErrorContainer` for PENDING; `primaryContainer` / `onPrimaryContainer` for IN_PROGRESS; `surfaceVariant` / `onSurfaceVariant` for RESOLVED and DISCARDED | Confirm chip `containerColor` and `labelColor` are set explicitly per status — do not rely on the default chip container, which would flatten all statuses to the same color. |
| `OutlinedTextField` | focused indicator and label → `primary`; unfocused outline → `outline`; cursor → `primary`; error state → `error` | No change needed. M3 `OutlinedTextField` defaults follow these role assignments automatically. |
| `CenterAlignedTopAppBar` | `containerColor` → `surface`; title → `onSurface`; icon → `onSurfaceVariant` | No change. The `ReportsMapScreen` variant uses `surface.copy(alpha = 0.85f)` — confirm alpha is applied after theme resolution, not to the raw hex. |
| `Button` | `containerColor` → `primary`; `contentColor` → `onPrimary` | No change. |
| `OutlinedButton` | border → `outline`; content → `primary` | No change. |
| `TextButton` | content → `primary` | No change. |
| `LinearProgressIndicator` (ThankYouScreen) | `trackColor` → `surfaceVariant`; `progressColor` → `primary` | No variant change. Confirm `progressColor` is not overridden with a hardcoded value. |

No component needs to switch to a tonal or container variant. The palette is designed so that the existing component choices already land on roles that provide correct contrast in both schemes.

**Implementation note**: the developer should audit every file in `feature/` for any `Color(0xFF...)` literals or `Color.White` / `Color.Black` usages before closing this feature. The acceptance criterion requires zero hardcoded color values.

---

### [FEAT-010] Location Service Gate

**Status**: `Done`

**Architect Notes**

Layer impact: Presentation (CameraScreen, CameraState, CameraIntent, CameraViewModel) and platform layer (two new `expect fun` declarations with four `actual` implementations each). No domain model changes. No data layer changes.

**The gap being closed**: the app already gates on location *permission* (user's OS dialog answer) but not on whether the device location service (GPS/network provider) is *enabled* in device settings. A user who grants the permission but has location services turned off in settings will silently get a `null` location, producing a report with no coordinates.

**New platform contract** — file `shared/src/commonMain/kotlin/com/espert/reporteciudadano/platform/LocationStatus.kt`:
- `expect fun isLocationEnabled(): Boolean` — synchronous check, no side effects
- `expect fun openLocationSettings()` — fire-and-forget platform side-effect to open device location settings

Actuals:
- `androidMain`: `LocationManager.isProviderEnabled(GPS_PROVIDER) || isProviderEnabled(NETWORK_PROVIDER)`; `openLocationSettings` launches `Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)`
- `iosMain`: `CLLocationManager.locationServicesEnabled()`; `openLocationSettings` calls `UIApplication.openURL("App-Prefs:root=LOCATION_SERVICES")`
- `jsMain` / `wasmJsMain`: `isLocationEnabled` returns `true`; `openLocationSettings` is a no-op

**CameraState change**: add `val locationDisabled: Boolean = false`

**CameraIntent change**: add `object LocationServiceDisabled : CameraIntent()` (the actual trigger path goes through a `LocationGranted` intent the Developer introduces when lifting `locationChecked` into the ViewModel)

**CameraViewModel change**: after location permission is confirmed as granted, call `isLocationEnabled()`; if false, update state with `locationDisabled = true`

**CameraScreen change**: add a `LocationDisabledContent` composable (same structure as `LocationDeniedContent`) rendered when `state.locationDisabled` is true; contains a "Go to Settings" button that calls `openLocationSettings()` and a "Cancel" button that calls `onCancel()`

**New string keys** (must be added to both `values/strings.xml` and `values-es/strings.xml`):
- `location_service_disabled_title`
- `location_service_disabled_body`
- `go_to_settings_button`

Full string values and Spanish translations are documented in `architecture.md` under FEAT-010.

**Android context note**: the Android actual of `isLocationEnabled()` needs a `Context`. It should be obtained via the Koin `androidContext()` binding (consistent with existing project patterns in `DatabaseDriverFactory.android.kt`). The `expect` signature in `commonMain` must remain context-free.

No new Gradle submodule. No new library dependencies. No new Koin modules. No module dependency cycles.

**User Story** *(Business Analyst)*
> As a citizen,
> I want to see a clear message when my device's location service is turned off before I try to report a pothole,
> So that I understand why the camera cannot open and I know exactly what to do to fix it.

**Acceptance Criteria**
- [ ] Given location permission has been granted and location services are disabled on the device, when the citizen taps "Register pothole", then the camera does not open and a `LocationDisabledContent` screen is shown with a title, an explanatory body message, a "Go to Settings" button, and a "Cancel" button.
- [ ] Given the `LocationDisabledContent` screen is visible, when the citizen taps "Go to Settings", then the device navigates directly to the location settings screen (Android: Location Source Settings; iOS: Location Services settings) without crashing.
- [ ] Given the `LocationDisabledContent` screen is visible, when the citizen taps "Cancel", then the camera flow exits cleanly and the citizen is returned to the main Report tab with no error or stale state.
- [ ] Given the citizen tapped "Go to Settings" and enabled location services and then returned to the app, when they tap "Register pothole" again, then the location service check passes and the camera opens normally.
- [ ] The location service check occurs after location permission is confirmed as granted and before the camera composable is rendered — the citizen never sees the camera UI when location services are off.
- [ ] On Web (JS/WasmJS targets), the location service gate is not shown; the browser's own Geolocation API handles the unavailability of location, and the camera flow proceeds uninterrupted.

**UX/UI Proposal** *(Designer — approved)*

**Screen: LocationDisabledContent**

- **Purpose**: inform the citizen that their device location service is turned off and direct them to device settings so they can enable it before proceeding. This is a recoverable error — the user has a clear action path.

- **Layout**: same top-level structure as the existing `LocationDeniedContent` and `CameraDeniedContent` — a full-screen `Box` centered, containing a `Column` with `horizontalAlignment = CenterHorizontally` and `verticalArrangement = spacedBy(16.dp)`, padded 32dp on all sides. No `Scaffold`, no `TopAppBar`. This is consistent with the existing blocked-state pattern in `CameraScreen`.

- **Differentiation from `LocationDeniedContent`**: do NOT reuse `LocationDeniedContent` verbatim. The two states are semantically different and require different call-to-action hierarchies:

  | Aspect | `LocationDeniedContent` | `LocationDisabledContent` |
  |---|---|---|
  | Problem | OS permission permanently denied | Device toggle turned off (fixable in one tap) |
  | Icon | `LocationOn` (current — acceptable for denied) | `LocationOff` (location is clearly inactive) |
  | Primary action | none — only Cancel | "Go to Settings" filled `Button` |
  | Secondary action | `OutlinedButton` Cancel | `OutlinedButton` Cancel |

  Using `LocationOff` over `LocationOn` is important: `LocationOn` already appears on the `ReportDetailScreen` address row as a decorative indicator that coordinates are present. Reusing it here would create visual ambiguity. `LocationOff` (available in `androidx.compose.material.icons.filled` as `Icons.Default.LocationOff` or in extended icons) makes the "service is inactive" state unambiguous at a glance.

- **Components**:

  ```
  Box(fillMaxSize, contentAlignment = Center)
    Column(horizontalAlignment = CenterHorizontally, spacedBy(16.dp), padding(32.dp))
      Icon(Icons.Default.LocationOff, size = 48.dp, tint = MaterialTheme.colorScheme.error)
      Text(title, style = titleLarge, textAlign = Center)
      Text(body, style = bodyMedium, textAlign = Center)
      Button(onClick = { openLocationSettings() })       // primary action, full-width
        Text("Go to Settings")
      OutlinedButton(onClick = onCancel)                 // secondary action, full-width
        Text("Cancel")
  ```

  Both buttons should be `Modifier.fillMaxWidth()` for comfortable touch targets, consistent with other full-screen error states in the app (e.g., `PhotoReviewScreen`'s Continue button).

- **Icon tint**: use `MaterialTheme.colorScheme.error` for the `LocationOff` icon. This matches the "something is wrong that needs fixing" semantic. The existing `LocationDeniedContent` uses the default icon tint (`onSurface`); this feature's icon should be visually stronger to communicate urgency.

- **Button hierarchy**:
  - Primary action: filled `Button` — "Go to Settings". This is the recommended, recoverable action. It calls `openLocationSettings()` (the platform expect function) and does nothing else — the user leaves the app, enables location, and returns manually to re-enter the flow.
  - Secondary action: `OutlinedButton` — "Cancel". Calls `onCancel()`, exits the camera flow cleanly, returns to the main Report tab.

- **String resources** — exact values to use (these become new keys in `values/strings.xml` and `values-es/strings.xml`):

  | Key | English value | Spanish value |
  |---|---|---|
  | `location_service_disabled_title` | Location Service Off | Servicio de ubicación desactivado |
  | `location_service_disabled_body` | Location services are turned off on your device. Please enable them in Settings to continue. | Los servicios de ubicación están desactivados en tu dispositivo. Actívalos en Configuración para continuar. |
  | `go_to_settings_button` | Go to Settings | Ir a Configuración |

  Note: `cancel_button` already exists from FEAT-002/FEAT-008 — reuse it for the Cancel label here. No new key needed.

- **States**: this composable has a single state — there is no loading, empty, or success variant. It is either shown (service disabled) or not rendered at all (service enabled).

- **Navigation**:
  - Entry point: `CameraScreen` renders `LocationDisabledContent` in place of all other content when `state.locationDisabled == true`. It appears after location permission is confirmed as granted but before the camera composable is shown — the sequence is: `LocationDenied` check → `CameraDenied` check → `LocationDisabled` check → camera flow.
  - Exit points: (1) "Go to Settings" — leaves the app via platform intent/URL; screen stays visible until the user backgrounds and returns, at which point they re-tap "Register pothole" to restart the check. (2) "Cancel" — `onCancel()` navigates back to the Report tab root.

- **Platform scope**: this composable is rendered in `commonMain` and is always compiled. The platform gate is enforced by the `isLocationEnabled()` expect function, which returns `true` on `jsMain` and `wasmJsMain` — meaning `state.locationDisabled` is never set to `true` on Web. The `LocationDisabledContent` composable itself is never shown on Web; no platform `if` guards are needed in the composable layer.

- **ASCII wireframe**:

  ```
  ┌──────────────────────────────────┐
  │                                  │
  │                                  │
  │          [LocationOff]           │
  │           (48dp, error)          │
  │                                  │
  │      Location Service Off        │  ← titleLarge, centered
  │                                  │
  │   Location services are turned   │
  │   off on your device. Please     │  ← bodyMedium, centered
  │   enable them in Settings to     │
  │   continue.                      │
  │                                  │
  │  ┌────────────────────────────┐  │
  │  │       Go to Settings       │  │  ← filled Button (primary)
  │  └────────────────────────────┘  │
  │  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐  │
  │  │          Cancel            │  │  ← OutlinedButton
  │  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘  │
  │                                  │
  └──────────────────────────────────┘
  ```

---

### [FEAT-011] Offline-First Location

**Status**: `Done`

**Architect Notes**

This is a cross-cutting refactor touching the domain model, the SQLDelight database schema, the report submission flow, and the report detail display flow. Three separate problems are corrected in one coherent change.

**What is broken today**

1. `CitizenReport.address: String` exists in the domain model and is persisted in `ReportEntity.address TEXT NOT NULL`. A stale, potentially wrong address string is stored forever.
2. `ReportFormViewModel.submit()` falls back silently to `GeoLocation(0.0, 0.0)` when no photo has a GPS fix. A report can be submitted with null-equivalent coordinates and a "Location unavailable" address string — both incorrect.
3. `ReportDetailScreen` reads `report.address` directly from the persisted domain model. There is no on-demand geocoding and no offline fallback.

**Domain model changes**

- `CitizenReport`: remove `address: String`. The model carries only `location: GeoLocation` (coordinates).
- New sealed interface `LocationDisplay` in `domain/model/LocationDisplay.kt`:
  - `data class Address(val text: String)`
  - `data class Coordinates(val latitude: Double, val longitude: Double)`
  - `data object Loading`
  This type lives in the domain layer but is only consumed by the presentation layer. It is never persisted.
- `GeoLocation.kt`: add a pure Kotlin extension function `formatCoordinates()` that formats lat/lon as `"18.4861° N, 69.9312° W"`.

**Database migration (SQLDelight)**

- `AppDatabase.sq`: remove `address TEXT NOT NULL` from `ReportEntity`; update `insertReport` to 7 columns.
- New file `1.sqm`: migration script that recreates `ReportEntity` without the `address` column and copies existing rows (using the table-rename pattern for SQLite compatibility).
- `schemaVersion` in `shared/build.gradle.kts`: increment from `1` to `2`.
- `ReportRepositoryImpl`: remove `address` parameter from `insertReport` call; remove `address = entity.address` from mapping in `getAll()` and `getById()`.

**Connectivity detection**

New `expect fun isNetworkAvailable(): Boolean` in `commonMain/platform/NetworkStatus.kt`. Four actuals:
- Android: `ConnectivityManager` + `NET_CAPABILITY_INTERNET`; `Context` injected via Koin `KoinComponent` pattern (same as `LocationStatusHelper`).
- iOS: `NWPathMonitor` current path status.
- JS: `navigator.onLine`.
- WasmJS: `navigator.onLine`.

**Navigation gate (coordinate availability)**

`CapturedPhoto.exifLocation: GeoLocation?` remains nullable at the capture layer. The gate is in `PhotoReviewScreen` / `CameraViewModel`:
- New `CameraState.noLocationOnPhotos: Boolean = false`.
- New `CameraIntent.NoLocationOnPhotos`.
- When the user taps "Continue" in `PhotoReviewScreen`, the screen checks `photos.none { it.exifLocation != null }`. If true, it dispatches `NoLocationOnPhotos` and the ViewModel sets `state.noLocationOnPhotos = true`. An inline error message is shown below the photo strip and the Continue button is disabled.
- If at least one photo has a non-null `exifLocation`, navigation proceeds normally. The first non-null `exifLocation` is the report's `GeoLocation`.

**Report form changes**

- `ReportFormState`: replace `address: String` + `isLoadingAddress: Boolean` with `locationDisplay: LocationDisplay = LocationDisplay.Loading`.
- `ReportFormViewModel.init()`: receives the resolved `GeoLocation` (not the photo list). Calls `isNetworkAvailable()`. If online, launches `ReverseGeocodeUseCase`; on success sets `LocationDisplay.Address`; on failure falls back to `LocationDisplay.Coordinates`. If offline, immediately sets `LocationDisplay.Coordinates`.
- `ReportFormViewModel.submit()`: constructs `CitizenReport` without `address`; uses the stored `GeoLocation` (never falls back to `0.0, 0.0`).
- `ReportFormScreen`: address `ElevatedCard` renders based on `state.locationDisplay` — spinner for `Loading`, address text for `Address`, formatted coordinates for `Coordinates`.

**Report detail changes**

- `ReportDetailState`: add `locationDisplay: LocationDisplay = LocationDisplay.Loading`.
- `ReportDetailViewModel`: add `ReverseGeocodeUseCase` as a second dependency. After loading the report, launch a second coroutine: call `isNetworkAvailable()`, then geocode-or-coordinates logic, update `locationDisplay`.
- `ReportDetailScreen`: replace `Text(report.address, ...)` with a `when (state.locationDisplay)` renderer.
- Koin binding: `viewModel { (reportId: String) -> ReportDetailViewModel(reportId, get(), get()) }`.

**New string keys**

| Key | English | Spanish |
|---|---|---|
| `no_location_on_photos_title` | No Location Data | Sin datos de ubicación |
| `no_location_on_photos_body` | None of your photos have GPS coordinates. Please retake photos with location services enabled. | Ninguna de tus fotos tiene coordenadas GPS. Vuelve a tomar las fotos con los servicios de ubicación activados. |
| `location_loading` | Resolving location... | Resolviendo ubicación... |

Existing keys `address_dialog_title` and `address_dialog_body` remain; the BA should review their wording to reflect the offline-coordinates fallback scenario.

**Modules affected**: `:shared` only. No new Gradle submodule. No new library dependencies.

**Files added**: `domain/model/LocationDisplay.kt`, `platform/NetworkStatus.kt` (commonMain expect + 4 actuals), `1.sqm` (migration).

**Koin impact**: one changed binding (`ReportDetailViewModel`). No new modules.

**User Story** *(Business Analyst)*
> As a citizen,
> I want my pothole report to always capture precise GPS coordinates and display my location in the best available format (human-readable address when online, formatted coordinates when offline),
> So that every report I submit carries an accurate, verifiable location regardless of my network state, and I am never able to submit a report with missing or fabricated coordinates.

**Acceptance Criteria**

**Coordinate capture gate (PhotoReviewScreen)**

- [ ] Given none of the captured photos have a GPS fix embedded in their EXIF data, when the citizen taps "Continue" on the photo review screen, then the Continue button becomes disabled, an inline error message appears directly below the photo strip reading "None of your photos have GPS coordinates. Please retake photos with location services enabled.", and no navigation to the report form occurs.
- [ ] Given at least one captured photo has a non-null EXIF GPS fix, when the citizen taps "Continue", then navigation proceeds to the report form using the first photo's GPS coordinates as the report location, and no error message is shown.
- [ ] The inline GPS error message is dismissed automatically if the citizen deletes a photo with no GPS fix and a remaining photo does have a fix; the Continue button re-enables without requiring a manual dismiss action.

**Disclaimer dialog (ReportFormScreen)**

- [ ] Given the citizen enters the report form screen, when the disclaimer dialog appears, then its body text reads: "The location shown may be an approximate address resolved from your photo's GPS coordinates, or the raw coordinates if network is unavailable." — replacing the previous wording that only covered the online case.
- [ ] The disclaimer dialog appears on every entry to the report form (both online and offline sessions) and is dismissed by a single "Got it" button.

**Online location display (ReportFormScreen and ReportDetailScreen)**

- [ ] Given the device has network connectivity when the report form or report detail screen loads, when reverse geocoding completes successfully, then the address card displays the human-readable address text (e.g., "Av. Independencia 45, Centro, Guanajuato") with no label prefix.
- [ ] Given the device has network connectivity and reverse geocoding is in progress, then the address card displays a spinner (`CircularProgressIndicator`) and the text "Resolving location..." in place of the address; the Submit button remains enabled throughout the geocoding wait.
- [ ] Given the device has network connectivity and reverse geocoding fails (network error or no result), then the address card falls back to the offline display format described below, with no error dialog shown to the citizen.

**Offline location display (ReportFormScreen and ReportDetailScreen)**

- [ ] Given the device has no network connectivity when the report form or report detail screen loads, then the address card immediately shows a label "Location (offline)" followed by the formatted coordinate string (e.g., "18.4861° N, 69.9312° W") with no spinner and no geocoding request made.
- [ ] The formatted coordinate string follows the pattern `{abs(lat)}° N|S, {abs(lon)}° E|W`, with four decimal places for each value, matching the output of `GeoLocation.formatCoordinates()`.

**Database storage**

- [ ] After this feature ships, the `ReportEntity` table no longer contains an `address` column; only `latitude` and `longitude` are stored as the canonical location record.
- [ ] Citizens who had reports saved under the previous schema (version 1) experience a transparent migration to schema version 2: their existing reports load correctly in My Reports and Report Detail with coordinates shown, and no data is lost.
- [ ] The domain model `CitizenReport` no longer carries an `address` field; address resolution is ephemeral and never written to or read from the database.

**Submit integrity**

- [ ] The report form's Submit button remains gated solely on the Title and Description fields being non-empty; it is not disabled by geocoding state, network state, or the type of location display shown.
- [ ] On submit, the report is saved with the GPS coordinates from the photo EXIF data; the value `GeoLocation(0.0, 0.0)` is never saved as a report's location.

**UX/UI Proposal** *(Designer)*

This feature touches four surfaces. Each is described below with an ASCII wireframe, component list, state variants, and style notes.

---

#### Surface 1 — PhotoReviewScreen: GPS coordinate gate

**Purpose**: prevent the citizen from advancing to the report form when none of the captured photos carry GPS coordinates. The gate is non-modal — it appears inline, below the photo strip, so the citizen can see their photos and understand what action to take.

**Layout change**: the existing `Scaffold` layout is unchanged. A new slot is inserted between the `LazyRow` and the bottom bar. This slot is conditionally rendered only when `state.noLocationOnPhotos == true`.

**Continue button**: the existing `Button` already uses `enabled = photos.isNotEmpty()`. The gate adds a second condition: `enabled = photos.isNotEmpty() && !state.noLocationOnPhotos`. When the gate is active the button is visually disabled (Material3 default disabled alpha) — no tooltip or additional indicator needed.

**Inline error slot**: placed in the `Column` body immediately below the `LazyRow`, before any remaining vertical space. It uses a `Row` with `verticalAlignment = CenterVertically` and `horizontalArrangement = spacedBy(8.dp)`, padded `horizontal = 16.dp, top = 8.dp`.

Components inside the error row:
- `Icon(Icons.Default.GpsOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))` — `GpsOff` (filled, extended icons) is preferred over `LocationOff` because `LocationOff` is already reserved for the `LocationDisabledContent` gate introduced in FEAT-010. Using a distinct icon prevents visual collision between the two blocked states.
- `Text(stringResource(Res.string.no_location_on_photos_body), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)`

Auto-clear: the error row is rendered conditionally on `state.noLocationOnPhotos`. When the citizen deletes a photo with no GPS fix and at least one remaining photo has a fix, the ViewModel sets `noLocationOnPhotos = false`, which causes the row to disappear and the Continue button to re-enable — no animation needed, the recomposition is fast enough to feel instant.

**States summary**:

| State | Photo strip | Error row | Continue button |
| --- | --- | --- | --- |
| No photos | empty message (existing) | hidden | disabled (existing gate) |
| Photos present, all have GPS | visible | hidden | enabled |
| Photos present, none have GPS | visible | visible, error style | disabled |
| Photos present, mixed GPS | visible | hidden | enabled |

**ASCII wireframe (gate active)**:

```
┌──────────────────────────────────────┐
│  [X]       Review Photos             │  ← CenterAlignedTopAppBar
├──────────────────────────────────────┤
│                                      │
│  [ photo ] [ photo ] [ photo ]  →    │  ← LazyRow, 160dp thumbnails
│                                      │
│  [GpsOff] None of your photos have  │  ← error Row, bodySmall, error color
│           GPS coordinates. Please   │
│           retake photos with        │
│           location services enabled.│
│                                      │
│                                      │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐  │
│  │           Continue              │  │  ← Button, disabled state
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘  │
└──────────────────────────────────────┘
```

**Navigation**: no change to entry or exit points. The gate blocks only the `onContinue` path; `onCancel` (the Close icon in the top bar) remains always active.

**String resource used**: `no_location_on_photos_body` (already defined in Architect Notes). No additional string keys needed for this surface — the icon communicates the error category and the body text provides the full message.

---

#### Surface 2 — Disclaimer dialog: updated body copy

**Purpose**: inform the citizen that the address shown may be a human-readable address (when online) or raw coordinates (when offline), replacing the previous wording that only described the online case.

**No structural change**: the `AlertDialog` trigger (the info icon on the address card), the title key (`address_dialog_title`), the "Got it" button, and the `onDismissRequest = {}` lock are all unchanged. Only the body text is updated.

**Body copy change**:

| | Value |
| --- | --- |
| Key | `address_dialog_body` |
| New English value | The location shown may be an approximate address resolved from your photo's GPS coordinates, or the raw coordinates if network is unavailable. |
| New Spanish value | La ubicación mostrada puede ser una dirección aproximada obtenida de las coordenadas GPS de tu foto, o las coordenadas sin procesar si no hay conexión a internet. |

The updated text fits within three lines at standard `bodyMedium` on a 360dp-wide screen. No layout change is needed. The `AlertDialog` intrinsic sizing handles wrapping automatically.

**Component**: `AlertDialog` — same as current implementation in `ReportFormScreen.kt` lines 38–48. The only code change is in `values/strings.xml` and `values-es/strings.xml`.

---

#### Surface 3 — Address ElevatedCard: three-state renderer

This card is used identically in both `ReportFormScreen` (lines 104–114) and `ReportDetailScreen` (lines 81–87). The proposal describes a shared `LocationDisplayCard` composable that both screens import, replacing the duplicated `ElevatedCard` + `Row` inline code.

**Purpose of the shared composable**: display the best available location representation for the current network and geocoding state, with a consistent layout across the form and detail screens.

**Composable signature**:

```
@Composable
fun LocationDisplayCard(locationDisplay: LocationDisplay, modifier: Modifier = Modifier)
```

**Layout**: `ElevatedCard(modifier)` wrapping a `Row(Modifier.padding(16.dp), verticalAlignment = CenterVertically, horizontalArrangement = spacedBy(12.dp))`.

The leading `Icon(Icons.Default.LocationOn, tint = MaterialTheme.colorScheme.primary)` is always present across all three states. It anchors the card's identity as a location element and provides a consistent left-edge alignment point.

**State A — Loading (`LocationDisplay.Loading`)**:

Content after the leading icon:
- `CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)` — inline with the icon, 20dp to match the current isLoadingAddress spinner in `ReportFormScreen.kt` line 111.
- `Text(stringResource(Res.string.location_loading), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)` — greyed label using `onSurfaceVariant` to signal a transient, not-yet-resolved state. This is the same color role used for placeholder text in `OutlinedTextField`.

```
┌─────────────────────────────────────┐  ElevatedCard
│  [LocationOn]  ◌  Resolving         │  ← CircularProgressIndicator + bodyMedium/onSurfaceVariant
│                   location...       │
└─────────────────────────────────────┘
```

**State B — Address (`LocationDisplay.Address`)**:

Content after the leading icon:
- `Text(address.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)` — full street address, same visual as the current `state.address` text in `ReportFormScreen.kt` line 112.

No label prefix. This is the online, fully resolved state — the address speaks for itself.

```
┌─────────────────────────────────────┐  ElevatedCard
│  [LocationOn]  Av. Independencia    │  ← bodyMedium/onSurface
│                45, Centro,          │
│                Guanajuato           │
└─────────────────────────────────────┘
```

**State C — Coordinates (`LocationDisplay.Coordinates`)**:

Content after the leading icon: a `Column(verticalArrangement = spacedBy(2.dp))` to stack the label and the coordinate string vertically.

- First child: `Text("Location (offline)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)` — secondary, smaller label using `labelSmall` (M3 typography, 11sp) to distinguish it from the coordinate value below. Using `onSurfaceVariant` signals that this is a contextual annotation, not primary content.
- Second child: `Text(formatCoordinates(lat, lon), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)` — the formatted coordinate string (e.g., `18.4861° N, 69.9312° W`), same weight and color as the online address for visual consistency.

The two-line layout (label above, value below) avoids horizontal crowding on narrow screens and makes it easy to scan at a glance.

```
┌─────────────────────────────────────┐  ElevatedCard
│  [LocationOn]  Location (offline)   │  ← labelSmall/onSurfaceVariant
│                18.4861° N,          │  ← bodyMedium/onSurface
│                69.9312° W           │
└─────────────────────────────────────┘
```

**String resource for the offline label**: use a new key `location_offline_label` with value `Location (offline)` (English) and `Ubicación (sin conexión)` (Spanish). This is separate from `location_loading` (which is already defined in the Architect Notes).

**No animation between states**: the card content switches synchronously on recomposition. A crossfade animation would add visual complexity for a state that typically transitions only once (Loading → Address or Loading → Coordinates). Keep it simple; the `CircularProgressIndicator` already communicates the transient loading phase.

**Components used**:
- `ElevatedCard` — outer container, matches current usage in `ReportFormScreen.kt`
- `Row` — horizontal layout within the card
- `Icon` — `Icons.Default.LocationOn`, tint `MaterialTheme.colorScheme.primary`
- `CircularProgressIndicator` — Loading state, 20dp, strokeWidth 2dp
- `Text` — various typography roles as described above
- `Column` — nested inside Row for the Coordinates state label/value stack

---

#### Surface 4 — ReportDetailScreen: location row replacement

**Purpose**: display the same three-state `LocationDisplayCard` in the report detail info section, replacing the hardcoded `Row` + `Text(report.address)` at `ReportDetailScreen.kt` lines 81–87.

**Layout change**: the existing `Row(verticalAlignment = CenterVertically, horizontalArrangement = spacedBy(8.dp))` block containing `Icon(LocationOn)` + `Text(report.address)` is replaced entirely by a call to `LocationDisplayCard(state.locationDisplay, modifier = Modifier.fillMaxWidth())`.

The `LocationDisplayCard` carries its own `LocationOn` icon as part of its internal layout, so the outer `Row` and standalone `Icon` are removed. `Modifier.fillMaxWidth()` is passed to maintain the full-width layout of the info section.

**States**: same three states as Surface 3. The initial state when the detail screen loads is `LocationDisplay.Loading`; the ViewModel resolves it to `Address` or `Coordinates` in a coroutine launched after the report is loaded.

**Placement in the scrollable Column**: the `LocationDisplayCard` sits in the same vertical position as the current address `Row` — after `Text(description)` and before the bottom `Spacer`. No reordering of the surrounding elements.

```
  Column (scrollable, spacedBy 16.dp, padding horizontal 16.dp)
    StatusChip(report.status)
    Text(report.title, headlineSmall)
    Text(report.description, bodyLarge)
    LocationDisplayCard(state.locationDisplay)   ← replaces the inline Row
  Spacer(16.dp)
```

**Loading state appearance in context**:

```
┌──────────────────────────────────────┐
│  [←]       Report Detail            │  ← CenterAlignedTopAppBar
├──────────────────────────────────────┤
│  [ photo ] [ photo ]            →   │  ← LazyRow, 160dp
│                                      │
│  [SENT chip]                         │
│  Pothole on main avenue              │  ← headlineSmall
│  There is a large pothole...         │  ← bodyLarge
│                                      │
│  ┌───────────────────────────────┐   │
│  │  [LocationOn]  ◌  Resolving  │   │  ← ElevatedCard, Loading state
│  │                   location.. │   │
│  └───────────────────────────────┘   │
│                                      │
└──────────────────────────────────────┘
```

**No new navigation changes**: entry and exit points for `ReportDetailScreen` remain the same (back arrow returns to `MyReportsScreen` or `ReportsMapScreen`).

---

#### New string keys introduced by this proposal

| Key | English value | Spanish value |
| --- | --- | --- |
| `location_offline_label` | Location (offline) | Ubicación (sin conexión) |

Keys `no_location_on_photos_body`, `location_loading` are already defined in the Architect Notes and do not require new entries from the designer.

The `address_dialog_body` key already exists; only its value in `values/strings.xml` and `values-es/strings.xml` is updated (see Surface 2).

---

#### Material3 component summary

| Surface | Components |
| --- | --- |
| PhotoReviewScreen gate | `Scaffold`, `CenterAlignedTopAppBar`, `LazyRow`, `Button` (disabled), `Row`, `Icon` (`GpsOff`), `Text` |
| Disclaimer dialog | `AlertDialog` (value-only change, no component change) |
| LocationDisplayCard | `ElevatedCard`, `Row`, `Column`, `Icon` (`LocationOn`), `CircularProgressIndicator`, `Text` |
| ReportDetailScreen row | `LocationDisplayCard` (replaces inline `Row`) |

---

#### Color token reference

| Element | Token |
| --- | --- |
| GPS gate error icon | `MaterialTheme.colorScheme.error` |
| GPS gate error text | `MaterialTheme.colorScheme.error` |
| Location card icon (all states) | `MaterialTheme.colorScheme.primary` |
| Loading label text | `MaterialTheme.colorScheme.onSurfaceVariant` |
| Offline label text ("Location (offline)") | `MaterialTheme.colorScheme.onSurfaceVariant` |
| Address / coordinate value text | `MaterialTheme.colorScheme.onSurface` |

---

#### Adaptive layout notes

The `LocationDisplayCard` uses `Modifier.fillMaxWidth()` on both `ReportFormScreen` and `ReportDetailScreen`. On tablet and web layouts (where `NavigationRail` replaces `NavigationBar`), the card naturally fills its parent column width, which may be constrained to a max of 600dp by a wrapping `widthIn(max = 600.dp)` if the developer applies the standard adaptive container used elsewhere. No separate tablet-specific design is needed — the three-state card is readable at any width above 280dp.

---

**Proposal status**: complete. Ready for BA review before marking the feature pipeline stage as approved.

---

## Template (for new features)

### [FEAT-000] Feature Title

**Status**: `Draft` | `Design` | `Ready` | `In Progress` | `Done`

**Architect Notes**

**User Story** *(Business Analyst)*
> As a [user], I want [action], so that [benefit].

**Acceptance Criteria**
- [ ] ...

**UX/UI Proposal** *(Designer)*
