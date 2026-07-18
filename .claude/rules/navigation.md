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
| `navigation/{Feature}NavigationRoute.kt` | `@Serializable data object Xxx : NavKey` definition(s) **plus** the colocated `fun NavBackStack<NavKey>.navigateXxx() = add(Xxx)` extension — KEI does **not** use a separate `NavigationExtensions` file. A start destination nothing navigates to (e.g. `Splash`) omits the extension |
| `navigation/{Feature}Navigation.kt` | `EntryProviderScope<NavKey>.{feature}Entries()` registering the feature's destinations; the `ViewModel` is obtained **inside** the `entry<...> { }` block via `metroViewModel()` — never constructed manually or passed in |

Current examples: `app/feature/splash` (`Splash`, `splashEntries(navigateProfile: () -> Unit)`) and `app/feature/profile` (`Profile`, `profileEntries()`).

## AppNavDisplay

`AppNavDisplay` calls every feature's entries function into one `entryProvider`; the back stack is flat, so back handling is a single guarded `if (backStack.size > 1) backStack.removeLastOrNull()`. Transitions are set globally via `transitionSpec` / `popTransitionSpec` (see `AppNavDisplay.kt` for the exact durations and rationale); there is no per-entry metadata.

## Cross-Feature Navigation

Passed as a plain lambda parameter on `{feature}Entries()` (e.g. `splashEntries(navigateProfile = backStack::navigateProfile)`) — a feature never depends on another feature module or a shared navigation module.

## CRITICAL: Register Every NavKey in the SerializersModule

wasmJs has no reflection, so the open-polymorphic `NavKey` back stack cannot restore itself automatically. `AppNavDisplay`'s `navKeySavedStateConfiguration` registers every `NavKey` subclass explicitly: `polymorphic(NavKey::class) { subclass(Xxx::class, Xxx.serializer()) }`. **Forgetting to add a new `NavKey` here is the #1 pitfall when adding a destination** — the app compiles but back-stack save/restore silently breaks (or crashes) on that destination.

## Adding a New Destination

1. Add the `NavKey` (and its `navigate{Destination}` extension) to `{Feature}NavigationRoute.kt` in the owning feature module.
2. Register the entry in `{Feature}Navigation.kt`'s `{feature}Entries()`, obtaining the `ViewModel` via `metroViewModel()` inside the `entry<...> { }` block.
3. Register the new `NavKey` subclass in `navKeySavedStateConfiguration` — do not skip this (CRITICAL above).
4. For a new feature module, wire its `{feature}Entries()` into `AppNavDisplay`'s `entryProvider { ... }`, passing any cross-feature navigation lambdas.

## Dialogs, BottomSheets, ResultEventBus — Not Used

KEI has no Dialog/BottomSheet destinations and no `ResultEventBus`. The license sheet
(`LicenseSheetOverlay`, `app/feature/profile/.../component/licensecard/LicenseSheet.kt`) is a
plain in-card overlay drawn inside the license preview card and driven by
`ProfileState.selectedLicense` — no M3 `ModalBottomSheet`, no `NavKey`, nothing registered in
this file's patterns. If a Dialog/BottomSheet destination is ever needed, define and document a
project-specific pattern with the user before implementation.

See also: `.claude/rules/mvi-architecture.md` for how `Effect`s (e.g. `SplashEffect.NavigateProfile`) trigger navigation callbacks, and `.claude/rules/ui-implementation.md` for where navigation fits in the screen structure.
