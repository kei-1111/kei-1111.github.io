---
name: create-destination
description: Procedures and templates for adding a new screen destination to a kei-1111.github.io feature module (feature/*), following the project's Navigation 3 + MVI (MviViewModel) + Metro DI patterns. Use when the user asks to add a new screen / destination, create a XxxScreen, add a NavKey, create a new feature module, or wire entries into AppNavDisplay. Screen is the only destination kind in this project (no Dialogs / BottomSheets).
---

# create-destination skill

A runbook for adding a new screen destination consistently following KEI's Navigation 3 + MVI +
Metro patterns. Unlike withmo, KEI has exactly one destination kind — the breakpoint-branching
Screen — and a two-file navigation layer per feature.

## Overview

Adding a destination touches ~9 new files plus 1–3 wiring edits. The most common mistakes:

- **Forgetting to register the new NavKey in `AppNavDisplay`'s `SerializersModule`** — wasmJs has
  no reflection, so the polymorphic NavKey back stack is restored via an explicit
  `subclass(Xxx::class, Xxx.serializer())` registration. Forgetting it compiles fine but silently
  breaks (or crashes) back-stack save/restore. This is the #1 pitfall.
- Forgetting to call the new `{feature}Entries()` inside `AppNavDisplay`'s `entryProvider`
- Putting the `navigate{Name}` extension in a separate NavigationExtensions file (withmo style) —
  KEI colocates it with the NavKey in `{Feature}NavigationRoute.kt`
- Forgetting `ConsumeEffect` in the Intent, or an effect branch that doesn't clear `effect` to null
- Injecting a Repository into the ViewModel (feature modules have no `core:data` dependency at all)
- Referencing a ViewModel from Desktop/Mobile Content (their signature is `(state, onIntent, modifier)`)

## Scope

**In scope**: a standard Screen destination — in an existing feature module or a brand-new
`feature/*` module (module scaffolding is a 3-edit step, covered below).

**Out of scope**: Dialogs, BottomSheets, ResultEventBus — KEI does not use them. If one is ever
introduced, consult withmo's `.claude/rules/navigation.md` for the established patterns rather
than inventing a new one (see KEI `.claude/rules/navigation.md`, "Dialogs, BottomSheets,
ResultEventBus — Not Used").

## Prerequisites — confirm before implementation

Confirm with the user if anything is ambiguous:

1. **Destination name (PascalCase)** — e.g. `Works`. Used bare for the NavKey and all MVI class
   names (`WorksViewModel`, not `WorksScreenViewModel`); only the Composable takes the `Screen` suffix.
2. **Target feature module** — an existing `feature/*`, or a new module (extra scaffolding step).
   In KEI today the destination name and feature name coincide (`profile`/`profile`), but a
   feature may host multiple destinations under `destination/<name>/`.
3. **One-shot effects?** — navigation, opening a URL. Decides whether `{Name}Effect.kt` exists and
   whether State/ViewModelState carry an `effect` property and the Screen a `MviEffect` block.
4. **Data loading?** — does the ViewModel inject UseCases from `core:domain` and observe
   `useCase().asResult()`? (UseCases only — never a Repository.)
5. **Who navigates here?** — which existing entry/screen calls `navigate{Name}()`, or is it a
   start destination (then omit the navigate extension, like `Splash`)?

## Workflow

### Phase 1 — Read the rules

Read these in order; if a template here has drifted from them or from the code, the code wins:

- `.claude/rules/navigation.md`
- `.claude/rules/mvi-architecture.md`
- `.claude/rules/ui-implementation.md`
- `.claude/rules/naming-conventions.md`
- `.claude/rules/preview.md`
- `.claude/rules/usecase.md` (when data loading is involved)

### Phase 2 — Read the reference implementations

- `feature/profile/src/commonMain/kotlin/io/github/kei_1111/feature/profile/` — UseCase injection,
  data loading via `asResult()`, `OpenUrl` effect, layout-reset logic in `UpdateLayout`
- `feature/splash/src/commonMain/kotlin/io/github/kei_1111/feature/splash/` — no injection,
  cross-feature navigation effect (`NavigateProfile`), entries function with a lambda parameter
- `composeApp/src/commonMain/kotlin/io/github/kei_1111/navigation/AppNavDisplay.kt` — the single
  NavDisplay, `navKeySavedStateConfiguration`, `entryProvider`

### Phase 3 — New feature module only (skip for an existing module)

1. `settings.gradle.kts` — add `include(":feature:{feature}")` alongside the existing feature includes
2. Create `feature/{feature}/build.gradle.kts` — exactly this (mirrors `feature/profile/build.gradle.kts`;
   `KmpFeaturePlugin` supplies all core dependencies, wasm target, preview Android target, Metro,
   serialization):

   ```kotlin
   plugins {
       alias(libs.plugins.kei1111.detekt)
       alias(libs.plugins.kei1111.kmp.feature)
   }
   ```

3. `composeApp/build.gradle.kts` — add `implementation(projects.feature.{feature})` to
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

Base path `KOTLIN = feature/{{feature}}/src/commonMain/kotlin/io/github/kei_1111/feature/{{feature}}`:

| Template | Target |
|---|---|
| `NavigationRoute.kt.template` | `KOTLIN/navigation/{{Feature}}NavigationRoute.kt` (or add to the existing file) |
| `Navigation.kt.template` | `KOTLIN/navigation/{{Feature}}Navigation.kt` (or add the entry to the existing `{{feature}}Entries()`) |
| `Screen.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Screen.kt` |
| `DesktopContent.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}DesktopContent.kt` |
| `MobileContent.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}MobileContent.kt` |
| `ViewModel.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ViewModel.kt` |
| `ViewModelState.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ViewModelState.kt` |
| `State.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}State.kt` |
| `Intent.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Intent.kt` |
| `Effect.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Effect.kt` — **only when one-shot effects exist**; without effects also drop the `effect` property from State/ViewModelState and the `MviEffect` block from the Screen (`ConsumeEffect` stays in Intent) |

Not templated but usually needed: `destination/{{name}}/preview/{{Name}}PreviewFixtures.kt`
(sample domain data for previews — see `ProfilePreviewFixtures.kt`; fixtures duplicate content
because a feature cannot read `core:data`), section components under `destination/{{name}}/component/`,
and feature-local tokens under `theme/` (`{{Name}}Dimensions.kt` / `{{Name}}Animations.kt`).

Templates are skeletons: pull concrete UseCase/model types, Intent/Effect variants, and layout
sections from the Phase 2 reference implementations.

### Phase 5 — MANDATORY wiring in `composeApp/.../navigation/AppNavDisplay.kt`

Both edits, always:

1. Register the NavKey in `navKeySavedStateConfiguration`'s `SerializersModule`:

   ```kotlin
   polymorphic(NavKey::class) {
       subclass(Splash::class, Splash.serializer())
       subclass(Profile::class, Profile.serializer())
       subclass({{Name}}::class, {{Name}}.serializer())   // <- new
   }
   ```

   wasmJs has no reflection — skipping this breaks back-stack serialization at runtime while
   compiling cleanly.

2. Call the entries function inside `entryProvider`:

   ```kotlin
   entryProvider = entryProvider {
       splashEntries(navigateProfile = backStack::navigateProfile)
       profileEntries()
       {{feature}}Entries()   // <- new; pass navigation lambdas: backStack::navigate{{Name}}
   }
   ```

If an existing screen navigates here, thread the `navigate{{Name}}` lambda through that feature's
entries function → public Screen → Effect handler (Intent → Effect → `currentNavigate{{Name}}()`,
retained via `rememberUpdatedState` — see `SplashScreen.kt`).

### Phase 6 — Checklist

Run through `references/checklists/screen.md` to spot misses.

### Phase 7 — Verification (completion criteria)

```bash
./gradlew :feature:{feature}:compileKotlinWasmJs   # wasm (distribution target) compiles
./gradlew :feature:{feature}:compileAndroidMain    # preview-only Android target compiles
./gradlew detekt                                   # lint; autoCorrect is enabled
```

detekt note: because autoCorrect rewrites files, a first run that reformats can end BUILD FAILED —
run detekt again before judging the result. Never fix import ordering manually.

## References

- `.claude/rules/navigation.md` — single NavDisplay, SerializersModule (CRITICAL section), colocation rule
- `.claude/rules/mvi-architecture.md` — MviViewModel contract, Effect lifecycle
- `.claude/rules/ui-implementation.md` — 3-layer screen structure, Islands Dark design rules
- `.claude/rules/naming-conventions.md` — Intent/Effect/callback/package naming
- `.claude/rules/preview.md` — plain `@Preview` + KeiTheme + fixtures pattern
- `.claude/rules/usecase.md` / `.claude/rules/data-layer.md` — layering (UseCase-only injection)

## Important Constraints

- Do not deviate from existing patterns or restructure `AppNavDisplay` without the user's approval
- If templates have drifted from the current code, **follow the current code** — the source of
  truth is `feature/profile`, `feature/splash`, and `.claude/rules/*.md`
- The Android target is preview-only — no Android-specific runtime behavior in the new screen
