# Checklist — Screen (the only destination kind in KEI)

Reference implementations: `feature/profile/src/commonMain/kotlin/io/github/kei_1111/feature/profile/`
(data loading + effects) and `feature/splash/src/commonMain/kotlin/io/github/kei_1111/feature/splash/`
(no injection, navigation effect).

## New feature module only (skip when adding into an existing module)

- [ ] `settings.gradle.kts` — `include(":feature:{feature}")` added in the feature block
- [ ] `feature/{feature}/build.gradle.kts` created with exactly the two convention plugins
      (`alias(libs.plugins.kei1111.detekt)` + `alias(libs.plugins.kei1111.kmp.feature)`) — no
      dependencies block; `KmpFeaturePlugin` wires core:common/designsystem/domain/model/mvi/utils
- [ ] `composeApp/build.gradle.kts` — `implementation(projects.feature.{feature})` added to
      `commonMain.dependencies` (typesafe project accessor style)
- [ ] NO dependency on `core:data` added anywhere in the feature module (layering rule)

## Files created (`destination/{name}/` + `navigation/`)

- [ ] `navigation/{Feature}NavigationRoute.kt` — `@Serializable data object {Name} : NavKey` +
      `fun NavBackStack<NavKey>.navigate{Name}() = add({Name})` colocated in the same file
      (KEI has no separate NavigationExtensions.kt) with `@file:Suppress("MatchingDeclarationName", "Filename")`
- [ ] `navigation/{Feature}Navigation.kt` — `EntryProviderScope<NavKey>.{feature}Entries()` with
      `metroViewModel()` obtained inside the `entry<{Name}>` block (never constructed manually)
- [ ] `{Name}Screen.kt` — public Screen (ViewModel param, `collectAsStateWithLifecycle()`) +
      private Screen (BoxWithConstraints + `windowLayoutFor(screenWidth)` + `LaunchedEffect(layout)`
      dispatching `UpdateLayout`, branching to Mobile/Desktop content)
- [ ] `{Name}DesktopContent.kt` / `{Name}MobileContent.kt` — `(state, onIntent, modifier)`
      signature, no ViewModel reference, SLA section components only
- [ ] `{Name}ViewModel.kt` / `{Name}ViewModelState.kt` / `{Name}State.kt` / `{Name}Intent.kt` /
      `{Name}Effect.kt`
- [ ] `preview/{Name}PreviewFixtures.kt` when Screens/Content previews need sample domain data
      (fixtures duplicate content — a feature cannot read core:data)

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
- [ ] Public Screen wires `MviEffect(effect = state.effect, onConsume = { viewModel.onIntent({Name}Intent.ConsumeEffect) }) { ... }`
      — never handle an effect without ConsumeEffect or it re-fires on recomposition
- [ ] Data loading (if any) collects `useCase().asResult()` in `init {}` and stores the raw
      `Result<T>` in ViewModelState; `toState()` unwraps via `(result as? Result.Success<T>)?.data`

## Layering

- [ ] ViewModel injects UseCases from `core:domain` only — never a Repository
- [ ] No `core:data` Gradle dependency added to the feature module
- [ ] Components below the Content layer receive plain values + callbacks
      (`onClickPage: (EditorPage) -> Unit` style) — never an `Intent`

## Navigation wiring (composeApp/.../navigation/AppNavDisplay.kt) — MANDATORY

- [ ] New NavKey registered in `navKeySavedStateConfiguration`'s `SerializersModule`:
      `subclass({Name}::class, {Name}.serializer())` inside `polymorphic(NavKey::class) { ... }`.
      wasmJs has no reflection — forgetting this compiles fine but silently breaks (or crashes)
      back-stack save/restore. This is the #1 pitfall.
- [ ] `{feature}Entries()` called inside `entryProvider { ... }`, passing any cross-feature
      navigation lambdas (`splashEntries(navigateProfile = backStack::navigateProfile)` style)
- [ ] Cross-feature navigation is a plain lambda parameter on `{feature}Entries()` — the feature
      never depends on another feature module

## UI rules

- [ ] Colors/typography/shapes only from `KeiTheme.colors` / `.typography` / `.shapes`
      (`keiColorScheme.*` in non-composable code); no hardcoded colors — add to `KeiColorScheme` if missing
- [ ] Selection colors: grey `KeiTheme.colors.selectionPill` for tree/list selection; blue pill
      (`tabSelected` + `tabSelectedBorder`) only for the selected editor tab; `androidGreen`
      reserved for content-side accents — never for chrome selection states
- [ ] Feature-local dimensions/animations live in `theme/{Name}Dimensions.kt` /
      `{Name}Animations.kt`, not inline magic numbers

## Preview

- [ ] Every component file has a plain `@Preview` (no parameters and no shared wrapper annotations)
      as a private `{ComponentName}Preview` function at the
      bottom of the same file, hand-wrapped in `KeiTheme { ... }`
- [ ] Screen/Content previews build State from `preview/{Name}PreviewFixtures.kt` — never a live ViewModel
- [ ] Layouts needing bounded constraints get a fixed `Modifier.size(...)` box
      (1280x800 desktop / 390x820 mobile in the real previews)

## Verification

- [ ] `./gradlew :feature:{feature}:compileKotlinWasmJs` passes
- [ ] `./gradlew :feature:{feature}:compileAndroidMain` passes (preview-only Android target)
- [ ] `./gradlew detekt` passes — autoCorrect is enabled: if the first run reports BUILD FAILED
      after reformatting, run it again before judging; never fix import ordering manually
