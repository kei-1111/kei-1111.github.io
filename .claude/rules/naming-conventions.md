---
paths:
  - "app/feature/**/*.kt"
  - "app/core/designsystem/**/*.kt"
  - "app/core/domain/**/*.kt"
  - "test/**/*.kt"
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

## testTag

`Modifier.testTag(...)` becomes the DOM `id` verbatim (CMP applies no sanitization), so the value
must be a valid, document-unique `id`: kebab-case `feature-component-role[-key]`, ASCII
letters/digits and `-` only. The feature segment is mandatory — Navigation 3 mounts both
destinations during a transition animation, so ids must be unique across screens, not just within
one. Repeated/list elements need a stable key (prefer an entity id over a positional index — an
index shifts on sort/filter). Tag only elements a test actually drives.
The string is defined once in `test/tags`, inside `TestTags`'s per-feature nested object: a `const`
for static tags (e.g. `TestTags.Profile.TITLE_BAR_THEME_TOGGLE = "profile-title-bar-theme-toggle"`),
a function in the same object for keyed tags (e.g. `fun projectTreeItem(id: String) =
"profile-project-tree-item-$id"`). Both the Composable's `Modifier.testTag(...)` and the Playwright
locator in `test/e2e` reference it — never inline the literal on either side.
How Playwright interacts with these elements: `.claude/rules/ui-testing.md` (canonical home).

## UseCase

`[present-tense verb][target]UseCase`, following the [Android official domain-layer guidelines](https://developer.android.com/topic/architecture/domain-layer). Only the `Get` verb exists today (`GetProfileUseCase`, `GetContributionsUseCase`, `GetLicensesUseCase` — return `Flow<T>`, wrapped with `.asResult()` in the ViewModel); future verbs follow the same convention. Binding/layering rules: `.claude/rules/usecase.md`.

## Packages

| Module kind | Pattern | Real example |
|-------------|---------|---------------|
| `app/feature/<name>` screen | `io.github.kei_1111.app.feature.<name>.destination.<name>...` | `io.github.kei_1111.app.feature.profile.destination.profile` |
| `app/core/<module>` | `io.github.kei_1111.app.core.<module>...` | `io.github.kei_1111.app.core.domain.usecase`, `io.github.kei_1111.app.core.mvi` |
| `shared/model` | `io.github.kei_1111.shared.model...` | `io.github.kei_1111.shared.model` |
| `server` | `io.github.kei_1111.server.<layer>...` | `io.github.kei_1111.server.routing`, `io.github.kei_1111.server.service`, `io.github.kei_1111.server.client` |
| `test/<module>` | `io.github.kei_1111.test.<module>...` | `io.github.kei_1111.test.tags`, `io.github.kei_1111.test.e2e` |

`destination/<name>/` directory names are lowercase single words (`profile`, `splash`), matching the screen name.

## Text Content

- Translatable UI strings (today only a11y `contentDescription`s) live in Compose Multiplatform string resources: `app/feature/<name>/src/commonMain/composeResources/values/strings.xml` (English, the fallback) + `values-ja/strings.xml` (Japanese), read via `stringResource(Res.string.*)`. Runtime language switching is driven by `KeiLanguageController` + `KeiLanguageResourceEnvironment` (designsystem), which override `LocalComposeEnvironment`; the `language_toggle` string is deliberately locale-crossed (it announces the language the press switches to).
- IDE-chrome labels ("Project", "PINNED", status-bar items, the splash build log, the fake project tree, and the code-mimicry pane's fixed code) stay English in both languages by design — do not externalize or translate them.
- Profile content is bilingual data, not resources: Japanese-bearing fields use `LocalizedText(ja, en)` (`shared/model`), resolved client-side via `LocalizedText.forLanguage(KeiLanguageController.language)`. The source of truth is the server's `server/.../content/ProfileContent.kt` (`DefaultGitHubProfile`), with duplicates at `app/core/data/.../profile/FallbackProfile.kt` and `app/feature/profile/.../preview/ProfilePreviewFixtures.kt` — edit all three together. Japanese literals remain allowed directly in content data.
