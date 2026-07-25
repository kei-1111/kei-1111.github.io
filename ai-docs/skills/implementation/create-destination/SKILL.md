---
name: create-destination
description: Add a new destination to a kei-1111.github.io feature module (app/feature/*) — procedures and templates following the project's Navigation 3 + MVI (MviViewModel) + Metro DI patterns. Use when the user asks to add a new screen / destination / dialog / palette, create a XxxScreen, add a NavKey, create a new feature module, or wire entries into AppNavDisplay. Destinations come in two kinds: full-window Screens, and dialog destinations drawn above the previous entry via DialogSceneStrategy.
---

# create-destination skill

A runbook for adding a new destination consistently following KEI's Navigation 3 + MVI + Metro
patterns. Every destination is a `NavKey` on the single flat back stack, served by a three-file
navigation layer per feature; it renders either as a full-window Screen or as a dialog destination
above the entry beneath it.

## Overview

Adding a destination touches ~8–10 new files plus 1–4 wiring edits. The most common mistakes:

- **Forgetting to register the new NavKey in `AppNavDisplay`'s `SerializersModule`** — wasmJs has
  no reflection, so the polymorphic NavKey back stack is restored via an explicit
  `subclass(Xxx::class, Xxx.serializer())` registration. Forgetting it compiles fine but silently
  breaks (or crashes) back-stack save/restore. This is the #1 pitfall.
- Dialog only — forgetting `metadata = dialogTransition()` on its `entry<...>`. This compiles and
  then renders the destination full-window, replacing the entry it was supposed to float above.
- Forgetting to call the new `{feature}Entries()` inside `AppNavDisplay`'s `entryProvider`
- Forgetting `ConsumeEffect` in the Intent, or an effect branch that doesn't clear `effect` to null
- Injecting a Repository into the ViewModel (feature modules have no `core:data` dependency at all)
- Referencing a ViewModel from Desktop/Mobile Content (their signature is `(state, onIntent, modifier)`)

## Scope

**In scope**: any destination reached through the back stack — in an existing feature module or a
brand-new `app/feature/*` module (module scaffolding is a 3-edit step, covered below). Two kinds:

| Kind | Rendering | Layering |
|---|---|---|
| **Screen** | Fills the window; the entry beneath is replaced | `ScreenRoot` → `Screen` (breakpoint branch) → Desktop/Mobile `Content` → `component/` |
| **Dialog** (dialog / palette) | Drawn above the previous entry by `DialogSceneStrategy` | `DialogRoot` → `Dialog` (panel content) → `component/` — no Content split |

Both kinds are `NavKey`s registered in the same `SerializersModule` and provided by the same
`{feature}Entries()`; they differ only in the entry metadata and UI layering above.
Reference dialog: `SearchEverywhere`.

**Out of scope**: in-place UI that is not reached through the back stack — e.g. the license sheet
(`LicenseSheetOverlay`) drawn inside its own card and driven by `ProfileState.selectedLicense`.
That is plain state-driven rendering with no `NavKey`, and it stays that way.

## Prerequisites — confirm before implementation

Confirm with the user if anything is ambiguous:

1. **Destination name (PascalCase)** — e.g. `Works`. Used bare for the NavKey and all MVI class
   names (`WorksViewModel`, not `WorksScreenViewModel`); only the Composable takes the `Screen` suffix.
2. **Destination kind** — a full-window Screen, or a dialog floating over the entry beneath it?
   A dialog keeps that entry composed underneath and needs `dialogTransition()` metadata in Phase 5.
3. **Target feature module** — an existing `app/feature/*`, or a new module (extra scaffolding step).
   In KEI today the destination name and feature name coincide (`profile`/`profile`), but a
   feature may host multiple destinations under `destination/<name>/`.
4. **One-shot effects?** — identify the concrete navigation, URL-opening, or other one-shot variants
   for the required `{Name}Effect` type. Every destination keeps the Effect lifecycle wiring.
5. **Data loading?** — does the ViewModel inject UseCases from `core:domain` and observe
   `useCase().asResult()`? (UseCases only — never a Repository.)
6. **Who navigates here?** — which existing entry/screen calls `navigate{Name}()`, or is it a
   start destination (then omit the navigate extension, like `Splash`)?
7. **Returning a result?** — if the destination hands data back to the entry beneath it, declare a
   dedicated result type beside the producing NavKey and use `ResultEventBus`'s reified type API
   (see `docs/ArchitectureOverview.md`).

## Workflow

### Phase 1 — Read the rules

Read `AGENTS.md`, `docs/ArchitectureOverview.md`, and `docs/ModuleOverview.md`. If a template has
drifted from them or from the current code, the code wins.

### Phase 2 — Read the reference implementations

- `app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/` — UseCase injection,
  data loading via `asResult()`, `OpenUrl` effect, layout-reset logic in `UpdateLayout`
- `app/feature/splash/src/commonMain/kotlin/io/github/kei_1111/app/feature/splash/` — no injection,
  cross-feature navigation effect (`NavigateProfile`), entries function with a lambda parameter
- `app/webApp/src/commonMain/kotlin/io/github/kei_1111/app/navigation/AppNavDisplay.kt` — the single
  NavDisplay, `navKeySavedStateConfiguration`, `entryProvider`

### Phase 3 — New feature module only (skip for an existing module)

1. `settings.gradle.kts` — add `include(":app:feature:{feature}")` alongside the existing feature includes
2. Create `app/feature/{feature}/build.gradle.kts` — exactly this (mirrors `app/feature/profile/build.gradle.kts`;
   `KmpFeaturePlugin` supplies all core dependencies, wasm target, preview Android target, Metro,
   serialization):

   ```kotlin
   plugins {
       alias(libs.plugins.kei1111.detekt)
       alias(libs.plugins.kei1111.kmp.feature)
   }
   ```

3. `app/webApp/build.gradle.kts` — add `implementation(projects.app.feature.{feature})` to
   `commonMain.dependencies` (typesafe project accessors)

Do NOT add a `core:data` dependency to the feature module, ever.

### Phase 4 — Generate files from templates

Templates live in `references/templates/`. Placeholders:

| Placeholder | Meaning | Example |
|---|---|---|
| `{{Name}}` | PascalCase destination name (NavKey, MVI classes, Screen prefix) | `Works` |
| `{{name}}` | lowercase destination directory / package segment | `works` |
| `{{feature}}` | lowercase feature module name (Gradle path, package, entries function) | `works` |
| `{{Feature}}` | PascalCase feature name (navigation file names only) | `Works` |

Base path `KOTLIN = app/feature/{{feature}}/src/commonMain/kotlin/io/github/kei_1111/app/feature/{{feature}}`:

| Template | Target |
|---|---|
| `NavigationRoute.kt.template` | `KOTLIN/navigation/{{Feature}}NavigationRoute.kt` (or add to the existing file) |
| `NavigationExtensions.kt.template` | `KOTLIN/navigation/{{Feature}}NavigationExtensions.kt` (omit when no navigation extension is needed) |
| `Navigation.kt.template` | `KOTLIN/navigation/{{Feature}}Navigation.kt` (or add the entry to the existing `{{feature}}Entries()`) |
| `ScreenRoot.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ScreenRoot.kt` |
| `Screen.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Screen.kt` |
| `DialogRoot.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}DialogRoot.kt` — Dialog kind only |
| `Dialog.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Dialog.kt` — Dialog kind only |
| `DesktopContent.kt.template` | `KOTLIN/destination/{{name}}/content/{{Name}}DesktopContent.kt` — Screen kind only |
| `MobileContent.kt.template` | `KOTLIN/destination/{{name}}/content/{{Name}}MobileContent.kt` — Screen kind only |
| `ViewModel.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ViewModel.kt` |
| `ViewModelState.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ViewModelState.kt` |
| `State.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}State.kt` |
| `Intent.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Intent.kt` |
| `Effect.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Effect.kt` |

Not templated but usually needed: `destination/{{name}}/preview/{{Name}}PreviewFixtures.kt`
(sample domain data for previews — see `ProfilePreviewFixtures.kt`; fixtures duplicate content
because a feature cannot read `core:data`), section components under `destination/{{name}}/component/`,
screen-local UI model types (enums etc.) under `destination/{{name}}/model/` (see `EditorPage.kt` /
`SplashFont.kt`; an organizational subpackage, not a dependency layer),
and destination-specific tokens/helpers under `destination/{{name}}/theme/`
(`{{Name}}Dimensions.kt` / `{{Name}}Animations.kt`). A token or helper shared by two destinations
moves up to the feature-level `theme/` package.
The `destination/{{name}}/` top level holds only the seven contract/orchestration files
(`ScreenRoot` / `Screen` or `DialogRoot` / `Dialog`, plus the five MVI files) — everything else goes
into the subpackages above.

**Dialog kind** — use the Dialog templates instead of Screen/Content templates. There is no
Desktop/Mobile split. `DialogSceneStrategy` owns the window and scrim; `{{Name}}Dialog` owns the panel
sized from `BoxWithConstraints` and styled from `KeiTheme.shapes` / `.colors`. Section components
still live under `component/` and take plain values + callbacks.
Reference: `destination/searcheverywhere/`.

Templates are minimal skeletons. Every `// PLACEHOLDER:` comment marks an insertion point —
replace it with real code for this destination (or delete it where nothing is needed); no
`PLACEHOLDER` comment may survive into the generated files. Pull concrete UseCase/model types,
Intent/Effect variants, and layout sections from the Phase 2 reference implementations; Effect
variants in particular are chosen per destination (Prerequisites #4) — `OpenUrl` for URL-opening
screens (Profile), `Navigate{Target}` for navigation (Splash) — never copied blindly.

### Phase 5 — MANDATORY wiring in `app/webApp/.../navigation/AppNavDisplay.kt`

1. Register the NavKey in `navKeySavedStateConfiguration`'s `SerializersModule` — always, for every new destination:

   ```kotlin
   polymorphic(NavKey::class) {
       subclass(Splash::class, Splash.serializer())
       subclass(Profile::class, Profile.serializer())
       subclass({{Name}}::class, {{Name}}.serializer())   // <- new
   }
   ```

   wasmJs has no reflection — skipping this breaks back-stack serialization at runtime while
   compiling cleanly.

2. New feature module only — call the entries function inside `entryProvider` (for a destination added to an existing feature, `{{feature}}Entries()` is already wired; the new entry was added to it in Phase 4):

   ```kotlin
   entryProvider = entryProvider {
       splashEntries(navigateProfile = backStack::navigateProfile)
       profileEntries()
       {{feature}}Entries()   // <- new; pass navigation lambdas: backStack::navigate{{Name}}
   }
   ```

If an existing screen navigates here, thread the `navigate{{Name}}` lambda through that feature's
entries function → `{{Name}}ScreenRoot` → Effect handler (Intent → Effect → `currentNavigate{{Name}}()`,
retained via `rememberUpdatedState` — see `SplashScreenRoot.kt`).

3. **Dialog kind only** — declare the presentation on the entry:

   ```kotlin
   entry<{{Name}}>(
       metadata = dialogTransition(),
   ) { ... }
   ```

   `AppNavDisplay` installs `DialogSceneStrategy`. Omitting the metadata compiles and then renders
   full-window — verify visually, not just by build. Result reception belongs inside the receiving
   `entry<>` block via `ResultEffect<ResultType>(LocalResultEventBus.current)`.

### Phase 6 — Checklist

Run through `references/checklists/screen.md` (Screen kind) or
`references/checklists/overlay.md` (Dialog kind) to spot misses.

### Phase 7 — Verification (completion criteria)

```bash
./gradlew :app:feature:{feature}:compileKotlinWasmJs   # wasm (distribution target) compiles
./gradlew :app:feature:{feature}:compileAndroidMain    # preview-only Android target compiles
./gradlew :app:webApp:compileKotlinWasmJs              # app wiring compiles — Phase 5 edits AppNavDisplay, which a feature-only compile cannot catch
./gradlew detekt                                   # lint; autoCorrect is enabled
```

detekt note: because autoCorrect rewrites files, a first run that reformats can end BUILD FAILED —
run detekt again before judging the result. Never fix import ordering manually.

## References

- `AGENTS.md` — project conventions and validation requirements
- `docs/ArchitectureOverview.md` — MVI, DI, data flow, and navigation
- `docs/ModuleOverview.md` — module responsibilities and dependencies

## Important Constraints

- Do not deviate from existing patterns or restructure `AppNavDisplay` without the user's approval
- If templates have drifted from the current code, **follow the current code** — the source of
  truth is `app/feature/profile`, `app/feature/splash`, and the project's architecture documents
- The Android target is preview-only — no Android-specific runtime behavior in the new screen
