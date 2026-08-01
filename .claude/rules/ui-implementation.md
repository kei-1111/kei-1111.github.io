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
- `model/` — destination-local UI model types (profile: `EditorViewMode.kt`; splash: `BuildStatus.kt` / `SplashFont.kt` / `SplashStep.kt`)
- `component/` — section components
- `preview/` — sample data (`XxxPreviewFixtures.kt`)
- `theme/` — destination-specific UI tokens and helpers (`XxxDimensions` / `XxxAnimations`; profile also has `DeskBackground.kt` / `SyntaxHighlighter.kt`)

Route/entries files live under `navigation/`. Reference: `app/feature/profile/`, mirrored by `app/feature/splash`.

### Destination Isolation — MUST

Nothing under `destination/<a>/` may be referenced from `destination/<b>/`, components most of all.
Everything is `internal`, so the compiler allows it and `scripts/check_destination_isolation.sh`
catches it instead. Two destinations needing the same UI element either each keep their own, or get
a real shared component in `app/core/designsystem` — never an import across destinations, never a
component hoisted to the feature level.

Only types and non-component helpers may be promoted out of a destination, and only when every
consumer changes them for the same reason. `EditorPage` qualifies: adding an editor file changes the
editor, the palette, and the navigation result contract together. Similar shape or avoiding an
import do not qualify — there, each destination keeps its own type and converts at the boundary.

| Promoted type | Home |
|---|---|
| Feature vocabulary | that feature's `model/` — `EditorPage.kt` |
| App-wide, defines how something looks | `app:core:designsystem` — `LinkServiceStyle.kt` |
| App-wide, stateful helper with no visual identity | `app:core:ui` — `HoverState.kt` |

Promoted types must not depend on `destination.*`; the sole exception is `navigation/`, which
composes destinations by referencing their Roots and ViewModels.

## Component Responsibilities

- Pure view: render what it receives, notify events via callbacks. Components never read `app:core:data` or call a UseCase/Repository — that boundary belongs to the ViewModel.
- Do not hold sync-relevant state internally (hoist it to `ViewModelState`/`State`), and do not fetch or decide how to obtain data.
- Single level of abstraction (SLA) at **every** container level, recursively — not just Content layers: a container's direct children are either all leaf composables or all named components (`Spacer`/dividers exempt). If any sibling is named, extract the remaining leaves into named components too; an all-leaf container needs no extraction. Name components for their purpose, not what they display.
- Split component files by cohesion, never declaration count: one section file holds its whole SLA tree as `private` sub-components plus its `@Preview` (`@file:Suppress("TooManyFunctions")` is the intended trade-off, not a smell). A separate file only for pieces genuinely shared across sections (`ChromeIconButton`, `EditorPreviewIsland`) or an independently-evolving unit.
- Padding: the parent container sets internal padding to secure spacing — do not add padding to child components as if it were a margin.

## IDE Design Rules (Islands Dark / Light)

`app/feature/profile` mimics the Android Studio New UI (Islands Dark and Light; the active theme is hoisted state in `app:webApp`'s `App`, applied via `KeiTheme(isDark)` and toggled through the `onToggleTheme` callback threaded down to `TitleBar`). When touching its UI:

- Colors come from `KeiTheme.colors.*` in `@Composable` code; non-composable code (`deskBackground`, draw lambdas, style helpers) takes an explicit `KeiColorScheme` parameter resolved from `KeiTheme.colors` at the composable call site — there is no global scheme instance. Theme-dependent resource/text branches read `KeiTheme.colors.isDark`. Shapes/radii from `KeiTheme.shapes.*`; gaps/widths from `ProfileDimensions`. Never hardcode a new color — add a field to `KeiColorScheme` instead. The syntax highlighter (`highlightKotlin`/`codeLinesFor`) is a pure function taking a `KeiColorScheme` parameter.
- The desk (`KeiTheme.colors.desk`) is the window background itself; both themes draw a top-left glow (`Modifier.deskBackground()`): a horizontal desk→`deskGlow`→desk ramp centered under the project chip, fading back to `desk` within 300dp of the top edge, as in real AS Islands (the tint comes from the IDE's per-project color — currently warm gray, not blue). Title bar, status bar, and tool rails sit transparently on it.
- Panels are floating rounded "islands" on the desk: the project tree uses the darker `islandDark`, editor/preview use `island`, with no island borders. Popups are a third surface — `popup` with a `popupBorder` outline and `popupBorder` dividers, matching Islands' `Popup.background` / `Popup.borderColor`. It equals `island` in light but not in dark, so never substitute one for the other.
- Selection: copy the real Android Studio surface by surface, never generalizing from one to another. Today grey `selectionPill` for tree rows and view-mode toggles; grey `selectionPill` + `muted` border for the TODO window's selected scope tab; blue `tabSelected` + `tabSelectedBorder` for the selected editor tab and Search Everywhere's tab chips; the brighter `popupSelection` (no border) for Search Everywhere's result rows; `focusBorder` for an input's outline, drawn unconditionally where AS keeps that input permanently focused (Search Everywhere's field). A surface outside this list extends it in the same change, after checking the real IDE. `androidGreen` is content-side only — **never** a chrome selection state.
- The editor code pane (left) and the Preview pane (right) must always show the same data — update both together. The one sanctioned divergence: while the edited `ProfileScreen.kt` fails to parse, the preview keeps rendering the last successfully parsed data and shows the Out-of-date status.
- Typography: base `TextStyle`s live on `KeiTheme.typography` — `code` / `chrome` (JetBrains Mono), `cardJp` (Noto Sans JP), `githubJp` (Zen Kaku Gothic New), `mono` (splash); adjust per-use size/weight/color via `.copy(...)`.
- Decorative chrome that mimics AS but is not clickable is dimmed with `Modifier.alpha(KeiTheme.colors.nonClickableAlpha)`; genuinely clickable elements stay at full emphasis.
- Hover feedback uses `chip` on islands and the translucent `deskChip` on the desk; keep transitions subtle. No always-running animations except the editor caret blink and loading indicators while their data is in flight (skeleton shimmer, Preview building spinner/bar, contributions pulse) — all of which go static under `prefersReducedMotion()`.
- Syntax highlight colors (`KeiTheme.colors.syntax*`): dark measured from a real AS screenshot, light from the IntelliJ Light default scheme.
- Icons are converted from official sources only (IntelliJ Platform expUI SVGs from intellij-community, official Android Studio / brand artwork) — hand-drawn approximations are not accepted.

## Preview

Add a plain `@Preview` wrapped in `KeiTheme { ... }` at the bottom of each component file. Full rules (annotation, naming, fixtures, Android-target requirement): `.claude/rules/preview.md` (canonical home).

## Compose Pitfalls (verified in this codebase)

- Hit testing prunes a child's pointer regions outside the parent's bounds — an interactive child pushed outside its parent (negative offset, `requiredWidth` overflow) silently receives no pointer events there; reserve real layout width instead.
- Dialog content that fills the viewport (`fillMaxSize` / full-screen padding) leaves no "outside", so outside-click dismissal (`InlineDialogSceneStrategy`'s `boundsInParent` comparison) stops working — keep dismissable dialog content smaller than the viewport or handle dismissal explicitly.
- Deferred lambdas (`Modifier.offset { }` / `Modifier.layout { }`) run outside the composable body and bypass its early-return guards — re-guard state-dependent computations (e.g. divisions) inside the lambda.

## Browser Smoke Test

After a user-visible UI change, verify runtime behavior in a browser — compilation proves nothing; never claim browser verification from compilation alone.

Prefer Playwright wherever it covers the change: the E2E suite (`.claude/rules/ui-testing.md` — canonical home for locating and driving the canvas) against a served distribution, or a headless Playwright session against the dev server. Check both sides of the 900dp breakpoint by setting the viewport size — resizing a real Chrome window is not automatable in this environment.

What to confirm:

1. Splash completes and transitions to Profile
2. Desktop and mobile layouts on both sides of the 900dp breakpoint
3. The interactions and links the change touched
4. Editor code pane and Preview pane still show the same data

Fall back to manual dev-server verification (`./gradlew :app:webApp:wasmJsBrowserDevelopmentRun` → http://localhost:8080) only for what Playwright cannot cover, minding two verified traps: editing sources mid-verification live-reloads the app back to Splash (redo the checks after any edit), and a backgrounded tab throttles frames — re-screenshot before trusting a broken-looking state. Ask the user to verify manually only when neither lane can confirm the behavior.

Report which checks were performed and call out anything left unverified.
