---
paths:
  - "app/feature/**/*.kt"
  - "app/core/designsystem/**/*.kt"
  - "app/core/domain/**/*.kt"
---

# Naming Conventions

## Intent / Effect

Name based on **intent (what to do)**, not on operation (what was clicked). Operation-based names such as `OnSaveButtonClick` are prohibited.

| Category | Pattern | Real example |
|----------|---------|---------------|
| State update | `Update{Target}` | `UpdateLayout(layout)`, `UpdateSelectedPage(page)` |
| Toggle | `Toggle{Target}` | `ToggleTree(layout)` |
| Result reception | `Receive{Target}` | `ReceiveFontLoaded(font)` |
| Visibility notification | `Update{Target}Visibility` | `UpdatePageVisibility(isVisible)` |
| Open (Intent and matching Effect) | `Open{Target}` | `ProfileIntent.OpenUrl(url)` → `ProfileEffect.OpenUrl(url)` |
| Navigation (Effect only) | `Navigate{Destination}` | `SplashEffect.NavigateProfile` |
| Consume (fixed) | `ConsumeEffect` | every `XxxIntent` ends with `data object ConsumeEffect` |

Reference: `ProfileIntent.kt`, `SplashIntent.kt`.

## Composable

- Feature components (`destination/<name>/component/`) are purpose-named with no prefix: `TitleBar`, `ProjectTree`, `EditorPane`, `StatusBar`.
- Shared components in `app/core/designsystem` take the `Kei` prefix (`KeiXxx`) — convention for the future; none exist yet.
- Callbacks: `on + Action + Target` — `Click` for taps (`onClickPage: (EditorPage) -> Unit`), `Change` for value changes (`onChangeViewMode: (EditorViewMode) -> Unit`).
- Below the Content layer, components receive plain values and callbacks — **never** an `Intent`. The Content layer maps callbacks back to Intents (see `.claude/rules/ui-implementation.md` and `.claude/rules/mvi-architecture.md`).

## UseCase

`[present-tense verb][target]UseCase`, following the [Android official domain-layer guidelines](https://developer.android.com/topic/architecture/domain-layer). Only the `Get` verb exists today (`GetProfileUseCase`, `GetContributionsUseCase` — return `Flow<T>`, wrapped with `.asResult()` in the ViewModel); future verbs follow the same convention. Binding/layering rules: `.claude/rules/usecase.md`.

## Packages

| Module kind | Pattern | Real example |
|-------------|---------|---------------|
| `app/feature/<name>` screen | `io.github.kei_1111.app.feature.<name>.destination.<name>...` | `io.github.kei_1111.app.feature.profile.destination.profile` |
| `app/core/<module>` | `io.github.kei_1111.app.core.<module>...` | `io.github.kei_1111.app.core.domain.usecase`, `io.github.kei_1111.app.core.mvi` |
| `shared/model` | `io.github.kei_1111.shared.model...` | `io.github.kei_1111.shared.model` |
| `server` | `io.github.kei_1111.server.<layer>...` | `io.github.kei_1111.server.routing`, `io.github.kei_1111.server.service`, `io.github.kei_1111.server.client` |

`destination/<name>/` directory names are lowercase single words (`profile`, `splash`), matching the screen name.

## Text Content

No `strings.xml` — there are no Android resources at runtime (the Android target exists only for `@Preview` rendering). UI text is static Kotlin data: profile content's source of truth is the server's `server/.../content/ProfileContent.kt` (`DefaultGitHubProfile`), with a client-side fallback copy at `app/core/data/.../profile/FallbackProfile.kt` (`FallbackProfile.profile`) — edit both together. Japanese literals are allowed directly in content data and composables.
