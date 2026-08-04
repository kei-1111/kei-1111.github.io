# AGENTS.md — app/

Rules for the wasm client (`app/*`). The root `AGENTS.md` (Working Agreement, validation, Git rules) still applies; this file adds the app-specific rules and points at their canonical homes in `.claude/rules/`.

## Architecture

Canonical detail: `.claude/rules/mvi-architecture.md` and `.claude/rules/ui-implementation.md`.

- Layering: `app:feature` → `app:core:domain` → `app:core:data` → `app:core:api` (HTTP) / `app:core:local` (persistence); a feature module has no Gradle dependency on `app:core:data` — a ViewModel only ever calls a UseCase, never a Repository (canonical: `.claude/rules/data-layer.md`).
- MVI flow: `Intent` → `onIntent` updates `ViewModelState` → `toState()` derives `State`; one-shot side effects live as `State.effect` and are consumed exactly once via `MviEffect`, so every `XxxIntent` includes `ConsumeEffect`.

## Data, Domain, And Error Handling

Canonical detail: `.claude/rules/data-layer.md`, `.claude/rules/error-handling.md`, and
`.claude/rules/usecase.md`. Testing: the Unit Testing And TDD section below.

- There is NO `Dispatchers.IO` on wasm — never introduce an `@IoDispatcher`; use the `DefaultDispatcher` qualifier from `app/core/common`.
- Fetch/parse failures propagate: the Repository `Flow` throws and the ViewModel-side `.asResult()` turns it into `Result.Error` — there is no client-side content fallback.
- Profile content placement and synchronization are canonical in
  `.claude/rules/naming-conventions.md` — Text Content.

## Navigation

Canonical detail: `.claude/rules/navigation.md`.

- Navigation 3: a single `NavDisplay` + back stack owned by `webApp`'s `AppNavDisplay`; cross-feature navigation is a plain lambda parameter on `xxxEntries()` — a feature never depends on another feature module.
- CRITICAL: contribute every new `NavKey` to its own `{Feature}NavigationRoute.kt`'s `SerializersModule` fragment (Metro `@IntoSet`, aggregated as `AppGraph.navKeySerializers`) — wasmJs has no reflection, so a missing registration compiles fine but silently breaks back-stack save/restore.
- Dialogs and command palettes are dialog destinations on the back stack (`InlineDialogSceneStrategy`), not ad-hoc UI state.

## Compose UI

Canonical detail: `.claude/rules/ui-implementation.md` and `.claude/rules/preview.md`.

- Never hardcode a new color — add a field to `KeiColorScheme`. Selection colors copy the real Android Studio surface by surface; `androidGreen` is content-side only, never a chrome selection state.
- The editor code pane (left) and the Preview pane (right) must always show the same data — update both together.

## Naming

Canonical detail: `.claude/rules/naming-conventions.md`.

- Destination layout follows `.claude/rules/ui-implementation.md`; do not add unrelated files at
  `destination/<name>/`'s top level.
- MUST: a destination never references a sibling destination — especially not its components (`scripts/check_destination_isolation.sh` enforces it). Promotion rules: `.claude/rules/ui-implementation.md`.
- Intent names are intent-based (`UpdateLayout`, `ToggleTree`), never operation-based (`OnSaveButtonClick` is prohibited).
- Place translatable and long-form text according to `.claude/rules/naming-conventions.md` — Text
  Content; do not invent a third localization path.

## Unit Testing And TDD

Canonical detail: `.claude/rules/app-testing.md`, `.claude/rules/tdd.md`, and
`.claude/rules/mvi-testing.md`.

- Run the client task selected by `.claude/rules/working-agreement.md` — Build And Validation.
- Hand-written fakes only — no mocking framework; assert observable behavior, never internal calls.

## Browser Smoke Test

For user-visible wasm UI changes, follow `.claude/rules/ui-implementation.md` — Browser Smoke Test.
Report which checks were performed and call out anything left unverified.
