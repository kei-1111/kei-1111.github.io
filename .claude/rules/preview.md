---
paths:
  - "app/feature/**/*.kt"
  - "app/core/designsystem/**/*.kt"
---

# Preview Implementation Guide

## Rules

- Use the unified `androidx.compose.ui.tooling.preview.Preview` annotation (CMP 1.10+, usable directly in `commonMain`), always plain with **no parameters**.
- No shared preview infrastructure (`@ComponentPreviews` / `@ScreenPreviews` / `@PreviewWrapper`) — do not introduce it. Wrap content in `KeiTheme { ... }` by hand.
- The preview is a `private` function named `{ComponentName}Preview` at the bottom of the component's own file, with empty `{}` for callback parameters.
- A component whose layout needs bounded constraints (e.g. `verticalScroll` under `BoxWithConstraints`) gives its preview a fixed `Modifier.size(...)` box — Preview otherwise measures under infinite constraints. See `ProfileDesktopContentPreview` (1280×800) and `PreviewPanePreview` (420×640).

```kt
@Preview
@Composable
private fun TitleBarPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(8.dp)) {
            TitleBar()
        }
    }
}
```

## State for Screens/Content Previews

Screens and Desktop/Mobile Content that require a `State` build it from sample data in `preview/XxxPreviewFixtures.kt` — **never** a live `ViewModel`. `ProfilePreviewFixtures.kt` defines `PreviewGitHubProfile` / `PreviewContributionCalendar`; fixtures duplicate real content because a feature module cannot depend on `app:core:data` (layering rule). Component previews that only need a value (not a full `State`) pass the same fixtures directly.

## Rendering Requirements

Preview rendering relies on the preview-only Android target from the `kei_1111.kmp.wasm` convention plugin (`android {}`, namespace auto-derived from the project path — see `KmpWasm.kt`); the `compose.ui.tooling` dependency is wired by `kei_1111.cmp`. Do not remove that target. Compile-check a module's previews without opening the IDE:

```bash
./gradlew :app:feature:profile:compileAndroidMain
```
