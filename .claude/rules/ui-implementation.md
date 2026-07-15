---
paths: "app/feature/**/*.kt,app/core/designsystem/**/*.kt"
---

# UI Implementation Guide

This document defines UI implementation patterns for the kei-1111.github.io project.

---

## Screen Structure (MVI + Breakpoint-Branching)

Screens follow a 3-layer pattern. A screen is never handed raw `Intent`-dispatch access below the Content layer — leaf components only ever see plain values and callbacks.

| Layer | Role | File |
|-------|------|------|
| public Screen | Takes the `ViewModel`, collects `state` via `collectAsStateWithLifecycle()`, handles one-shot Effects via the `MviEffect` composable | `XxxScreen.kt` (public overload) |
| private Screen | Measures screen width (`BoxWithConstraints`), branches by breakpoint, forwards `state` + `onIntent` down | `XxxScreen.kt` (private overload, same file) |
| Desktop/Mobile Content | Layout per form factor. Takes `state: XxxState` and `onIntent: (XxxIntent) -> Unit` — no `ViewModel` reference | `XxxDesktopContent.kt` / `XxxMobileContent.kt` |
| Component | Pure UI rendering. Plain value + callback params (`onClickPage: (EditorPage) -> Unit`) — **never** an `Intent` | `component/*.kt` |

`onIntent` flows down only when the UI dispatches intents — Splash's Content layers take `state` only (all `SplashIntent`s fire from the public Screen).

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

- Breakpoint: below `900.dp` is Mobile (same IDE chrome as Desktop — TitleBar, tool rails, status bar — but the tree opens as an overlay from the LeftToolRail and the editor island defaults to PreviewOnly; Split stacks code above preview)
- UI state that must sync across components (e.g. selected `EditorPage`) lives in `State` and is passed down as value + callback (`selectedPage` / `onClickPage` → dispatches `UpdateSelectedPage`/`UpdateSelectedPageFromTree` intents from the Content layer)

---

## MVI Type Conventions

The five MVI files per screen (`XxxViewModelState` / `XxxState` / `XxxIntent` / `XxxEffect` / `XxxViewModel`), the Metro ViewModel annotations, the inline-`onIntent` policy, and `MviEffect` handling are defined in `.claude/rules/mvi-architecture.md` — that file is the canonical home. UI-side takeaway: always wire `MviEffect` with `ConsumeEffect` in the public Screen (see the example at the top of this file).

---

## `destination/<name>/` Directory Layout

Each screen lives under `destination/<name>/` inside its feature module. Example — `app/feature/profile`:

```
app/feature/profile/src/commonMain/kotlin/.../feature/profile/
├── theme/                            # feature-local UI tokens. Colors/typography/shapes come from KeiTheme
│   ├── ProfileDimensions.kt          # gaps/widths/sizes (DeskPadding, IslandGap, RailWidth, ChromeIconSize, GitHubCardPadding, ...)
│   ├── ProfileAnimations.kt          # animation durations (caret blink, hover transition)
│   ├── HoverState.kt                 # rememberHoverState() — shared hover interaction-source helper
│   ├── DeskBackground.kt             # Modifier.deskBackground() — desk background + top-left blue glow
│   └── SyntaxHighlighter.kt          # highlightKotlin() — AS-style syntax highlighting (pure fn, takes KeiColorScheme)
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
    ├── component/                    # one file per section: TitleBar, ProjectTree, EditorPane, PreviewPane,
    │   │                             # ToolRail, StatusBar, CodeContent, githubcard/; plus shared
    │   │                             # ChromeIconButton, EditorPreviewIsland. SLA sub-components stay private in-file
    └── preview/
        └── ProfilePreviewFixtures.kt  # sample GitHubProfile used by @Preview functions
```

`feature/splash` mirrors this shape: a feature-local `theme/` (`SplashDimensions`/`SplashAnimations`) alongside `destination/splash/`. Splash colors/fonts come from the shared `KeiTheme` (`KeiTheme.colors.splash*`).

---

## Navigation Entry Pattern

Route/entries file layout, `metroViewModel()` usage, cross-feature navigation lambdas, and the **mandatory SerializersModule registration for every new `NavKey`** are defined in `.claude/rules/navigation.md` — that file is the canonical home.

---

## Component Responsibilities

### Be a Pure View

1. UI rendering
2. Event notification via callbacks (`onClickPage: (EditorPage) -> Unit`)
3. Rendering only what it receives as `State`/parameters — components never read `app:core:data` or call a UseCase/Repository themselves; that boundary belongs to the ViewModel

### Responsibilities It Should NOT Have

- Holding sync-relevant state internally (hoist it to `ViewModelState`/`State`)
- Fetching or deciding *how* to obtain data (that's the ViewModel → UseCase → Repository's job) — a component only displays the `State` it's given, whether the underlying data is static (`ProfileRepository`) or fetched (`ContributionsRepository`)

---

## Single Level of Abstraction (SLA)

A container's **direct children must all be the same granularity**: either all leaf composables (`Text`, `Image`, `KeiIcon`, ...) or all named components. Judge each container by its direct children only, and apply the rule recursively at every level — not just in Desktop/Mobile Content layers.

- If any sibling is a named component, extract the remaining leaves into named components too
- A container whose direct children are all leaves needs no extraction
- Exception: structural elements (`Spacer` / `HorizontalDivider` / `VerticalDivider`) may be placed directly at any level

```kt
// NG — leaf, inline composite layout, and leaf mixed at one level
Column {
    Text(repo.name)
    Row {
        Image(languageIcon)
        Text(repo.language)
    }
    Image(ownerAvatar)
}

// OK — direct children unified into same-granularity named components
Column {
    RepoName(name = repo.name)
    RepoLanguage(icon = languageIcon, language = repo.language) // its Row { Image; Text } is all leaves — OK
    OwnerAvatar(avatar = repo.ownerAvatar)
}
```

Extracted components should express "what is this component for" rather than "what does it display".

---

## File Splitting

**Split by cohesion — never by declaration count or call depth.** A file is the unit read and changed together: one `component/` file per section (`TitleBar`, `ProjectTree`, `EditorPane`, `PreviewPane`, `StatusBar`, `GitHubPreviewCard`, ...), holding that section's whole SLA tree as `private` sub-components. SLA multiplies composables, not files.

A section file may expose more than one composable when they form one concept — `EditorPane.kt` exposes `EditorTabBar` + `EditorCodeArea`; `ToolRail.kt` exposes both rails. Do **not** split them out just because a second file calls them: `internal` is module-wide regardless of file count, so splitting adds no encapsulation.

### What to Keep in the Same File

- Every `private` sub-component the section's composables call (the whole SLA tree)
- The section's `@Preview` function(s) (bottom of the file)

Never widen a composable's visibility for a `@Preview` in another file — put the preview beside what it previews.

`@file:Suppress("TooManyFunctions")` is expected on a section file whose SLA tree exceeds the detekt threshold — the intended trade-off, not a smell.

### When a Separate File Is Warranted

- Genuinely shared across sibling sections, so it cannot be `private` in any one of them — `ChromeIconButton` (`TitleBar` + `ToolRail`), `EditorPreviewIsland` (Desktop + Mobile Content), `theme/HoverState.kt`
- A component that grows into an independently-evolving unit, or a file that becomes unwieldy

See the [`destination/<name>/` Directory Layout](#destinationname-directory-layout) section above for the full file breakdown (Screen / ViewModel / MVI types / Content / component / preview).

---

## Padding Design

**Do not add Padding to child components as if it were a margin**. The parent container sets internal Padding to secure spacing.

---

## IDE Design Rules (Islands Dark / Light)

`feature/profile` mimics the Android Studio New UI (Islands Dark and Light, switchable via `KeiThemeController`). When touching its UI:

- Colors come from `KeiTheme.colors.*` (in `@Composable` code) or the default instance `keiColorScheme.*` (in non-composable code — `drawBehind`, `DeskBackground.kt`, etc.); shapes/radii from `KeiTheme.shapes.*`; gaps/widths from `ProfileDimensions`. Never hardcode new colors — add a field to `KeiColorScheme` instead. The syntax highlighter (`highlightKotlin`/`codeLinesFor`) is a pure function that takes a `KeiColorScheme` parameter rather than reading the global instance
- The desk (`KeiTheme.colors.desk`; dark measured `#26282C`, light measured from a real AS Light screenshot) is the window background itself. In dark it has a blue glow at the top-left (`Modifier.deskBackground()` in `theme/DeskBackground.kt`, which reads `keiColorScheme` since it is non-composable); light has no glow (`deskGlow` equals `desk`). Title bar, status bar, and tool rails sit transparently on it
- Panels are floating rounded "islands" on the desk: the project tree uses the darker `KeiTheme.colors.islandDark`, editor/preview use `KeiTheme.colors.island`, with no island borders — matching real AS Islands Dark/Light
- Tree rows and view-mode toggles use grey `KeiTheme.colors.selectionPill` for selection; the selected editor tab uses the blue pill (`KeiTheme.colors.tabSelected` fill + `KeiTheme.colors.tabSelectedBorder` border), matching real AS Islands Dark/Light. Android green (`KeiTheme.colors.androidGreen`) is reserved for content-side accents (primary button, brand tile). Never use green for chrome
- The editor code (left) and the Preview pane (right) must always show the same data — update both together
- Typography: base `TextStyle`s live on `KeiTheme.typography` — `code` / `chrome` (JetBrains Mono), `cardJp` (Noto Sans JP) / `githubJp` (Zen Kaku Gothic New), and `mono` (splash). Adjust per-use size/weight/color via `.copy(...)`
- Hover feedback uses `KeiTheme.colors.chip` on islands and the translucent `KeiTheme.colors.deskChip` on the desk/gradient; keep transitions subtle. No always-running animations except the editor caret blink
- Syntax highlight colors (`KeiTheme.colors.syntax*`) are dark = measured from a real AS screenshot, light = the IntelliJ Light default scheme: named args cyan(-ish), numbers teal/blue, composable calls green, enum entries magenta italic, type references plain

---

## Preview

When implementing a UI component, add a plain `@Preview` wrapped in `KeiTheme { ... }` at the bottom of the same file. Full rules (annotation, naming, fixtures, Android-target requirement) live in `.claude/rules/preview.md` — that file is the canonical home.
