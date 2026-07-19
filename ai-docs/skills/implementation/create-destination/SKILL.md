---
name: create-destination
description: Add a new screen destination to a kei-1111.github.io feature module (app/feature/*) — procedures and templates following the project's Navigation 3 + MVI (MviViewModel) + Metro DI patterns. Use when the user asks to add a new screen / destination, create a XxxScreen, add a NavKey, create a new feature module, or wire entries into AppNavDisplay. Screen is the only destination kind in this project (no Dialogs / BottomSheets).
---

# create-destination skill

A runbook for adding a new screen destination consistently following KEI's Navigation 3 + MVI +
Metro patterns. KEI currently has one destination kind — the breakpoint-branching Screen — and a
two-file navigation layer per feature.

## Overview

Adding a destination touches ~10 new files plus 1–3 wiring edits. The most common mistakes:

- **Forgetting to register the new NavKey in `AppNavDisplay`'s `SerializersModule`** — wasmJs has
  no reflection, so the polymorphic NavKey back stack is restored via an explicit
  `subclass(Xxx::class, Xxx.serializer())` registration. Forgetting it compiles fine but silently
  breaks (or crashes) back-stack save/restore. This is the #1 pitfall.
- Forgetting to call the new `{feature}Entries()` inside `AppNavDisplay`'s `entryProvider`
- Putting the `navigate{Name}` extension in a separate NavigationExtensions file — KEI colocates
  it with the NavKey in `{Feature}NavigationRoute.kt`
- Forgetting `ConsumeEffect` in the Intent, or an effect branch that doesn't clear `effect` to null
- Injecting a Repository into the ViewModel (feature modules have no `core:data` dependency at all)
- Referencing a ViewModel from Desktop/Mobile Content (their signature is `(state, onIntent, modifier)`)

## Scope

**In scope**: a standard Screen destination — in an existing feature module or a brand-new
`app/feature/*` module (module scaffolding is a 3-edit step, covered below).

**Out of scope**: Dialogs, BottomSheets, ResultEventBus — KEI does not use them. Define and document
a project-specific architecture before introducing one.

## Prerequisites — confirm before implementation

Confirm with the user if anything is ambiguous:

1. **Destination name (PascalCase)** — e.g. `Works`. Used bare for the NavKey and all MVI class
   names (`WorksViewModel`, not `WorksScreenViewModel`); only the Composable takes the `Screen` suffix.
2. **Target feature module** — an existing `app/feature/*`, or a new module (extra scaffolding step).
   In KEI today the destination name and feature name coincide (`profile`/`profile`), but a
   feature may host multiple destinations under `destination/<name>/`.
3. **One-shot effects?** — identify the concrete navigation, URL-opening, or other one-shot variants
   for the required `{Name}Effect` type. Every destination keeps the Effect lifecycle wiring.
4. **Data loading?** — does the ViewModel inject UseCases from `core:domain` and observe
   `useCase().asResult()`? (UseCases only — never a Repository.)
5. **Who navigates here?** — which existing entry/screen calls `navigate{Name}()`, or is it a
   start destination (then omit the navigate extension, like `Splash`)?

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
| `Navigation.kt.template` | `KOTLIN/navigation/{{Feature}}Navigation.kt` (or add the entry to the existing `{{feature}}Entries()`) |
| `ScreenRoot.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ScreenRoot.kt` |
| `Screen.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Screen.kt` |
| `DesktopContent.kt.template` | `KOTLIN/destination/{{name}}/content/{{Name}}DesktopContent.kt` |
| `MobileContent.kt.template` | `KOTLIN/destination/{{name}}/content/{{Name}}MobileContent.kt` |
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
and feature-local tokens under `theme/` (`{{Name}}Dimensions.kt` / `{{Name}}Animations.kt`).
The `destination/{{name}}/` top level holds only the seven contract/orchestration files
(`ScreenRoot` / `Screen` + the five MVI files) — everything else goes into the subpackages above.

Templates are minimal skeletons. Every `// PLACEHOLDER:` comment marks an insertion point —
replace it with real code for this destination (or delete it where nothing is needed); no
`PLACEHOLDER` comment may survive into the generated files. Pull concrete UseCase/model types,
Intent/Effect variants, and layout sections from the Phase 2 reference implementations; Effect
variants in particular are chosen per destination (Prerequisites #3) — `OpenUrl` for URL-opening
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

### Phase 6 — Checklist

Run through `references/checklists/screen.md` to spot misses.

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
