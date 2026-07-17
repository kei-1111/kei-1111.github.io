---
paths: "app/feature/**/*.kt,app/core/designsystem/**/*.kt"
---

# Preview Implementation Guide

## Preview Annotation

Use the unified `androidx.compose.ui.tooling.preview.Preview` annotation (CMP 1.10+), usable directly in `commonMain`. This project does **not** use shared `@ComponentPreviews` / `@ScreenPreviews` / `@PreviewWrapper` infrastructure — do not introduce it. Every preview is a plain `@Preview` with **no parameters**.

```kt
@Preview
@Composable
private fun TitleBarPreview() {
    KeiTheme {
        ...
    }
}
```

---

## Basic Pattern

1. Wrap the content in `KeiTheme { ... }` by hand — there is no shared wrapper Composable
2. Place it as a `private` function at the bottom of the same file as the component
3. Empty `{}` for callback parameters

### Naming

The preview function is named `{ComponentName}Preview` — matching the enclosing public/internal Composable's name:

| Component | Preview function |
|-----------|-------------------|
| `TitleBar` | `TitleBarPreview` |
| `ProjectTree` | `ProjectTreePreview` |
| `ToolRail` | `ToolRailPreview` |
| `PreviewPane` | `PreviewPanePreview` |
| `ProfileDesktopContent` | `ProfileDesktopContentPreview` |

**Example**: `feature/profile/src/commonMain/kotlin/.../destination/profile/component/TitleBar.kt`

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

A component whose layout needs unbounded constraints (e.g. `verticalScroll` under `BoxWithConstraints`) gives its preview a fixed `Modifier.size(...)` box, since Preview measures under infinite constraints otherwise — see `ProfileDesktopContentPreview` (`Modifier.size(width = 1280.dp, height = 800.dp)`) and `PreviewPanePreview` (`Modifier.size(width = 420.dp, height = 640.dp)`) in `feature/profile/src/commonMain/kotlin/.../destination/profile/{ProfileDesktopContent.kt,component/PreviewPane.kt}`.

---

## Building State for Screens/Content Previews

Screens and Desktop/Mobile Content that require a `State` build it from sample data in `preview/XxxPreviewFixtures.kt` — **never** a live `ViewModel`.

**Example**: `app/feature/profile/src/commonMain/kotlin/.../destination/profile/preview/ProfilePreviewFixtures.kt` defines `internal val PreviewGitHubProfile = GitHubProfile(...)` and `internal val PreviewContributionCalendar = ContributionCalendar(...)`. A feature module cannot depend on `app:core:data` (layering rule), so this fixture duplicates real profile content for Preview use only.

```kt
@Preview
@Composable
private fun ProfileDesktopContentPreview() {
    KeiTheme {
        Box(modifier = Modifier.size(width = 1280.dp, height = 800.dp)) {
            ProfileDesktopContent(
                state = ProfileState(profile = PreviewGitHubProfile),
                onIntent = {},
            )
        }
    }
}
```

Component-level previews that only need a `GitHubProfile`/`ContributionCalendar` value (not a full `State`) pass the same fixtures directly, e.g. `PreviewPanePreview` passes `profile = PreviewGitHubProfile, contributions = PreviewContributionCalendar`.

Note: `ContributionsRepository`'s `fetchText()` `androidMain` actual always returns `null` (`core/data/src/androidMain/kotlin/.../contributions/FetchText.android.kt`), so any contribution data reached through the real repository on the preview-only Android target always falls back to the static `FallbackContributions` snapshot — Previews sidestep this entirely by using `PreviewContributionCalendar` fixture data instead.

---

## Rendering Requirements

Preview rendering relies on the preview-only Android target provided by the `kei_1111.kmp.wasm` convention plugin (`android {}`, namespace auto-derived from the Gradle project path — see `build-logic/convention/src/main/kotlin/io/github/kei_1111/KmpWasm.kt`); the `compose.ui.tooling` dependency is wired by `kei_1111.cmp`. Do not remove that target.

Compile-check a module's previews without opening the IDE:

```bash
./gradlew :app:feature:profile:compileAndroidMain
```
