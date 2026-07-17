---
paths:
  - "feature/**/*.kt"
  - "core/designsystem/**/*.kt"
  - "core/domain/**/*.kt"
---

# Naming Conventions

This document defines naming conventions for the kei-1111.github.io project.

---

## Intent / Effect

### Basic Principle

Name based on **intent (what to do)**, not on operation (what was clicked). Operation-based names such as `OnSaveButtonClick` are prohibited.

| Category | Pattern | Real example |
|----------|---------|---------------|
| State update | `Update{Target}` | `UpdateLayout(layout)`, `UpdateSelectedPage(page)`, `UpdateViewMode(viewMode, layout)` |
| Toggle | `Toggle{Target}` | `ToggleTree(layout)` |
| Result reception | `Receive{Target}` | `ReceiveFontLoaded(font)` |
| Visibility notification | `Update{Target}Visibility` | `UpdatePageVisibility(isVisible)` |
| Open (Intent and matching Effect) | `Open{Target}` | `ProfileIntent.OpenUrl(url)` → `ProfileEffect.OpenUrl(url)` |
| Navigation (Effect only) | `Navigate{Destination}` | `SplashEffect.NavigateProfile` |
| Consume (fixed) | `ConsumeEffect` | every `XxxIntent` ends with `data object ConsumeEffect` |

**Example**: `feature/profile/src/commonMain/kotlin/.../destination/profile/ProfileIntent.kt`, `feature/splash/src/commonMain/kotlin/.../destination/splash/SplashIntent.kt`

---

## Composable

### Component Naming

| Location | Prefix | Example |
|----------|--------|---------|
| `feature/<name>/.../destination/<name>/component/` | none — purpose-named | `TitleBar`, `ProjectTree`, `EditorPane`, `PreviewPane`, `StatusBar`, `ToolRail`, `CodeContent`; `githubcard/` subpackage |
| `core/designsystem/.../component/` (convention for future shared components — none exist yet) | `Kei` | `KeiXxx` |

**Example**: `feature/profile/src/commonMain/kotlin/.../destination/profile/component/TitleBar.kt`

### Callback Naming

**Pattern**: `on + Action + Target`

| Action | Usage | Real example |
|--------|-------|---------------|
| `Click` | Tap/click | `onClickPage: (EditorPage) -> Unit`, `onClickToggleTree: () -> Unit`, `onClickUrl: (String) -> Unit`, `onClickFit` / `onClickZoomIn` / `onClickZoomOut` |
| `Change` | Value change | `onChangeViewMode: (EditorViewMode) -> Unit`, `onChangeEffectiveScale: (Float) -> Unit` |

Below the Content layer (`XxxDesktopContent` / `XxxMobileContent`), components receive plain values and callbacks — **never** an `Intent`. See `.claude/rules/ui-implementation.md` for the full layer breakdown and `.claude/rules/mvi-architecture.md` for how a callback maps back to an Intent at the Content layer, e.g.:

```kt
ProjectTree(
    selectedPage = state.selectedPage,
    onClickPage = { onIntent(ProfileIntent.UpdateSelectedPageFromTree(it, WindowLayout.Desktop)) },
)
```

---

## UseCase

### Format

```
[present tense verb] + [target] + UseCase
```

Follows [Android official guidelines](https://developer.android.com/topic/architecture/domain-layer). Only the `Get` verb exists in this project today; future UseCases (`Save`/`Update`/...) should follow the same Android-official verb conventions. Full binding/layering rules live in `.claude/rules/usecase.md`.

| Verb | Return type | Real example |
|------|-------------|---------------|
| `Get` | `Flow<T>` (wrap with `.asResult()` in the ViewModel) | `GetProfileUseCase`, `GetContributionsUseCase` |

**Example**: `core/domain/src/commonMain/kotlin/.../usecase/GetProfileUseCase.kt`

---

## Packages

| Module kind | Pattern | Real example |
|-------------|---------|---------------|
| `feature/<name>` screen | `io.github.kei_1111.feature.<name>.destination.<name>...` | `io.github.kei_1111.feature.profile.destination.profile` |
| `core/<module>` | `io.github.kei_1111.core.<module>...` | `io.github.kei_1111.core.domain.usecase`, `io.github.kei_1111.core.mvi` |

`destination/<name>/` directory names are lowercase single words (`profile`, `splash`), matching the screen name.

---

## Text content

There is no `strings.xml` in this project — no Android resources are used at runtime (the Android target exists only for `@Preview` rendering). UI text is static Kotlin data:

- Profile content lives in `core/data/src/commonMain/kotlin/.../repository/ProfileContent.kt` as `internal val DefaultGitHubProfile = GitHubProfile(...)`
- Japanese literals are allowed directly in content data and composables, e.g. `DefaultGitHubProfile`'s `name = "けい"` and `description = "自己紹介Webサイトのリポジトリ"`
