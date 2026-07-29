# AGENTS.md — app/

Rules for the wasm client (`app/*`). The root `AGENTS.md` (Working Agreement, validation, Git rules) still applies; this file adds the app-specific rules and points at their canonical homes in `.claude/rules/`.

## Architecture

Canonical detail: `.claude/rules/mvi-architecture.md` (MVI types, ViewModel pattern, `onIntent` policy, Effect handling) and `.claude/rules/ui-implementation.md` (screen layering, directory layout).

- Layering: `app:feature` → `app:core:domain` → `app:core:data`; a feature module has no Gradle dependency on `app:core:data` — a ViewModel only ever calls a UseCase, never a Repository (canonical: `.claude/rules/data-layer.md`).
- Screen structure: `XxxScreenRoot` → internal `XxxScreen` (branches on the `900.dp` breakpoint) → `content/` Desktop/Mobile Content → pure `component/*` (plain values + callbacks, never an `Intent`).
- MVI flow: `Intent` → `onIntent` updates `ViewModelState` → `toState()` derives `State`; one-shot side effects live as `State.effect` and are consumed exactly once via `MviEffect`, so every `XxxIntent` includes `ConsumeEffect`.

## Data, Domain, And Error Handling

Canonical detail: `.claude/rules/data-layer.md` (Repository shape, fetch & failure propagation), `.claude/rules/error-handling.md` (`Result<T>` boundary), `.claude/rules/usecase.md` (UseCase shape and Metro bindings).

- There is NO `Dispatchers.IO` on wasm — never introduce an `@IoDispatcher`; use the `DefaultDispatcher` qualifier from `app/core/common`.
- Fetch/parse failures propagate: the Repository `Flow` throws and the ViewModel-side `.asResult()` turns it into `Result.Error` — there is no client-side content fallback.
- Profile source content lives in the server's `ProfileContent.kt` (`DefaultGitHubProfile`), with a preview duplicate in `app/feature/profile`'s `ProfilePreviewFixtures.kt` — update both together.

## Navigation

Canonical detail: `.claude/rules/navigation.md` (route/entries file layout, dialog destinations, `ResultEventBus` results).

- Navigation 3: a single `NavDisplay` + back stack owned by `webApp`'s `AppNavDisplay`; cross-feature navigation is a plain lambda parameter on `xxxEntries()` — a feature never depends on another feature module.
- CRITICAL: contribute every new `NavKey` to its own `{Feature}NavigationRoute.kt`'s `SerializersModule` fragment (Metro `@IntoSet`, aggregated as `AppGraph.navKeySerializers`) — wasmJs has no reflection, so a missing registration compiles fine but silently breaks back-stack save/restore.
- Dialogs and command palettes are dialog destinations on the back stack (`DialogSceneStrategy`), not ad-hoc UI state.

## Compose UI

Canonical detail: `.claude/rules/ui-implementation.md` (theme/color usage, per-surface selection colors, IDE design rules, Compose pitfalls) and `.claude/rules/preview.md` (Preview conventions).

- Never hardcode a new color — add a field to `KeiColorScheme`. Selection colors copy the real Android Studio surface by surface; `androidGreen` is content-side only, never a chrome selection state.
- The editor code pane (left) and the Preview pane (right) must always show the same data — update both together.

## Naming

Canonical detail: `.claude/rules/naming-conventions.md` (Intent/Effect patterns, callbacks, UseCases, packages, testTag, text content).

- Every destination defines the 5-file MVI set plus `XxxScreenRoot` / `XxxScreen` (dialogs: `XxxDialogRoot` / `XxxDialog`) at the `destination/<name>/` top level; Content in `content/`, local models in `model/`, UI tokens in `theme/`.
- MUST: a destination never references a sibling destination — especially not its components (`scripts/check_destination_isolation.sh` enforces it). Promotion rules: `.claude/rules/ui-implementation.md`.
- Intent names are intent-based (`UpdateLayout`, `ToggleTree`), never operation-based (`OnSaveButtonClick` is prohibited).
- Translatable UI strings (a11y labels, small chrome captions) live in Compose string resources (`values/strings.xml` English fallback + `values-ja/strings.xml`) read via `stringResource`; long-form editor content (README / usage pages) stays per-language Kotlin data resolved via `KeiLanguage`, not string resources.

## Browser Smoke Test

For user-visible wasm UI changes, follow `.claude/rules/ui-implementation.md` — Browser Smoke Test (canonical home; Playwright-first, manual dev server as fallback). Report which checks were performed and call out anything left unverified.
