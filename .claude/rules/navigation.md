---
paths: "app/composeApp/**/navigation/**/*.kt,app/feature/**/navigation/**/*.kt"
---

# Navigation Guide

Type-safe navigation using a single `NavDisplay` with Navigation 3 (`androidx.navigation3`). This is the
canonical home for KEI's navigation patterns — see also the "Navigation Entry Pattern" summary in
`.claude/rules/ui-implementation.md`.

## Route Definition

Every destination is a `NavKey` defined in its feature module's `navigation` package. Each feature's
start destination is a first-class entry in the single flat back stack.

| Feature Module | File | NavKey |
|---|---|---|
| `feature/splash` | `SplashNavigationRoute.kt` | `Splash` (`@Serializable data object Splash : NavKey`) |
| `feature/profile` | `ProfileNavigationRoute.kt` | `Profile` (`@Serializable data object Profile : NavKey`) |

## Single NavDisplay Pattern

`AppNavDisplay` (in `composeApp`) owns the single `NavDisplay` and the single `NavBackStack`. Each
feature module exposes a `{feature}Entries` extension on `EntryProviderScope<NavKey>` that registers its
destinations; `AppNavDisplay` calls every feature's entries function into one `entryProvider`. The back
stack is flat, so back handling is a single guarded `if (backStack.size > 1) backStack.removeLastOrNull()`.

**File**: `composeApp/src/commonMain/kotlin/.../navigation/AppNavDisplay.kt`

```kt
entryProvider = entryProvider {
    splashEntries(navigateProfile = backStack::navigateProfile)
    profileEntries()
}
```

### Entries Functions

| Feature | Entries function | Registered NavKeys |
|---|---|---|
| `feature/splash` | `splashEntries(navigateProfile: () -> Unit)` | `Splash` (app start destination) |
| `feature/profile` | `profileEntries()` | `Profile` |

The `ViewModel` is obtained **inside** the entry via `metroViewModel()` — never constructed manually or
passed in from outside (see `feature/profile/src/commonMain/kotlin/.../navigation/ProfileNavigation.kt`).

## Navigation Extension Colocation

KEI does **not** use a separate `{Feature}NavigationExtensions.kt` file. The `navigate{Destination}`
extension on `NavBackStack<NavKey>` is colocated with the NavKey in `{Feature}NavigationRoute.kt`.

**Example**: `feature/profile/src/commonMain/kotlin/.../navigation/ProfileNavigationRoute.kt`

```kt
@Serializable
data object Profile : NavKey

fun NavBackStack<NavKey>.navigateProfile() = add(Profile)
```

| File | Role |
|---|---|
| `{Feature}NavigationRoute.kt` | NavKey definition(s) + `navigate{Destination}` extension(s) |
| `{Feature}Navigation.kt` | `{feature}Entries` function (entry definitions, `metroViewModel()` calls) |

## Cross-Feature Navigation

A feature that needs to trigger navigation into another feature takes a plain lambda parameter on its
`{feature}Entries()` function — it never depends on another feature module or a shared navigation module.
`splashEntries(navigateProfile: () -> Unit)` is the only current example; `AppNavDisplay` wires
`navigateProfile = backStack::navigateProfile` in.

## CRITICAL: Register Every NavKey in the SerializersModule

wasmJs has no reflection, so the open-polymorphic `NavKey` back stack cannot restore itself automatically.
`AppNavDisplay` builds an explicit `SavedStateConfiguration` registering every `NavKey` subclass:

```kt
private val navKeySavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Splash::class, Splash.serializer())
            subclass(Profile::class, Profile.serializer())
        }
    }
}
```

**Forgetting to add a new `NavKey` here is the #1 pitfall when adding a destination** — the app will
compile but back-stack save/restore will silently break (or crash) on that destination.

## Naming Conventions

| Type | Format | Example |
|---|---|---|
| Entries function | `[feature]Entries` | `splashEntries`, `profileEntries` |
| Navigation extension function | `navigate[Destination]` | `navigateProfile` |

## Animation

`AppNavDisplay` sets `transitionSpec` / `popTransitionSpec` globally on the single `NavDisplay` (fade
in/out via `tween`); there is currently no per-entry metadata. See `AppNavDisplay.kt` for the exact
durations and rationale (mirroring the old `NavGraph.kt` nav2 transitions).

## Dialogs, BottomSheets, ResultEventBus — Not Used

KEI has no Dialog/BottomSheet destinations and no `ResultEventBus`. If one is ever needed, define
and document a project-specific pattern with the user before implementation.

## Adding a New Destination

1. Add the `NavKey` (and its `navigate{Destination}` extension) to `{Feature}NavigationRoute.kt` in the
   owning feature module.
2. Register the entry in `{Feature}Navigation.kt`'s `{feature}Entries()` function, obtaining the
   `ViewModel` via `metroViewModel()` inside the `entry<...> { }` block.
3. Register the new `NavKey` subclass in `AppNavDisplay`'s `navKeySavedStateConfiguration` — do not skip
   this step (see CRITICAL section above).
4. Wire the feature's `{feature}Entries()` into `AppNavDisplay`'s `entryProvider { ... }`, passing any
   cross-feature navigation lambdas it needs.

See also: `.claude/rules/mvi-architecture.md` for how `Effect`s (e.g. `SplashEffect.NavigateProfile`)
trigger these navigation callbacks, and `.claude/rules/ui-implementation.md` for where navigation fits in
the screen structure.
