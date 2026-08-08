<!-- Keep this file and the Japanese version ArchitectureOverview.md in sync when editing. -->
<p align="right"><sub><a href="ArchitectureOverview.md">🌐 日本語</a></sub></p>

## Architecture

- The client (`:app`) is a multi-module project combining Clean Architecture (`app:feature` → `app:core:domain` → `app:core:data` → `app:core:api` (HTTP) / `app:core:local` (persistence)) with MVI
- `app:feature` has no Gradle dependency on `app:core:data`; it always accesses data through `app:core:domain` UseCases
- Data is served by a self-built API server (`:server`, Ktor / Cloud Run); `:app` and `:server` share a JSON contract via `:shared:model`. The server composes live data from the GitHub GraphQL API with static content
- On fetch failure (offline, timeout, server down, running under Android Preview), the Flow throws, the ViewModel's `.asResult()` converts it into `Result.Error`, and the UI renders an error state with retry

## Data flow

```mermaid
flowchart LR
    UI["UI (ScreenRoot / Screen / Content / Component)"]
    VM["ViewModel (MviViewModel)"]
    UC["UseCase (app:core:domain)"]
    Repo["Repository (app:core:data)"]

    UI -->|Intent| VM
    VM -->|State| UI
    VM -->|Effect (carried in State)| UI
    VM -->|calls| UC
    UC -->|calls| Repo
    Repo -->|Flow| UC
    UC -->|Flow| VM
```

- **Intent** … input that carries user actions to the ViewModel
- **ViewModelState** … the ViewModel's internal state, including implementation details not exposed to the UI, such as `Result<T>`
- **State** … the rendering state exposed to the UI. Converted from `ViewModelState.toState()`, and carries Effect as well
- **Effect** … a one-time side effect executed by the UI. `MviEffect` calls `onConsume` after handling it, and ScreenRoot consumes it via a `ConsumeEffect` Intent

The canonical source for MVI implementation conventions is `.claude/rules/mvi-architecture.md`.

## DI (Metro)

- `app:webApp`'s `AppGraph` (`@DependencyGraph(scope = AppScope::class)`) is the DI root
- Repository/UseCase implementations are auto-bound simply by annotating an `internal class` with `@ContributesBinding(AppScope::class)` + `@SingleIn(AppScope::class)` + `@Inject`
- Values such as Dispatchers are supplied by `DispatcherBindings` (`app:core:common`), a `@BindingContainer` + `@ContributesTo(AppScope::class)`
- ViewModels are registered with `@ViewModelKey` + `@ContributesIntoMap` and retrieved via `metroViewModel()` inside a Navigation Entry

The canonical source for binding conventions (including exceptions for test seams) is `.claude/rules/data-layer.md`; ViewModel patterns are canonical in `.claude/rules/mvi-architecture.md`.

## Navigation (Navigation 3)

- Only `app:webApp`'s `AppNavDisplay` holds the single `NavDisplay` and back stack
- Each feature defines a `NavKey` and an `xxxEntries()` extension function, which `AppNavDisplay` registers collectively (file layout is canonical in `.claude/rules/navigation.md`)
- Since wasmJs does not support reflection, each feature contributes `SerializersModule` fragments via `@IntoSet`, aggregated into `AppGraph.navKeySerializers` and used to serialize/restore the back stack
- Dialogs are declared with `entry<X>(metadata = dialogTransition())`, and `:app:core:navigation`'s `InlineDialogSceneStrategy` renders them above the previous entry (dismiss behavior and a11y are owned by the strategy)
- One-shot results between destinations use `ResultEventBus`; the receiving `entry<>`'s `ResultEffect<T>` re-dispatches into an existing Intent
