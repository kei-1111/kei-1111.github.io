---
paths: "feature/**/*.kt,core/designsystem/**/*.kt"
---

# UI Implementation Guide

This document defines UI implementation patterns for the kei-1111.github.io project.

---

## Screen Structure (Breakpoint-Branching Architecture)

This project has no ViewModel / UiState / Intent by design. Screens are pure renderings of static content defined in `:core:data`.

| Layer | Role | File |
|-------|------|------|
| public Screen | Measures screen width, branches by device type, holds page-selection state | `XxxScreen.kt` |
| Desktop/Mobile Content | Layout per form factor | `XxxDesktopContent.kt` / `XxxMobileContent.kt` |
| Component | Pure UI rendering | `component/*.kt` |

**Example**: `feature/profile/src/commonMain/kotlin/.../profile/ProfileScreen.kt`

- Breakpoint: below `900.dp` is Mobile (same IDE chrome as Desktop — TitleBar, tool rails, status bar — but the tree opens as an overlay from the ToolRail and the editor island defaults to PreviewOnly; Split stacks code above preview)
- UI state that must sync across components (e.g. selected `EditorPage`) is hoisted to the public Screen and passed down as value + callback (`selectedPage` / `onSelectPage`)

---

## Component Responsibilities

### Be a Pure View

1. UI rendering
2. Event notification via callbacks (`onSelectPage: (EditorPage) -> Unit`)
3. Reading display content from `:core:data` / `PortfolioContent` constants

### Responsibilities It Should NOT Have

- Holding sync-relevant state internally (hoist it to the Screen)
- Fetching or computing data (all content is static definitions)

---

## Single Level of Abstraction (SLA)

In Desktop/Mobile Content layers, **place only components at the same level of abstraction** (e.g. `TitleBar` / `ProjectTree` / `EditorPane` / `PreviewPane` / `StatusBar`).

Extracted components should express "what is this component for" rather than "what does it display".

---

## File Splitting

### Directory Structure

```
feature/xxx/src/commonMain/kotlin/.../xxx/
├── XxxScreen.kt              # public Screen (breakpoint branching)
├── XxxDesktopContent.kt      # Desktop layout
├── XxxMobileContent.kt       # Mobile layout
├── component/                # Section-level components
├── theme/                    # Feature-local dimensions/animations (optional)
└── navigation/               # Route definition + NavHostController extension
```

### What to Keep in the Same File

- `private` sub-components internal to a component
- The component's `@Preview` function (bottom of the file)

---

## Padding Design

**Do not add Padding to child components as if it were a margin**. The parent container sets internal Padding to secure spacing.

---

## IDE Design Rules (Islands Dark)

`feature/profile` mimics the Android Studio New UI (Islands Dark). When touching its UI:

- Use tokens from `IdeColors` (colors) and `IdeDimens` (radii, gaps, widths) — never hardcode new colors
- The desk (`IdeColors.Desk` #26282C) is the window background itself, with a blue glow at the top-left (`Modifier.deskBackground()` in IdeUi.kt). Title bar, status bar, and tool rails sit transparently on it
- Panels are floating rounded "islands" on the desk: the project tree uses the darker `IdeColors.IslandDark`, editor/preview use `IdeColors.Island`, with no island borders — matching real AS Islands Dark
- Tree rows and view-mode toggles use grey `IdeColors.SelectionPill` for selection; the selected editor tab uses the blue pill (`IdeColors.TabSelected` fill + `IdeColors.TabSelectedBorder` border), matching real AS Islands Dark. Android green (`IdeColors.AndroidGreen`) is reserved for content-side accents (primary button, brand tile). Never use green for chrome
- The editor code (left) and the Preview pane (right) must always show the same data — update both together
- Fonts: JetBrains Mono for code/IDE chrome (`ChromeTextStyle` / `CodeTextStyle`), Noto Sans JP for Japanese card text (`CardTextStyle`)
- Hover feedback uses `IdeColors.Chip` on islands and the translucent `IdeColors.DeskChip` on the desk/gradient; keep transitions subtle. No always-running animations except the editor caret blink
- Syntax highlight colors in `IdeColors` are measured from a real AS screenshot: named args cyan, numbers teal, composable calls green, enum entries magenta italic, type references plain

---

## Preview

When implementing a UI component, add a Preview in the same file:

1. Use the unified annotation `androidx.compose.ui.tooling.preview.Preview` (plain `@Preview`, no parameters)
2. Wrap the content in `AppTheme(darkTheme = true)` so the Islands Dark palette renders
3. Place it as a `private` function at the bottom of the component's file
4. Rendering relies on the preview-only Android target — do not remove `androidLibrary` from the `kei_1111.kmp.wasm` convention plugin
