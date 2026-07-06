---
paths: "feature/**/*.kt,core/designsystem/**/*.kt"
---

# UI Implementation Guide

This document defines UI implementation patterns for the kei-1111.github.io project.

---

## Screen Structure (MVI + Breakpoint-Branching)

Screens follow a 3-layer pattern (withmo-style). A screen is never handed raw `Intent`-dispatch access below the Content layer — leaf components only ever see plain values and callbacks.

| Layer | Role | File |
|-------|------|------|
| public Screen | Takes the `ViewModel`, collects `state` via `collectAsStateWithLifecycle()`, handles one-shot Effects via the `MviEffect` composable | `XxxScreen.kt` (public overload) |
| private Screen | Measures screen width (`BoxWithConstraints`), branches by breakpoint, forwards `state` + `onIntent` down | `XxxScreen.kt` (private overload, same file) |
| Desktop/Mobile Content | Layout per form factor. Takes `state: XxxState` and `onIntent: (XxxIntent) -> Unit` — no `ViewModel` reference | `XxxDesktopContent.kt` / `XxxMobileContent.kt` |
| Component | Pure UI rendering. Plain value + callback params (`onClickPage: (EditorPage) -> Unit`) — **never** an `Intent` | `component/*.kt` |

**Example**: `feature/profile/src/commonMain/kotlin/.../destination/profile/ProfileScreen.kt`

```kt
@Composable
internal fun ProfileScreen(viewModel: ProfileViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MviEffect(
        effect = state.effect,
        onConsume = { viewModel.onIntent(ProfileIntent.ConsumeEffect) },
    ) { effect ->
        when (effect) {
            is ProfileEffect.OpenUrl -> openUrl(effect.url)
        }
    }

    ProfileScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
private fun ProfileScreen(state: ProfileState, onIntent: (ProfileIntent) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val layout = windowLayoutFor(screenWidth)
        LaunchedEffect(layout) { onIntent(ProfileIntent.UpdateLayout(layout)) }
        when (layout) {
            WindowLayout.Mobile -> ProfileMobileContent(state = state, onIntent = onIntent)
            WindowLayout.Desktop -> ProfileDesktopContent(state = state, onIntent = onIntent)
        }
    }
}
```

- Breakpoint: below `900.dp` is Mobile (same IDE chrome as Desktop — TitleBar, tool rails, status bar — but the tree opens as an overlay from the ToolRail and the editor island defaults to PreviewOnly; Split stacks code above preview)
- UI state that must sync across components (e.g. selected `EditorPage`) lives in `State` and is passed down as value + callback (`selectedPage` / `onClickPage` → dispatches `UpdateSelectedPage`/`UpdateSelectedPageFromTree` intents from the Content layer)

---

## MVI Type Conventions

Every screen defines five files (`Xxx` = feature/destination name, e.g. `Profile`, `Splash`):

- **`XxxViewModelState`** — `internal data class`, the ViewModel's internal state. May hold implementation details the UI doesn't need directly (e.g. `contributionsResult: Result<ContributionCalendar>`). Implements `ViewModelState<XxxState>` and converts via `override fun toState()`. **Includes the `effect: XxxEffect?` property.**
- **`XxxState`** — `internal data class` (or `sealed interface` for screens with distinct Idle/Loading/Error phases), implements `State`. Exposed to the UI via `viewModel.state`. **Also carries `effect: XxxEffect?`.**
- **`XxxIntent`** — `internal sealed interface : Intent`. Always includes a `ConsumeEffect` data object.
- **`XxxEffect`** — `internal sealed interface`, one-shot side effects (navigation, opening a URL). Not part of `State`'s persisted data — cleared back to `null` once handled.
- **`XxxViewModel`** — `internal class`, extends `MviViewModel<XxxViewModelState, XxxState, XxxIntent>()`, annotated `@Inject @ViewModelKey @ContributesIntoMap(AppScope::class, binding<ViewModel>())`. Injects UseCases from `core:domain`, never a Repository.

Effect handling in the public Screen always follows the same shape:

```kt
MviEffect(
    effect = state.effect,
    onConsume = { viewModel.onIntent(XxxIntent.ConsumeEffect) },
) { effect ->
    when (effect) {
        is XxxEffect.SomeEffect -> ...
    }
}
```

`MviEffect` runs `onHandle` inside a `LaunchedEffect(effect)` and calls `onConsume` right after — never handle an Effect without also wiring `ConsumeEffect`, or it will keep re-firing on recomposition.

---

## `destination/<name>/` Directory Layout

Each screen lives under `destination/<name>/` inside its feature module. Example — `feature/profile`:

```
feature/profile/src/commonMain/kotlin/.../feature/profile/
├── IdeUi.kt                          # feature-local UI tokens: deskBackground() + IdeDimens (gaps/widths). Colors/typography/shapes come from KeiTheme
├── SyntaxHighlighter.kt
├── navigation/
│   ├── ProfileNavigationRoute.kt     # @Serializable NavKey (`Profile`)
│   └── ProfileNavigation.kt          # EntryProviderScope<NavKey>.profileEntries()
└── destination/profile/
    ├── ProfileScreen.kt              # public Screen + private Screen (breakpoint branch)
    ├── ProfileViewModel.kt
    ├── ProfileViewModelState.kt
    ├── ProfileState.kt
    ├── ProfileIntent.kt
    ├── ProfileEffect.kt
    ├── EditorPage.kt                 # EditorPage / EditorViewMode enums
    ├── ProfileDesktopContent.kt
    ├── ProfileMobileContent.kt
    ├── component/                    # section-level components: TitleBar, ProjectTree, EditorPane,
    │   │                             # PreviewPane, ToolRail, StatusBar, CodeContent, githubcard/...
    └── preview/
        └── ProfilePreviewFixtures.kt  # sample GitHubProfile used by @Preview functions
```

`feature/splash` follows the same shape under `destination/splash/` (plus a feature-local `theme/` for `SplashAnimations`/`SplashDimensions`). Splash colors/fonts come from the shared `KeiTheme` (`KeiTheme.colors.splash*`).

---

## Navigation Entry Pattern

- Each feature defines its `NavKey` route(s) in `navigation/XxxNavigationRoute.kt` (`@Serializable data object Xxx : NavKey`)
- Each feature exposes one `EntryProviderScope<NavKey>.xxxEntries()` extension in `navigation/XxxNavigation.kt`. The ViewModel is obtained *inside* the entry via `metroViewModel()` — never constructed manually or passed in from outside
- `composeApp`'s `AppNavDisplay` owns the single `NavDisplay` and back stack, and wires every feature's entries together:

```kt
entryProvider = entryProvider {
    splashEntries(navigateProfile = backStack::navigateProfile)
    profileEntries()
}
```

- A feature that needs to trigger navigation takes a plain lambda parameter in its `xxxEntries()` function (see `splashEntries(navigateProfile: () -> Unit)`) rather than depending on another feature or a shared navigation module

---

## Component Responsibilities

### Be a Pure View

1. UI rendering
2. Event notification via callbacks (`onClickPage: (EditorPage) -> Unit`)
3. Rendering only what it receives as `State`/parameters — components never read `core:data` or call a UseCase/Repository themselves; that boundary belongs to the ViewModel

### Responsibilities It Should NOT Have

- Holding sync-relevant state internally (hoist it to `ViewModelState`/`State`)
- Fetching or deciding *how* to obtain data (that's the ViewModel → UseCase → Repository's job) — a component only displays the `State` it's given, whether the underlying data is static (`ProfileRepository`) or fetched (`ContributionsRepository`)

---

## Single Level of Abstraction (SLA)

In Desktop/Mobile Content layers, **place only components at the same level of abstraction** (e.g. `TitleBar` / `ProjectTree` / `EditorPane` / `PreviewPane` / `StatusBar`).

Extracted components should express "what is this component for" rather than "what does it display".

---

## File Splitting

### What to Keep in the Same File

- `private` sub-components internal to a component
- The component's `@Preview` function (bottom of the file)

See the [`destination/<name>/` Directory Layout](#destinationname-directory-layout) section above for the full file breakdown (Screen / ViewModel / MVI types / Content / component / preview).

---

## Padding Design

**Do not add Padding to child components as if it were a margin**. The parent container sets internal Padding to secure spacing.

---

## IDE Design Rules (Islands Dark)

`feature/profile` mimics the Android Studio New UI (Islands Dark). When touching its UI:

- Colors come from `KeiTheme.colors.*` (in `@Composable` code) or the default instance `keiColorScheme.*` (in non-composable code — syntax highlighter, `drawBehind`, etc.); shapes/radii from `KeiTheme.shapes.*`; gaps/widths from `IdeDimens`. Never hardcode new colors — add a field to `KeiColorScheme` instead
- The desk (`KeiTheme.colors.desk` #26282C) is the window background itself, with a blue glow at the top-left (`Modifier.deskBackground()` in IdeUi.kt, which reads `keiColorScheme` since it is non-composable). Title bar, status bar, and tool rails sit transparently on it
- Panels are floating rounded "islands" on the desk: the project tree uses the darker `KeiTheme.colors.islandDark`, editor/preview use `KeiTheme.colors.island`, with no island borders — matching real AS Islands Dark
- Tree rows and view-mode toggles use grey `KeiTheme.colors.selectionPill` for selection; the selected editor tab uses the blue pill (`KeiTheme.colors.tabSelected` fill + `KeiTheme.colors.tabSelectedBorder` border), matching real AS Islands Dark. Android green (`KeiTheme.colors.androidGreen`) is reserved for content-side accents (primary button, brand tile). Never use green for chrome
- The editor code (left) and the Preview pane (right) must always show the same data — update both together
- Typography: base `TextStyle`s live on `KeiTheme.typography` — `code` / `chrome` (JetBrains Mono), `cardJp` (Noto Sans JP) / `githubJp` (Zen Kaku Gothic New), and `mono` (splash). Adjust per-use size/weight/color via `.copy(...)`
- Hover feedback uses `KeiTheme.colors.chip` on islands and the translucent `KeiTheme.colors.deskChip` on the desk/gradient; keep transitions subtle. No always-running animations except the editor caret blink
- Syntax highlight colors (`KeiTheme.colors.syntax*`, read as `keiColorScheme.syntax*` in the non-composable highlighter) are measured from a real AS screenshot: named args cyan, numbers teal, composable calls green, enum entries magenta italic, type references plain

---

## Preview

When implementing a UI component, add a Preview in the same file:

1. Use the unified annotation `androidx.compose.ui.tooling.preview.Preview` (plain `@Preview`, no parameters)
2. Wrap the content in `KeiTheme { ... }` so the Islands Dark palette/typography/shapes are provided
3. Place it as a `private` function at the bottom of the component's file
4. Rendering relies on the preview-only Android target — do not remove `androidLibrary` from the `kei_1111.kmp.wasm` convention plugin
5. Screens/Content that require a `State` should build one from `preview/XxxPreviewFixtures.kt` sample data rather than a live `ViewModel` (see `ProfilePreviewFixtures.kt`)
