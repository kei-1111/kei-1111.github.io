# Checklist — Screen (full-window destination)

For a dialog destination (dialog / palette) use `overlay.md`, which restates the
sections that differ and defers to this file for the rest.

Reference implementations: `app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/`
(data loading + effects) and `app/feature/splash/src/commonMain/kotlin/io/github/kei_1111/app/feature/splash/`
(no injection, navigation effect).

## New feature module only (skip when adding into an existing module)

- [ ] `settings.gradle.kts` — `include(":app:feature:{feature}")` added in the feature block
- [ ] `app/feature/{feature}/build.gradle.kts` created with exactly the two convention plugins
      (`alias(libs.plugins.kei1111.detekt)` + `alias(libs.plugins.kei1111.kmp.feature)`) — no
      dependencies block; `KmpFeaturePlugin` wires core:common/designsystem/domain/model/mvi/navigation/utils
- [ ] `app/webApp/build.gradle.kts` — `implementation(projects.app.feature.{feature})` added to
      `commonMain.dependencies` (typesafe project accessor style)
- [ ] NO dependency on `core:data` added anywhere in the feature module (layering rule)

## Files created (`destination/{name}/` + `navigation/`)

- [ ] `navigation/{Feature}NavigationRoute.kt` — `@Serializable data object {Name} : NavKey`, plus
      any result type produced by the destination, with `@file:Suppress("MatchingDeclarationName", "Filename")`
- [ ] `navigation/{Feature}NavigationExtensions.kt` —
      `fun NavBackStack<NavKey>.navigate{Name}() = add({Name})`; omitted only when no extension is needed
- [ ] `navigation/{Feature}Navigation.kt` — `EntryProviderScope<NavKey>.{feature}Entries()` with
      `metroViewModel()` obtained inside the `entry<{Name}>` block (never constructed manually)
- [ ] `{Name}ScreenRoot.kt` — takes the ViewModel, `collectAsStateWithLifecycle()`, `MviEffect`
      wiring, and any environment bridges dispatching Intents (font loading / page visibility —
      see `SplashScreenRoot.kt`)
- [ ] `{Name}Screen.kt` — internal pure-UI layer (BoxWithConstraints + `windowLayoutFor(screenWidth)`
      + `LaunchedEffect(layout)` dispatching `UpdateLayout`, branching to Mobile/Desktop content)
- [ ] `content/{Name}DesktopContent.kt` / `content/{Name}MobileContent.kt` — `(state, onIntent, modifier)`
      signature, no ViewModel reference, SLA section components only
- [ ] `{Name}ViewModel.kt` / `{Name}ViewModelState.kt` / `{Name}State.kt` / `{Name}Intent.kt` /
      `{Name}Effect.kt`
- [ ] Destination-local UI model types (enums etc.) under `model/` (see `EditorViewMode.kt` /
      `SplashFont.kt`) — an organizational subpackage, not a dependency layer; the
      `destination/{name}/` top level holds only the seven contract/orchestration files
      (`ScreenRoot` / `Screen` + the five MVI files)
- [ ] Destination-specific tokens and UI helpers under `theme/` (`{Name}Dimensions` /
      `{Name}Animations`), not inline magic numbers
- [ ] `preview/{Name}PreviewFixtures.kt` when Screens/Content previews need sample domain data
      (fixtures duplicate content — a feature cannot read core:data)

## Destination isolation — MUST

- [ ] This destination neither imports from another `destination/*/` nor is imported by one.
      Everything is `internal`, so only `scripts/check_destination_isolation.sh` catches it
- [ ] Nothing was promoted out of a destination for convenience — only types whose consumers all
      change them for the same reason, and never a component (see the project's UI rules)

## MVI wiring

- [ ] ViewModel annotated class-level `@Inject` + `@ViewModelKey` +
      `@ContributesIntoMap(AppScope::class, binding<ViewModel>())`, extends
      `MviViewModel<{Name}ViewModelState, {Name}State, {Name}Intent>()`
- [ ] `createInitialViewModelState()` / `createInitialState()` implemented
- [ ] `onIntent` branch logic written inline in the `when` (no private per-branch handler
      functions; private helpers only for init/observe work like `loadContributions`)
- [ ] `{Name}Intent` includes `data object ConsumeEffect`; its branch clears the effect:
      `updateViewModelState { copy(effect = null) }`
- [ ] `{Name}ViewModelState` implements `ViewModelState<{Name}State>` with `toState()`; both it
      and `{Name}State` carry `effect: {Name}Effect?`
- [ ] `{Name}State` is a plain data class with defaults (a sealed interface is only warranted for
      distinct Idle/Loading/Error phases — none exist in KEI today)
- [ ] `{Name}Effect` is a plain sealed interface with NO core:mvi marker (both real Effect files
      have zero imports); variants are chosen for THIS destination — `Open{Target}` mirrors its
      Intent (`ProfileEffect.OpenUrl`), navigation is `Navigate{Destination}`
      (`SplashEffect.NavigateProfile`) — never copy `OpenUrl` blindly
- [ ] `UpdateLayout` branch stores the layout; per-layout UI state resets only when the breakpoint
      actually changes (see ProfileViewModel's `UpdateLayout` branch). A destination with no
      per-layout state to reset (Splash) omits `UpdateLayout`/`currentLayout` entirely and
      branches on `windowLayoutFor` directly in the internal `{Name}Screen`
- [ ] No `// PLACEHOLDER:` comment from the templates survives in the generated files
- [ ] `{Name}ScreenRoot` wires `MviEffect(effect = state.effect, onConsume = { viewModel.onIntent({Name}Intent.ConsumeEffect) }) { ... }`
      — never handle an effect without ConsumeEffect or it re-fires on recomposition
- [ ] Data loading (if any) collects `useCase().asResult()` in `init {}` and stores the raw
      `Result<T>` in ViewModelState; `toState()` unwraps via `(result as? Result.Success<T>)?.data`

## Layering

- [ ] ViewModel injects UseCases from `core:domain` only — never a Repository
- [ ] No `core:data` Gradle dependency added to the feature module
- [ ] Components below the Content layer receive plain values + callbacks
      (`onClickPage: (EditorPage) -> Unit` style) — never an `Intent`

## Navigation wiring (app/webApp/.../navigation/AppNavDisplay.kt) — MANDATORY

- [ ] New NavKey registered in `navKeySavedStateConfiguration`'s `SerializersModule`:
      `subclass({Name}::class, {Name}.serializer())` inside `polymorphic(NavKey::class) { ... }`.
      wasmJs has no reflection — forgetting this compiles fine but silently breaks (or crashes)
      back-stack save/restore. This is the #1 pitfall.
- [ ] `{feature}Entries()` called inside `entryProvider { ... }`, passing any cross-feature
      navigation lambdas (`splashEntries(navigateProfile = backStack::navigateProfile)` style) —
      new feature module only; an existing feature's entries call is already wired
- [ ] Cross-feature navigation is a plain lambda parameter on `{feature}Entries()` — the feature
      never depends on another feature module
- [ ] When receiving a one-shot result, the result type is declared beside the producing `NavKey`;
      `ResultEffect<ResultType>(LocalResultEventBus.current)` runs inside the receiving `entry<>`
      block and dispatches an existing Intent

## UI rules

- [ ] Colors/typography/shapes only from `KeiTheme.colors` / `.typography` / `.shapes`
      (`keiColorScheme.*` in non-composable code); no hardcoded colors — add to `KeiColorScheme` if missing
- [ ] Selection colors match the corresponding surface in the real Android Studio (per surface —
      never generalized from another surface): grey `KeiTheme.colors.selectionPill` for tree rows
      and view-mode toggles; the blue pill (`tabSelected`, plus `tabSelectedBorder` where AS draws
      a border) for the selected editor tab and the Search Everywhere selection/focus. A surface
      needing something outside that list extends the project's UI rules in the same change;
      `androidGreen` is content-side only — never a chrome selection state
- [ ] Destination-specific dimensions/animations live in the destination's `theme/` subpackage as
      `{Name}Dimensions.kt` / `{Name}Animations.kt`, not inline magic numbers; a token shared by two
      destinations moves up to the feature-level `theme/`

## Preview

- [ ] Every component file has a plain `@Preview` (no parameters and no shared wrapper annotations)
      as a private `{ComponentName}Preview` function at the
      bottom of the same file, hand-wrapped in `KeiTheme { ... }`
- [ ] Screen/Content previews build State from `preview/{Name}PreviewFixtures.kt` — never a live ViewModel
- [ ] Layouts needing bounded constraints get a fixed `Modifier.size(...)` box
      (1280x800 desktop / 390x820 mobile in the real previews)

## Verification

- [ ] `./gradlew :app:feature:{feature}:compileKotlinWasmJs` passes
- [ ] `./gradlew :app:feature:{feature}:compileAndroidMain` passes (preview-only Android target)
- [ ] `./gradlew :app:webApp:compileKotlinWasmJs` passes — covers the mandatory
      `AppNavDisplay` wiring from Phase 5, which feature-only compiles cannot catch
- [ ] `./gradlew detekt` passes — autoCorrect is enabled: if the first run reports BUILD FAILED
      after reformatting, run it again before judging; never fix import ordering manually
