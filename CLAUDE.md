# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

ReporteCiudadano is a Kotlin Multiplatform (KMP) app targeting Android, iOS, and Web using Compose Multiplatform and MVI + Clean Architecture.

## Build & Run

```bash
# Android
./gradlew :androidApp:assembleDebug

# Web — Wasm (modern browsers)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Web — JS (older browsers)
./gradlew :webApp:jsBrowserDevelopmentRun

# iOS — open /iosApp in Xcode and run from there
```

## Tests

```bash
./gradlew :shared:testAndroidHostTest       # Android
./gradlew :shared:wasmJsTest               # Web Wasm
./gradlew :shared:jsTest                   # Web JS
./gradlew :shared:iosSimulatorArm64Test    # iOS simulator
```

## Architecture

### Module Structure

| Module | Role |
|---|---|
| `:shared` | All Compose Multiplatform UI and business logic shared across platforms |
| `:androidApp` | Thin Android launcher — runs `MainActivity` hosting the shared `App()` composable |
| `:webApp` | Web entry point bootstrapping shared UI for JS and WasmJS browser targets |
| `/iosApp` | Xcode project consuming the `Shared.framework` compiled from `:shared` |

**Growth rule**: new features must be built as separate Gradle submodules (Android Library format), not added directly into `:shared` or `:androidApp`. This allows features to be packaged as independent artifacts and consumed by other apps.

### Source Sets in `:shared`

| Source set | Purpose |
|---|---|
| `commonMain` | Platform-agnostic UI and logic |
| `androidMain` / `iosMain` / `jsMain` / `wasmJsMain` | Platform-specific implementations |
| `commonTest` / `androidHostTest` / `iosTest` | Tests per target |

### MVI Layers

- **Presentation**: Composable screens + `ViewModel` (via `lifecycle-viewmodel-compose`) + immutable `State` data class + sealed `Intent` class
- **Domain**: `UseCase` classes + repository interfaces + domain models (pure Kotlin, in `commonMain`)
- **Data**: Repository implementations + data sources (platform-specific code goes in the appropriate source set)

State flows downstream via `StateFlow`. User actions flow upstream as `Intent` sealed classes dispatched to the `ViewModel`.

## Agents

Specialized agents live in `.claude/agents/`. Use them for their designated concern:

| Agent | Concern |
|---|---|
| `versioning` | Commit strategy, branching, changelog |
| `kmp-architect` | Architecture decisions, module design |
| `kmp-developer` | Feature implementation |
| `kmp-qa` | Unit test authoring and execution |
| `android-expert` | Android platform specifics |
| `ios-expert` | iOS platform specifics |
| `web-expert` | JS/Wasm browser platform specifics |
| `ux-ui-designer` | UI/UX design proposals per feature |
| `business-analyst` | User story authoring and requirements |

## Standard Feature Flow

Every feature follows this pipeline without exception. Each stage hands off to the next:

```
1. ARCHITECT  → reads AppPurpose.md / user input
               → writes analysis to architecture.md
               → adds feature entry to features.md (Architect Notes section)

2. BA         → reads architect's notes in features.md
               → writes User Story + Acceptance Criteria into the feature entry
               → sets status: Design

3. DESIGNER   → reads User Story in features.md
               → writes UX/UI Proposal into the feature entry
               → iterates with BA until both approve
               → sets status: Ready

4. DEVELOPER  → reads features.md (status: Ready) + architecture.md
               → creates branch: feature/feat-NNN-short-name
               → implements following MVI + Clean Architecture
               → sets status: In Progress

5. QA         → writes unit tests for ViewModels and UseCases
               → runs all test targets
               → confirms tests pass on all platforms

6. VERSIONING → updates changelog.md
               → commits atomically with Conventional Commits
               → prepares PR for merge to main
               → sets status: Done
```

**Rule**: never start a new feature until the previous one is Done and merged.
**Rule**: features are always triggered by invoking the `kmp-architect` agent first.

## Feature & Change Tracking

- **`features.md`** — User stories written by the Business Analyst with UX/UI proposals. A feature must have an approved proposal before development starts.
- **`changelog.md`** — Updated per completed feature, before requesting merge to `main`.
