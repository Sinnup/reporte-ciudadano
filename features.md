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

## Template (for new features)

### [FEAT-000] Feature Title

**Status**: `Draft` | `Design` | `Ready` | `In Progress` | `Done`

**Architect Notes**

**User Story** *(Business Analyst)*
> As a [user], I want [action], so that [benefit].

**Acceptance Criteria**
- [ ] ...

**UX/UI Proposal** *(Designer)*
