# Features

Features flow through these stages before development starts:

```
Architect analysis → BA writes user story → UX/UI proposal → [iterate] → Ready → Development → QA → Versioning → Done
```

See `architecture.md` for domain model, feature sequence, and platform boundaries.

---

### [FEAT-001] App Shell & Navigation

**Status**: `Ready`

**Architect Notes**
Three-tab shell. No business logic. Sets up the `NavigationBar` host and empty placeholder screens
for each tab. Foundation for all other features.

**User Story** *(Business Analyst)*
> As a citizen, I want a tab-based main screen so that I can quickly navigate between reporting
> a new pothole, viewing my submitted reports, and seeing all reports on a map.

**Acceptance Criteria**
- [ ] App opens on the "Report" tab by default.
- [ ] Three tabs are always visible at the bottom: "Report", "My Reports", "Reports Map".
- [ ] Switching tabs preserves each tab's scroll/state position.
- [ ] The active tab is visually distinct from inactive ones.
- [ ] Each tab shows its dedicated screen (content delivered by subsequent features).

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

**Status**: `Draft`

**Architect Notes**
Platform-specific concern behind `expect/actual`. Location permission must be granted before
the camera opens — if denied, camera is blocked with an explanation. Captures up to 4 photos.
Each capture embeds GPS coordinates in EXIF. Preview screen allows retake or delete per photo.
Entire session can be cancelled at any time.

**User Story** *(pending BA)*

**UX/UI Proposal** *(pending Designer)*

---

### [FEAT-003] Report Form & Submission

**Status**: `Draft`

**Architect Notes**
Depends on FEAT-002 (receives photos + EXIF locations). Reverse-geocodes the location of the
first photo via `GeocodingRepository` (Ktor → Nominatim/system geocoder per platform).
Shows an approximation disclaimer popup on entry. Mandatory fields: Title and Description.
Address is non-editable. On submit: `SaveReportUseCase` persists via SQLDelight with status SENT.

**User Story** *(pending BA)*

**UX/UI Proposal** *(pending Designer)*

---

### [FEAT-004] Thank You Screen

**Status**: `Draft`

**Architect Notes**
Simple success screen shown after FEAT-003 submission. Auto-dismisses after 4 seconds via a
`LaunchedEffect` countdown. No user action required — navigates back to the "Report" tab root.

**User Story** *(pending BA)*

**UX/UI Proposal** *(pending Designer)*

---

### [FEAT-005] My Reports List

**Status**: `Draft`

**Architect Notes**
Reads from `ReportRepository.getAll()`. Displays title + status badge per item. Tapping navigates
to FEAT-006 (Report Detail). Status values: SENT, SEEN, PENDING, IN_PROGRESS, RESOLVED, DISCARDED.
Initially all reports will have status SENT (remote status sync is a future enhancement).

**User Story** *(pending BA)*

**UX/UI Proposal** *(pending Designer)*

---

### [FEAT-006] Report Detail (read-only)

**Status**: `Draft`

**Architect Notes**
Shared read-only view used by both FEAT-005 and FEAT-007. Loads a single report via
`ReportRepository.getById(id)`. Displays all fields (photos, title, description, address, status)
in non-editable form. Reuses the same layout structure as the Report Form (FEAT-003).

**User Story** *(pending BA)*

**UX/UI Proposal** *(pending Designer)*

---

### [FEAT-007] Reports Map

**Status**: `Draft`

**Architect Notes**
Platform-specific map (Google Maps on Android, MapKit on iOS, Leaflet on Web) behind
`expect/actual`. Loads all reports via `ReportRepository.getAll()` and places a pin at each
`GeoLocation`. Tapping a pin navigates to FEAT-006 (Report Detail).

**User Story** *(pending BA)*

**UX/UI Proposal** *(pending Designer)*

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
