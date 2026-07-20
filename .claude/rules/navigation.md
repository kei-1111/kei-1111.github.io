---
paths:
  - "app/webApp/**/navigation/**/*.kt"
  - "app/feature/**/navigation/**/*.kt"
---

# Navigation Guide

Navigation 3 (`androidx.navigation3`): a single `NavDisplay` + single flat `NavBackStack`, owned by `AppNavDisplay` (`app/webApp/src/commonMain/kotlin/io/github/kei_1111/app/navigation/AppNavDisplay.kt`). This is the canonical home for KEI's navigation patterns.

## Per-Feature File Layout

| File | Role |
|---|---|
| `navigation/{Feature}NavigationRoute.kt` | `@Serializable data object Xxx : NavKey` definition(s), plus any result type produced by those destinations |
| `navigation/{Feature}NavigationExtensions.kt` | `fun NavBackStack<NavKey>.navigateXxx() = add(Xxx)` extensions. Omit when nothing navigates to the feature's destinations (e.g. `Splash`) |
| `navigation/{Feature}Navigation.kt` | `EntryProviderScope<NavKey>.{feature}Entries()` registering the feature's destinations; the `ViewModel` is obtained **inside** the `entry<...> { }` block via `metroViewModel()` — never constructed manually or passed in |

Current examples: `app/feature/splash` (`Splash`, `splashEntries(navigateProfile: () -> Unit)`) and
`app/feature/profile` (`Profile` / `SearchEverywhere`, `profileEntries(...)`).

## AppNavDisplay

`AppNavDisplay` calls every feature's entries function into one `entryProvider`; the back stack is flat, so back handling is a single guarded `if (backStack.size > 1) backStack.removeLastOrNull()`. Base transitions are set globally via `transitionSpec` / `popTransitionSpec`; dialog presentation is declared per entry through metadata.

## Cross-Feature Navigation

Passed as a plain lambda parameter on `{feature}Entries()` (e.g. `splashEntries(navigateProfile = backStack::navigateProfile)`) — a feature never depends on another feature module or a shared navigation module.

## CRITICAL: Register Every NavKey in the SerializersModule

wasmJs has no reflection, so the open-polymorphic `NavKey` back stack cannot restore itself automatically. `AppNavDisplay`'s `navKeySavedStateConfiguration` registers every `NavKey` subclass explicitly: `polymorphic(NavKey::class) { subclass(Xxx::class, Xxx.serializer()) }`. **Forgetting to add a new `NavKey` here is the #1 pitfall when adding a destination** — the app compiles but back-stack save/restore silently breaks (or crashes) on that destination.

## Adding a New Destination

1. Add the `NavKey` and any result type to `{Feature}NavigationRoute.kt`, and its `navigate{Destination}` extension to `{Feature}NavigationExtensions.kt`.
2. Register the entry in `{Feature}Navigation.kt`'s `{feature}Entries()`, obtaining the `ViewModel` via `metroViewModel()` inside the `entry<...> { }` block.
3. Register the new `NavKey` subclass in `navKeySavedStateConfiguration` — do not skip this (CRITICAL above).
4. For a new feature module, wire its `{feature}Entries()` into `AppNavDisplay`'s `entryProvider { ... }`, passing any cross-feature navigation lambdas.

## Dialog Destinations and Cross-Destination Results

Two destination kinds share the flat back stack: the full-window **Screen** and the **dialog** —
dialogs and command palettes are destinations, not ad-hoc UI state, whenever the user navigates to
and backs out of them. A dialog declares itself with `entry<X>(metadata = dialogTransition())` and
is rendered by Navigation 3's built-in `DialogSceneStrategy`, which owns the window and scrim;
the Dialog owns only its panel, layered DialogRoot → Dialog → Component with no Content split.
Omitting the metadata compiles and then renders full-window, so verify in a browser.
Reference: `SearchEverywhere` (`app/feature/profile/.../destination/searcheverywhere/`).

One-shot results travel over `ResultEventBus` (`app:core:navigation`), supplied by `AppNavDisplay`
through `LocalResultEventBus` rather than Metro, and keyed by reified `typeOf<T>()` with the result
type declared beside the producing `NavKey`. The sender's Root calls `sendResult` then navigates
back; the receiver's `entry<>` block uses `ResultEffect<T>` to dispatch an existing Intent, so no
reducer is duplicated. Navigation 3 1.2's `androidx.navigation3.runtime.result` supersedes this
hand-rolled bus once the KMP artifact is stable.

The license sheet (`LicenseSheetOverlay`) stays a plain in-card overlay driven by
`ProfileState.selectedLicense` — not a destination, no `NavKey`.

See also: `.claude/rules/mvi-architecture.md` for how `Effect`s (e.g. `SplashEffect.NavigateProfile`) trigger navigation callbacks, and `.claude/rules/ui-implementation.md` for where navigation fits in the screen structure.
