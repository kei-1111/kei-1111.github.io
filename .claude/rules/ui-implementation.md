---
paths:
  - "app/feature/**/*.kt"
  - "app/core/designsystem/**/*.kt"
---

# UI Implementation Guide

## Screen Structure (MVI + Breakpoint-Branching)

Screens follow a ScreenRoot → Screen → Content → Component layering; raw `Intent`-dispatch access never goes below the Content layer.

| Layer | Role | File |
|-------|------|------|
| ScreenRoot | Takes the `ViewModel`, collects `state` via `collectAsStateWithLifecycle()`, handles one-shot Effects via the `MviEffect` composable, and hosts environment bridges (Splash's font loading / page visibility) | `XxxScreenRoot.kt` |
| Screen | `internal` pure-UI layer. Measures screen width (`BoxWithConstraints`), branches by breakpoint via `windowLayoutFor(screenWidth)`, forwards `state` + `onIntent` down (Profile dispatches `UpdateLayout` here, tied to the constraint measurement) | `XxxScreen.kt` |
| Desktop/Mobile Content | Layout per form factor. Takes `state: XxxState` and `onIntent: (XxxIntent) -> Unit` — no `ViewModel` reference | `content/XxxDesktopContent.kt` / `content/XxxMobileContent.kt` |
| Component | Pure UI rendering. Plain value + callback params — **never** an `Intent` | `component/*.kt` |

- Reference: `app/feature/profile/.../destination/profile/ProfileScreenRoot.kt` + `ProfileScreen.kt` (the latter with `LaunchedEffect(layout) { onIntent(UpdateLayout(layout)) }` on breakpoint change).
- `onIntent` flows down only when the UI dispatches intents — Splash's Content layers take `state` only (all `SplashIntent`s fire from `SplashScreenRoot`).
- Breakpoint: below `900.dp` is Mobile — same IDE chrome as Desktop, but the tree opens as an overlay from the ToolRail and the editor island defaults to PreviewOnly (Split stacks code above preview).
- UI state that must sync across components (e.g. selected `EditorPage`) lives in `State` and is passed down as value + callback; the Content layer maps the callback back to an Intent.

MVI types, ViewModel annotations, inline-`onIntent` policy, and `MviEffect`/`ConsumeEffect` wiring: `.claude/rules/mvi-architecture.md` (canonical home). Route/entries layout, `metroViewModel()`, and the mandatory SerializersModule registration: `.claude/rules/navigation.md` (canonical home).

## `destination/<name>/` Directory Layout

Each screen lives under `destination/<name>/` in its feature module. The top level holds only the seven destination contract and orchestration files — `XxxScreenRoot.kt`, `XxxScreen.kt`, and the five MVI files; everything else goes into purpose-named subpackages (organizational subpackages, not dependency layers):

- `content/` — `XxxDesktopContent.kt` / `XxxMobileContent.kt`
- `model/` — screen-local UI model types (profile: `EditorPage.kt` with `EditorViewMode`; splash: `BuildStatus.kt` / `SplashFont.kt` / `SplashStep.kt`)
- `component/` — section components
- `preview/` — sample data (`XxxPreviewFixtures.kt`)

Feature-local UI tokens live under the feature's `theme/` (`XxxDimensions` / `XxxAnimations`, plus e.g. `DeskBackground.kt` and `SyntaxHighlighter.kt` in profile); route/entries files under `navigation/`. Reference: `app/feature/profile/`, mirrored by `app/feature/splash`.

## Component Responsibilities

- Pure view: render what it receives, notify events via callbacks. Components never read `app:core:data` or call a UseCase/Repository — that boundary belongs to the ViewModel.
- Do not hold sync-relevant state internally (hoist it to `ViewModelState`/`State`), and do not fetch or decide how to obtain data.
- Single level of abstraction in Content layers: place only components at the same level (`TitleBar` / `ProjectTree` / `EditorPane` / `PreviewPane` / `StatusBar`); name components for their purpose, not what they display.
- Keep `private` sub-components and the component's `@Preview` function in the same file.
- Padding: the parent container sets internal padding to secure spacing — do not add padding to child components as if it were a margin.

## IDE Design Rules (Islands Dark / Light)

`app/feature/profile` mimics the Android Studio New UI (Islands Dark and Light, switchable via `KeiThemeController`). When touching its UI:

- Colors come from `KeiTheme.colors.*` in `@Composable` code, or the default instance `keiColorScheme.*` in non-composable code (`drawBehind`, `DeskBackground.kt`); shapes/radii from `KeiTheme.shapes.*`; gaps/widths from `ProfileDimensions`. Never hardcode a new color — add a field to `KeiColorScheme` instead. The syntax highlighter (`highlightKotlin`/`codeLinesFor`) is a pure function taking a `KeiColorScheme` parameter.
- The desk (`KeiTheme.colors.desk`) is the window background itself; both themes draw a top-left glow (`Modifier.deskBackground()`): a horizontal desk→`deskGlow`→desk ramp centered under the project chip, fading back to `desk` within 300dp of the top edge, as in real AS Islands (the tint comes from the IDE's per-project color — currently warm gray, not blue). Title bar, status bar, and tool rails sit transparently on it.
- Panels are floating rounded "islands" on the desk: the project tree uses the darker `islandDark`, editor/preview use `island`, with no island borders.
- Selection: grey `KeiTheme.colors.selectionPill` for tree rows and view-mode toggles; the selected editor tab uses the blue pill (`tabSelected` fill + `tabSelectedBorder` border). Android green (`androidGreen`) is reserved for content-side accents (primary button, brand tile) — **never** for chrome selection states.
- The editor code pane (left) and the Preview pane (right) must always show the same data — update both together. The one sanctioned divergence: while the edited `ProfileScreen.kt` fails to parse, the preview keeps rendering the last successfully parsed data and shows the Out-of-date status.
- Typography: base `TextStyle`s live on `KeiTheme.typography` — `code` / `chrome` (JetBrains Mono), `cardJp` (Noto Sans JP), `githubJp` (Zen Kaku Gothic New), `mono` (splash); adjust per-use size/weight/color via `.copy(...)`.
- Hover feedback uses `chip` on islands and the translucent `deskChip` on the desk; keep transitions subtle. No always-running animations except the editor caret blink.
- Syntax highlight colors (`KeiTheme.colors.syntax*`): dark measured from a real AS screenshot, light from the IntelliJ Light default scheme.

## Preview

Add a plain `@Preview` wrapped in `KeiTheme { ... }` at the bottom of each component file. Full rules (annotation, naming, fixtures, Android-target requirement): `.claude/rules/preview.md` (canonical home).

## Browser Smoke Test

After a user-visible UI change, verify in a real browser — compilation proves nothing about runtime behavior:

1. `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun` → http://localhost:8080
2. Splash completes and transitions to Profile
3. Resize across the 900dp breakpoint — check both desktop and mobile layouts
4. Exercise the interactions and links the change touched
5. Editor code pane and Preview pane still show the same data

Report which steps were performed; never claim browser verification from compilation alone.
