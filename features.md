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

### [FEAT-012] Greeting in test.md

**Status**: `Done`

**Architect Notes**

Layer impact: documentation/test file only. No Kotlin source, no Gradle modules, no dependencies, no platform-specific code. `test.md` is a scratchpad file tracked in the repo for integration tests and exploratory notes. A greeting line at the top of the file confirms the file is writable by the issue pipeline and validates the end-to-end feature flow on a non-production artifact.

No new Gradle submodule. No new library dependencies. No domain model changes. No Koin module changes.

**User Story** *(Business Analyst)*
> As a developer,
> I want to see a greeting message at the top of `test.md`,
> So that I can verify the issue pipeline (Architect → BA → Designer → Developer → QA → Versioning) executes correctly on a safe, non-production file.

**Acceptance Criteria**
- [ ] `test.md` contains a clearly visible greeting line (e.g., `¡Hola, ReporteCiudadano!`).
- [ ] The greeting is placed at the top of the file, before existing content.
- [ ] No other file in the repository is modified except `test.md`, `features.md`, and `changelog.md`.
- [ ] The commit message follows Conventional Commits format.

**UX/UI Proposal** *(Designer — approved)*

This feature has no screen or UI component. The "design" deliverable is the exact text of the greeting and its placement within `test.md`.

- **Greeting text**: `¡Hola, ReporteCiudadano! 👋` — uses the project name to make the greeting identifiable at a glance; the wave emoji adds warmth without introducing external imagery.
- **Placement**: first line of `test.md`, followed by a blank line separator before the existing content. This follows the standard Markdown convention for top-level headings/intros.
- **Format**: plain text line (no Markdown heading prefix needed — `test.md` is not a published document).

---

### [FEAT-013] Cloud Sync — DynamoDB + S3

**Status**: `Done`

**Architect Notes**

FEAT-013 adds reliable, offline-tolerant cloud synchronisation of all citizen reports and their associated photos to AWS infrastructure. Every `CitizenReport` row is uploaded to a DynamoDB table and every photo file (stored as a local path in `ReportPhotoEntity`) is uploaded to an S3 bucket. The sync pipeline runs entirely in the background; the user is only aware of it when a failure notification appears after five consecutive failed attempts.

**Module structure.** A new Gradle submodule `:feature:cloudsync` is introduced as an Android Library. All sync domain types (`SyncStatus`, `SyncRecord`, `CloudSyncRepository`, and five use cases) live in its `commonMain`. Platform-specific scheduler and notification code lives in the respective source sets. `:feature:cloudsync` depends on `:shared` for domain models but on no other feature module, keeping the dependency graph acyclic. `:androidApp` gains a single `implementation(projects.feature.cloudsync)` dependency. One new version-catalog entry is needed: `androidx.work:work-runtime-ktx` (WorkManager, latest stable).

**Domain and persistence.** Two new columns are appended to `ReportEntity` in a new SQLDelight migration file `2.sqm` (two `ALTER TABLE ADD COLUMN` statements — safe for SQLite, no table-rename cycle needed). This increments `schemaVersion` to 3. The new columns are `synced_at INTEGER` (nullable, set on first successful upload) and `sync_failure_count INTEGER NOT NULL DEFAULT 0`. A new `SyncRecord` domain model wraps these fields. `CitizenReport` itself is unchanged. The sync state is a separate operational concern; the existing presentation layer requires no changes.

**AWS integration and credentials.** `aws-sdk-kotlin` does not publish KMP artifacts for iOS, JS, or WasmJS targets as of mid-2025. All DynamoDB and S3 calls are therefore implemented as raw Ktor HTTP requests with AWS Signature Version 4 signing. SigV4 HMAC-SHA256 requires a platform `expect fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray` with actuals using `javax.crypto` (Android), `CommonCrypto` (iOS), and `SubtleCrypto` (JS/WasmJS). AWS credentials (Access Key ID, Secret Access Key, region, table name, bucket name) are read from a gitignored `aws.properties` file in the project root via a `loadAwsCredentials()` expect/actual: Android reads from `assets/`, iOS from `Bundle.main`, and JS/WasmJS from a compile-time-generated Kotlin object populated by a Gradle `processResources` task. The exact file format is documented in `architecture.md` under FEAT-013.

**Background sync and failure handling.** Three platform strategies are used behind a `SyncScheduler` expect/actual. Android uses `CloudSyncWorker : CoroutineWorker` with `NetworkType.CONNECTED` constraint, exponential backoff, `setExpedited` for the eager post-save enqueue, and a `PeriodicWorkRequest` every 15 minutes as a catch-all. iOS uses `BGProcessingTaskRequest` via `BGTaskScheduler` (requires network, registered in the app entry point) plus a foreground fallback that runs on app resume when online. JS/WasmJS attaches a `window.addEventListener("online", ...)` handler that triggers sync immediately; WasmJS sync is a documented no-op because no Ktor WasmJS engine is available. When `syncFailureCount` reaches 5, a `SyncFailureNotifier` expect/actual fires a device notification: Android dispatches via `NotificationManager` (channel `sync_failures`) with a `PendingIntent` to `SyncRetryReceiver` (a `BroadcastReceiver` that calls `RetryFailedSyncsUseCase` and re-enqueues the Worker); iOS uses `UNUserNotificationCenter` with a custom `"RETRY_SYNC"` action; Web posts to a `SharedFlow<SyncFailureEvent>` that surfaces as a Snackbar in the root composable.

**Idempotency.** DynamoDB writes use a `PutItem` with condition expression `attribute_not_exists(id)` on first upload and `UpdateItem` for subsequent status changes; a `ConditionalCheckFailedException` is treated as success. S3 uploads issue a `HeadObject` before each `PutObject` and skip the upload if the object already exists. S3 key format is `reports/<reportId>/<filename>`. The full technical detail — migration SQL, file map, Koin module, DynamoDB table schema, and design checklist — is recorded in `architecture.md` under FEAT-013.

**User Story** *(Business Analyst)*
> As a citizen,
> I want my submitted pothole reports and their photos to be automatically uploaded to the city's cloud infrastructure in the background,
> So that officials always receive my reports with full detail even if my device was offline at the time of submission, and I am notified if the upload repeatedly fails so I know my report may not have been delivered.

**Acceptance Criteria**

**Background sync triggers**

- [ ] Given a report is saved successfully via `SaveReportUseCase`, when the save completes, then a background sync job is enqueued immediately (Android: expedited `OneTimeWorkRequest` with `CONNECTED` constraint; iOS: `BGProcessingTaskRequest`; Web JS: sync runs in-foreground if `navigator.onLine` is true).
- [ ] Given the app is brought to the foreground and there are reports with `SyncStatus.PENDING` or `SyncStatus.FAILED` (with `syncFailureCount < 5`) in the database, when the app resumes, then a sync job is triggered (Android: `OneTimeWorkRequest` enqueued via `ProcessLifecycleOwner.ON_START`; iOS: foreground coroutine launched on app resume; Web JS: `window.addEventListener("online", ...)` handler fires if online).
- [ ] Given the Android platform, when the app is running, then a periodic background `PeriodicWorkRequest` with a 15-minute minimum interval is registered at app startup as a catch-all for reports that missed the eager sync trigger.

**Success path**

- [ ] Given a report has `SyncStatus.PENDING` and the device has network connectivity, when `SyncReportUseCase` runs to completion, then the report's metadata (`id`, `title`, `description`, `latitude`, `longitude`, `status`, `createdAt`) is present as an item in the DynamoDB table and `ReportEntity.synced_at` is set to the current epoch millis and `ReportEntity.sync_failure_count` is reset to 0.
- [ ] Given a report has at least one associated photo with a valid `localPath`, when `SyncReportUseCase` runs to completion, then each photo file is present in the S3 bucket at key `reports/<reportId>/<localFilename>` and the DynamoDB item's `photoPaths` attribute contains all uploaded S3 keys.

**Failure path and device notification**

- [ ] Given a sync attempt for a report fails (network error or AWS error response), when `RecordSyncFailureUseCase` is called, then `ReportEntity.sync_failure_count` is incremented by 1 for that report and the report remains eligible for retry on the next sync cycle.
- [ ] Given a report's `syncFailureCount` reaches exactly 5 on a failed attempt, when `RecordSyncFailureUseCase` signals the threshold, then a device notification is dispatched with the title "Sync failed" and body text `"Report \"<title>\" could not be uploaded after 5 attempts. Tap to retry."` (Android: via `NotificationManager` on channel `sync_failures`; iOS: via `UNUserNotificationCenter`; Web JS: via an in-app Snackbar message).
- [ ] Given the device notification for a failing report is shown, when the citizen taps it (Android: taps the notification; iOS: taps the "Retry" action), then `RetryFailedSyncsUseCase` is called, the report's `sync_failure_count` is reset to 0, and a new sync cycle of up to 5 attempts begins (Android: `SyncRetryReceiver` re-enqueues a `OneTimeWorkRequest`; iOS: notification action handler calls `SyncScheduler.scheduleBackgroundSync()`).

**Idempotency**

- [ ] Given a report has already been successfully uploaded to DynamoDB, when `SyncReportUseCase` runs again for the same report (e.g., due to a duplicate enqueue), then the DynamoDB `PutItem` uses condition expression `attribute_not_exists(id)` and a `ConditionalCheckFailedException` response is treated as success — no duplicate item is created and `synced_at` is not overwritten.
- [ ] Given a photo file has already been uploaded to S3 at key `reports/<reportId>/<filename>`, when `SyncPhotoUseCase` runs again for the same photo, then a `HeadObject` request confirms the object exists (HTTP 200) and the `PutObject` call is skipped — no duplicate upload occurs and no error is raised.

**Credentials — never embedded in the binary**

- [ ] Given the app is built without an `aws.properties` file in the appropriate platform asset location, when a sync job runs, then the job returns a failure result immediately rather than crashing; the credentials are never hardcoded as string literals in any Kotlin source file or resource file.
- [ ] Given a valid `aws.properties` file is present (Android: `assets/aws.properties`; iOS: bundle resource; Web JS/WasmJS: compile-time-generated `AwsConfig` object), when `loadAwsCredentials()` is called, then it returns an `AwsCredentials` instance populated with the values from that file at runtime, without the credentials appearing in the compiled app binary.

**Offline resilience**

- [ ] Given the device has no network connectivity when a report is saved, when the sync scheduler checks constraints (Android: `NetworkType.CONNECTED`; iOS/Web: `isNetworkAvailable()`), then the sync job is deferred and the report remains in `SyncStatus.PENDING` with `syncFailureCount = 0` — no failure is recorded for a connectivity-gated deferral.
- [ ] Given a report is in `SyncStatus.PENDING` and the device regains network connectivity, when the sync scheduler next runs (triggered by the online event, app foreground, or periodic job), then the report is picked up and a sync attempt is made without requiring any user action.

**Per-platform scheduler behavior**

- [ ] Given the Android platform, when `CloudSyncWorker.doWork()` executes, then it calls `GetPendingSyncsUseCase`, iterates each result calling `SyncReportUseCase`, records failures via `RecordSyncFailureUseCase`, and returns `Result.success()` for normal execution (including individual report failures tracked in the DB); it returns `Result.retry()` only on a total infrastructure failure (Ktor engine unavailable, database inaccessible).
- [ ] Given the iOS platform, when the app enters the background, then `BGProcessingTaskRequest` with identifier `com.espert.reporteciudadano.cloudsync` and `requiresNetworkConnectivity = true` is submitted to `BGTaskScheduler`; when the OS invokes the task, the sync handler calls `GetPendingSyncsUseCase` and processes each pending report, calling `task.setTaskCompleted(success:)` before the system time limit.
- [ ] Given the WasmJS platform, when a sync is triggered, then `CloudSyncRepository` returns `Result.failure(UnsupportedOperationException(...))` immediately — no network calls are made — because no Ktor WasmJS engine is available; this limitation is treated as a graceful no-op, not a failure that increments `syncFailureCount`.

**Database schema migration**

- [ ] Given an existing device with reports stored under schema version 2 (no sync columns), when the app is launched after upgrading to the FEAT-013 build, then migration `2.sqm` runs automatically, adding `synced_at INTEGER` (nullable) and `sync_failure_count INTEGER NOT NULL DEFAULT 0` to all existing `ReportEntity` rows without data loss.
- [ ] After migration, all pre-existing reports have `synced_at = NULL` and `sync_failure_count = 0`, making them eligible for their first sync on the next background sync cycle.
- [ ] The SQLDelight `schemaVersion` in `shared/build.gradle.kts` is 3 after this feature ships; attempting to open a schema-version-2 database without the migration present causes a detectable error rather than silent data corruption.

**No regression to existing features**

- [ ] Given FEAT-013 is installed, when the citizen opens the My Reports list, then all previously submitted reports are visible with their correct titles and status badges, with no missing or duplicated entries.
- [ ] Given FEAT-013 is installed, when the citizen opens the Report Detail screen for any report, then all fields (photos, title, description, location, status) display correctly; the location display (`LocationDisplayCard`) continues to resolve via the FEAT-011 online/offline path.
- [ ] Given FEAT-013 is installed, when the citizen opens the Reports Map, then all report pins are present at their correct coordinates and tapping a pin navigates to the correct Report Detail screen.
- [ ] The addition of `synced_at` and `sync_failure_count` columns to `ReportEntity` does not cause any query in `ReportRepositoryImpl` (`getAll`, `getById`, `insertReport`) to fail or return incorrect data.

**UX/UI Proposal** *(Designer — approved)*

### Overview

FEAT-013 introduces no new screens and no new navigation routes. All visual changes are additive to existing surfaces in `MyReportsScreen`. The design scope covers four deliverables: (1) a `SyncStatusIcon` composable added to each `ElevatedCard` row in the My Reports list; (2) device notification copy and channel description for Android; (3) an in-app Snackbar spec for the Web platform; and (4) the complete set of new string resource keys with English and Spanish values. The sync pipeline itself is invisible to the citizen — they see only the sync state summary on each report card and, if things go wrong repeatedly, a notification or Snackbar prompting a retry.

---

### 1. Sync Status Indicator — `SyncStatusIcon` composable

#### Purpose

Give citizens a quick, at-a-glance signal about whether each report has reached the cloud, is still waiting, or has encountered repeated upload failures. The indicator is passive — citizens do not interact with it — and must not crowd the existing `StatusChip` that shows the civic lifecycle status (`SENT`, `SEEN`, `IN_PROGRESS`, etc.).

#### Placement within the `ElevatedCard` row

The current card row layout (from FEAT-005) is:

```
Row(modifier, verticalAlignment = CenterVertically)
  Text(report.title, weight = 1f)          ← fills remaining space
  StatusChip(report.status)
```

The `SyncStatusIcon` is inserted as a third child between the title and the `StatusChip`, with `8.dp` end padding separating it from the chip. Using `weight = 1f` on the title ensures it still fills all remaining space and the two right-side elements simply wrap to their intrinsic sizes.

```
Row(modifier, verticalAlignment = CenterVertically)
  Text(report.title, weight = 1f)
  SyncStatusIcon(syncStatus, modifier = Modifier.padding(end = 8.dp))
  StatusChip(report.status)
```

The icon is 18dp × 18dp — large enough to tap-locate at a glance but smaller than the `StatusChip` text label so it reads as secondary information.

#### Icon and color decisions per state

| State | Icon | Rationale | Color token |
|---|---|---|---|
| `SyncStatus.SYNCED` | `Icons.Default.CloudDone` | Universally recognised "cloud upload confirmed" shape; the checkmark inside removes ambiguity vs `CloudUpload` which could be read as "in progress" | `MaterialTheme.colorScheme.primary` |
| `SyncStatus.PENDING` or `SyncStatus.IN_PROGRESS` | `Icons.Default.CloudUpload` | Conveys active or queued intent to upload; the upward arrow signals action without implying failure | `MaterialTheme.colorScheme.onSurfaceVariant` |
| `SyncStatus.FAILED` (count ≥ 1) | `Icons.Default.SyncProblem` | The `SyncProblem` icon (exclamation mark over sync arrows) is the standard Material "sync went wrong" metaphor and is distinct from `CloudOff` (which implies the service is unavailable system-wide) | `MaterialTheme.colorScheme.error` |

`Icons.Default.CloudOff` was considered for the failed state but rejected: it implies the cloud service itself is unreachable, which is not necessarily true. The failure may be transient. `SyncProblem` correctly signals "a sync attempt was made but encountered an error."

#### Composable signature

```kotlin
@Composable
fun SyncStatusIcon(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
)
```

#### Rendering logic

```kotlin
val (icon, tint, contentDesc) = when (syncStatus) {
    SyncStatus.SYNCED ->
        Triple(Icons.Default.CloudDone,
               MaterialTheme.colorScheme.primary,
               stringResource(Res.string.sync_status_synced_cd))
    SyncStatus.PENDING,
    SyncStatus.IN_PROGRESS ->
        Triple(Icons.Default.CloudUpload,
               MaterialTheme.colorScheme.onSurfaceVariant,
               stringResource(Res.string.sync_status_pending_cd))
    SyncStatus.FAILED ->
        Triple(Icons.Default.SyncProblem,
               MaterialTheme.colorScheme.error,
               stringResource(Res.string.sync_status_failed_cd))
}

Icon(
    imageVector = icon,
    contentDescription = contentDesc,
    tint = tint,
    modifier = modifier.size(18.dp)
)
```

#### ASCII wireframes

**Before (current card — no sync indicator):**

```
┌──────────────────────────────────────────┐
│  Pothole on main avenue    [SENT chip]   │
└──────────────────────────────────────────┘
```

**After — state: PENDING (cloud upload icon, muted):**

```
┌──────────────────────────────────────────┐
│  Pothole on main avenue  [↑☁]  [SENT]   │
│                          muted           │
└──────────────────────────────────────────┘
```

**After — state: SYNCED (cloud done icon, primary green):**

```
┌──────────────────────────────────────────┐
│  Pothole on main avenue  [✓☁]  [SEEN]   │
│                          green           │
└──────────────────────────────────────────┘
```

**After — state: FAILED (sync problem icon, error red):**

```
┌──────────────────────────────────────────┐
│  Pothole on main avenue  [!↻]  [SENT]   │
│                          red             │
└──────────────────────────────────────────┘
```

**Full list view (three reports, mixed states):**

```
┌──────────────────────────────────────────┐  CenterAlignedTopAppBar
│              My Reports                  │
├──────────────────────────────────────────┤
│                                          │
│  ┌──────────────────────────────────┐   │  ElevatedCard
│  │  Crack near bus stop  [✓☁] [SEEN] │   │  SYNCED
│  └──────────────────────────────────┘   │
│                                          │
│  ┌──────────────────────────────────┐   │  ElevatedCard
│  │  Pothole on main ave  [↑☁] [SENT] │   │  PENDING
│  └──────────────────────────────────┘   │
│                                          │
│  ┌──────────────────────────────────┐   │  ElevatedCard
│  │  Flooding in park     [!↻] [SENT] │   │  FAILED
│  └──────────────────────────────────┘   │
│                                          │
└──────────────────────────────────────────┘
  NavigationBar (Report | My Reports | Map)
```

#### `MyReportsState` change

`MyReportsState` must be extended with a map of sync states so the screen can render each icon without coupling to the sync module directly:

```kotlin
val syncStates: Map<String, SyncStatus> = emptyMap()  // keyed by report.id
```

The `MyReportsViewModel` loads sync records via `GetPendingSyncsUseCase` (already defined in the domain layer) and resolves `SyncStatus` per report ID. All reports absent from the map are treated as `PENDING` (safe default, avoids null-check in the composable).

---

### 2. Failure Notification Copy (Android)

#### Notification channel

| Field | Value |
|---|---|
| Channel ID | `sync_failures` |
| Channel name (shown in device settings) | `stringResource(Res.string.sync_notification_channel_name)` → "Sync Failures" |
| Channel description (shown in device settings) | `stringResource(Res.string.sync_notification_channel_desc)` → "Alerts when a report could not be uploaded to the city server after repeated attempts." |
| Importance | `IMPORTANCE_DEFAULT` (shows as a heads-up notification but does not interrupt with sound) |

#### Notification content

| Field | Value |
|---|---|
| Title | `stringResource(Res.string.sync_notification_title)` → "Sync failed" |
| Body | `"Report \"<reportTitle>\" could not be uploaded after 5 attempts. Tap to retry."` — assembled at runtime in `SyncFailureNotifier.android.kt`; the format string key is `sync_notification_body` with placeholder `%1$s` for the title |
| Action button label | `stringResource(Res.string.sync_notification_action_retry)` → "Retry Sync" |

The action button label "Retry Sync" (not just "Retry") disambiguates the action in notification shade for citizens who may have multiple notification types. It is short enough to fit the notification action chip without truncation on any standard screen size.

#### iOS notification action

Same title and body copy. The `UNNotificationAction` title uses the same `sync_notification_action_retry` key value: "Retry Sync".

---

### 3. Web In-App Snackbar

#### Trigger condition

The Snackbar is shown when `SyncFailureNotifier` (Web actual) posts a `SyncFailureEvent` to the root composable's `SharedFlow`. This occurs when `RecordSyncFailureUseCase` signals that `syncFailureCount` has reached `MAX_CONSECUTIVE_FAILURES (= 5)` for a given report.

#### Snackbar specification

| Property | Value |
|---|---|
| Message | `"Sync failed for \"<reportTitle>\". Reopen the app when you are online to retry."` — format key `sync_snackbar_message` with placeholder `%1$s` for the title |
| Action label | `stringResource(Res.string.sync_snackbar_action)` → "Retry Sync" |
| Duration | `SnackbarDuration.Indefinite` — stays visible until the citizen dismisses it or taps the action; this reflects the severity (data may not have reached the city server) |
| Dismissal | The citizen can swipe to dismiss (standard Material3 `Snackbar` behaviour) without retrying |
| Retry action | `SnackbarResult.ActionPerformed` triggers a call to `RetryFailedSyncsUseCase` |

The Web snackbar provides a "Retry Sync" action button (unlike the architect note's original spec which said "no retry action button") because the Web foreground sync path allows immediate retry when the user is online. Since the Web's sync scheduler re-fires on network reconnect, tapping "Retry Sync" simply calls `RetryFailedSyncsUseCase` and the next `window.addEventListener("online")` cycle picks up the reset-to-PENDING records.

#### Root composable wiring (design intent, not implementation)

```
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(Unit) {
    syncFailureEvents.collect { event ->
        val result = snackbarHostState.showSnackbar(
            message  = formatSyncFailureMessage(event.reportTitle),
            actionLabel = retryLabel,
            duration = SnackbarDuration.Indefinite
        )
        if (result == SnackbarResult.ActionPerformed) {
            retrySyncUseCase(event.reportId)
        }
    }
}

Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { ... }
```

#### ASCII wireframe (Web, wide screen, rail navigation)

```
┌────────────────────────────────────────────────────────┐
│ [Report] [My Reports] [Map]        (NavigationRail)    │
│                                                        │
│  My Reports                                            │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Crack near bus stop          [✓☁]  [SEEN]       │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Pothole on main ave          [!↻]  [SENT]       │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│ ┌──────────────────────────────────────────────────┐   │
│ │ Sync failed for "Pothole on main ave". Reopen…   │   │  ← Snackbar, Indefinite
│ │                              [Retry Sync]        │   │
│ └──────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

---

### 4. New String Resource Keys

All keys below must be added to both `shared/src/commonMain/composeResources/values/strings.xml` (English) and `shared/src/commonMain/composeResources/values-es/strings.xml` (Spanish).

| Key | English value | Spanish value |
|---|---|---|
| `sync_status_synced_cd` | Synced to cloud | Sincronizado con la nube |
| `sync_status_pending_cd` | Sync pending | Sincronización pendiente |
| `sync_status_failed_cd` | Sync failed | Error de sincronización |
| `sync_notification_channel_name` | Sync Failures | Errores de sincronización |
| `sync_notification_channel_desc` | Alerts when a report could not be uploaded to the city server after repeated attempts. | Alertas cuando un reporte no pudo ser enviado al servidor de la ciudad después de varios intentos. |
| `sync_notification_title` | Sync failed | Error de sincronización |
| `sync_notification_body` | Report "%1$s" could not be uploaded after 5 attempts. Tap to retry. | El reporte "%1$s" no pudo ser enviado después de 5 intentos. Toca para reintentar. |
| `sync_notification_action_retry` | Retry Sync | Reintentar sincronización |
| `sync_snackbar_message` | Sync failed for "%1$s". Reopen the app when you are online to retry. | Error al sincronizar "%1$s". Vuelve a abrir la aplicación cuando tengas conexión para reintentar. |
| `sync_snackbar_action` | Retry Sync | Reintentar sincronización |

Notes on the table:
- Keys suffixed `_cd` are `contentDescription` values passed to `Icon` composables. They are read by screen-reader assistive technology and must be concise and action-oriented.
- The `%1$s` placeholder in `sync_notification_body` and `sync_snackbar_message` is substituted at runtime with `report.title`. On Android, `String.format(context.getString(...), reportTitle)` is used; in commonMain Compose, `stringResource(Res.string.sync_notification_body, reportTitle)` with parameterised string resources handles substitution.
- `sync_notification_channel_name` and `sync_notification_channel_desc` are shown in the Android system notification settings UI, not inside the app — they should use plain prose rather than app-internal jargon.

---

### 5. Material3 Component Summary

| Surface | Component added / changed | Notes |
|---|---|---|
| `MyReportsScreen` — each `ElevatedCard` | `SyncStatusIcon` (new composable) | Inserted between `Text(title)` and `StatusChip` in the existing `Row` |
| `MyReportsScreen` — each `ElevatedCard` | `Icon` (`CloudDone` / `CloudUpload` / `SyncProblem`) | Inside `SyncStatusIcon`; 18dp, M3 color token |
| `MyReportsState` | `syncStates: Map<String, SyncStatus>` field added | Loaded by `MyReportsViewModel` from `GetPendingSyncsUseCase` |
| Android device notification | `NotificationCompat.Builder` (existing Android API, not a Compose component) | Channel `sync_failures`; `PendingIntent` to `SyncRetryReceiver` |
| Web root `Scaffold` | `SnackbarHost` + `showSnackbar(duration = Indefinite)` | Existing M3 `Snackbar`; wired to `SharedFlow<SyncFailureEvent>` |

No new navigation routes. No new screens. No `BottomSheet`, `Dialog`, or `AlertDialog` introduced by this feature.

---

### 6. States for `SyncStatusIcon`

| State | Trigger condition | Visual |
|---|---|---|
| PENDING | New report saved; `syncedAt == null` and `syncFailureCount == 0` | `CloudUpload` icon, `onSurfaceVariant` |
| IN_PROGRESS | Sync job is actively running | `CloudUpload` icon, `onSurfaceVariant` (same visual as PENDING; no spinner to keep the card row clean) |
| SYNCED | `syncedAt != null` and `syncFailureCount == 0` | `CloudDone` icon, `primary` (civic green) |
| FAILED | `syncFailureCount >= 1` | `SyncProblem` icon, `error` (red) |

`IN_PROGRESS` does not show a spinner inside the list item because it would animate every visible card during a batch sync, creating visual noise. The `CloudUpload` icon is sufficient to convey "something is happening"; detailed progress belongs in a hypothetical future sync progress screen, not in the list.

---

### 7. Adaptive Layout Notes

The `SyncStatusIcon` is an 18dp `Icon` — it is readable and non-intrusive at all screen widths used by Android phones, iOS devices, and Web browser windows. On tablet and web layouts where `NavigationRail` is used, the `ElevatedCard` may be rendered in a narrower content column (up to ~600dp max-width). The three-element row (`title [weight=1f]`, `SyncStatusIcon`, `StatusChip`) collapses gracefully: the title truncates before either right-side element is pushed off-screen.

---

**Status**: `Ready`

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
