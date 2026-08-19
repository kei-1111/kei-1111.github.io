---
paths:
  - "template/**/*.kt"
  - "app/feature/**/*.kt"
  - "app/core/designsystem/**/*.kt"
  - "app/core/domain/**/*.kt"
  - "app/core/common/**/*.kt"
  - "app/core/ui/**/*.kt"
  - "app/core/utils/**/*.kt"
  - "app/webApp/**/*.kt"
  - "shared/model/**/*.kt"
  - "server/**/content/**/*.kt"
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

`Receive{Target}` is only for results the UI did not directly request (e.g. `ReceiveFontLoaded` from an environment callback). A result-driven Intent whose requested action is clear keeps its action-based name — `OpenPage`, not `ReceivePage`.

Reference: `ProfileIntent.kt`, `SplashIntent.kt`.

## State / ViewModelState

Which side holds what is canonical in `.claude/rules/mvi-architecture.md` — ViewModelState / State Split. Names follow from that split:

- `ViewModelState` members are named after the raw value they hold (`openBottomTool`, `worksSheetOpen`, `profileCodeError`); one that mirrors a source already shaped as a predicate keeps that name (`isDarkTheme`).
- `State` members are named as the predicate the UI reads. Default to `is{Target}{State}` (`isLogcatOpen`, `isSelectedPageReadOnly`, `isBuildFailed`); `has{Noun}` only when the target is a thing that may or may not exist (`hasProfileCodeError`) — the target's grammar picks the prefix, and a state of the subject is always `is`. Boolean-returning `State` member functions follow the same form (`isEditorPaneVisible(layout)`, `hasCodeError(page)`).
- Composable parameters keep Compose's own vocabulary with no prefix (`enabled`, `visible`, `active`, `locked`, `treeOpen`), so a call site reads `logcatOpen = state.isLogcatOpen`.

Reference: `ProfileState.kt`, `SplashState.kt`.

## Composable

- Feature components (`destination/<name>/component/`) are purpose-named with no prefix: `TitleBar`, `ProjectTree`, `EditorPane`, `StatusBar`.
- Shared components in `app/core/designsystem` take the `Kei` prefix (`KeiXxx`) and live in `component/` — e.g. `KeiIcon`, `KeiAsyncImage`.
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
a function in the same object for keyed tags (e.g. `fun projectTreeItem(key: String) =
"profile-project-tree-item-$key"`). Both the Composable's `Modifier.testTag(...)` and the Playwright
locator in `test/e2e` reference it — never inline the literal on either side.
How Playwright interacts with these elements: `.claude/rules/ui-testing.md` (canonical home).

## UseCase

`[present-tense verb][target]UseCase`, following the [Android official domain-layer guidelines](https://developer.android.com/topic/architecture/domain-layer). `Get` UseCases return `Flow<T>`, wrapped with `.asResult()` in the ViewModel; `Save` UseCases are `suspend` and return `Unit`. The current set is canonical in `app/core/domain/src/commonMain/.../usecase/`; further verbs follow the same convention. Binding/layering rules: `.claude/rules/usecase.md`.

## Packages

| Module kind | Pattern | Real example |
|-------------|---------|---------------|
| `template` | `io.github.kei_1111.template.navigation` / `io.github.kei_1111.template.destination.{screen\|dialog}` | `io.github.kei_1111.template.destination.screen` |
| `app/feature/<name>` screen | `io.github.kei_1111.app.feature.<name>.{destination.<name>|navigation|model}...` (`commonTest` may add `fake`) | `io.github.kei_1111.app.feature.profile.destination.profile` |
| `app/core/<module>` | `io.github.kei_1111.app.core.<module>...` | `io.github.kei_1111.app.core.domain.usecase`, `io.github.kei_1111.app.core.mvi` |
| `app/webApp` | `io.github.kei_1111.app...` | `io.github.kei_1111.app.navigation` |
| `shared/model` | `io.github.kei_1111.shared.model...` | `io.github.kei_1111.shared.model` |
| `server` | `io.github.kei_1111.server.<layer>...` | `io.github.kei_1111.server.routing`, `io.github.kei_1111.server.service`, `io.github.kei_1111.server.client` |
| `test/<module>` | `io.github.kei_1111.test.<module>...` | `io.github.kei_1111.test.tags`, `io.github.kei_1111.test.e2e` |

`destination/<name>/` directory names are lowercase single words (`profile`, `splash`), matching the screen name.

## Text Content

- Translatable UI strings (a11y `contentDescription`s and small visible chrome captions such as the license card subtitle / sheet close label) live in Compose Multiplatform string resources: `app/feature/<name>/src/commonMain/composeResources/values/strings.xml` (English, the fallback) + `values-ja/strings.xml` (Japanese), read via `stringResource(Res.string.*)`. Runtime language switching is driven by `LocalKeiLanguage` + `KeiLanguageResourceEnvironment` (designsystem), which override `LocalComposeEnvironment`; the `language_toggle` string is deliberately locale-crossed (it announces the language the press switches to).
- IDE-chrome labels ("Project", "PINNED", status-bar items, the splash build log, the fake project tree, and the code-mimicry pane's Kotlin scaffolding — the `profileCode` / `worksCode` / `licenseCode` templates) stay English in both languages by design — do not externalize or translate them.
- In-editor visitor guidance — the all-tabs-closed usage page (`usageCode`) — is bilingual, defined as per-language variants in code and resolved via `KeiLanguage`, not string resources (multi-line content fits poorly in `strings.xml`).
- Profile, works, and README content is bilingual data, not resources: Japanese-bearing fields use `LocalizedText(ja, en)` (`shared/model`), resolved client-side via `LocalizedText.forLanguage(LocalKeiLanguage.current)`; README resolves through `Readme.blocksFor(KeiLanguage)`. The content is authored in the admin console and published to GCS — the repository holds no copy, so there is nothing to keep in sync. The `*PreviewFixtures.kt` files carry sample data for `@Preview` only (a feature module cannot depend on `app:core:data`) and mirror nothing. Japanese literals remain allowed directly in content data.
