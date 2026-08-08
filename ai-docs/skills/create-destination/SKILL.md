---
name: create-destination
description: "Add a new destination to a kei-1111.github.io feature module (app/feature/*) — procedures and templates following the project's Navigation 3 + MVI (MviViewModel) + Metro DI patterns. Use when the user asks to add a new screen / destination / dialog / palette, create a XxxScreen, add a NavKey, create a new feature module, or wire entries into AppNavDisplay. Destinations come in two kinds: full-window Screens, and dialog destinations drawn above the previous entry via InlineDialogSceneStrategy."
---

# create-destination skill

A runbook for adding a new destination consistently following KEI's Navigation 3 + MVI + Metro
patterns. Every destination is a `NavKey` on the single flat back stack and renders either as a
full-window Screen or as a dialog destination above the entry beneath it.

## Scope

**In scope**: any destination reached through the back stack — in an existing feature module or a
brand-new `app/feature/*` module. Choose full-window Screen or dialog destination using
`.claude/rules/navigation.md`; UI layering is canonical in `.claude/rules/ui-implementation.md`.
Reference dialog: `SearchEverywhere`.

**Out of scope**: in-place UI that is not reached through the back stack — e.g. the license sheet
(`LicenseSheetOverlay`) drawn inside its own card and driven by `ProfileState.selectedLicense`.
That is plain state-driven rendering with no `NavKey`, and it stays that way.

## Prerequisites — confirm before implementation

Confirm with the user if anything is ambiguous:

1. **Destination name (PascalCase)** — e.g. `Works`. Used bare for the NavKey and all MVI class
   names (`WorksViewModel`, not `WorksScreenViewModel`); only the Composable takes the `Screen` suffix.
2. **Destination kind** — a full-window Screen, or a dialog floating over the entry beneath it?
   A dialog keeps that entry composed underneath.
3. **Target feature module** — an existing `app/feature/*`, or a new module (extra scaffolding step).
   Keep the feature and destination names conceptually distinct even when they coincide; one
   feature may host multiple destinations under `destination/<name>/`.
4. **One-shot effects?** — identify the concrete navigation, URL-opening, or other variants needed
   by the destination.
5. **Data loading?** — identify the required domain inputs and existing UseCases; data boundaries
   are canonical in `.claude/rules/data-layer.md`.
6. **Who navigates here?** — identify the opening entry, or confirm that this is a start
   destination.
7. **Returning a result?** — identify the payload and receiving entry when applicable; the
   transport is canonical in `.claude/rules/navigation.md`.

## Workflow

### Phase 1 — Read the rules

Read the applicable `.claude/rules/*.md` (run `scripts/list_matching_rules.sh` on the files
you'll touch), `docs/ArchitectureOverview.md`, and `docs/ModuleOverview.md`. If a template has
drifted from them or from the current code, the code wins.

### Phase 2 — Read the reference implementations

- `app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/` — data-bearing
  screen and dialog patterns
- `app/feature/splash/src/commonMain/kotlin/io/github/kei_1111/app/feature/splash/` — minimal screen
  and cross-feature navigation pattern
- `app/webApp/src/commonMain/kotlin/io/github/kei_1111/app/navigation/AppNavDisplay.kt` — app-level
  navigation wiring

### Phase 3 — New feature module only (skip for an existing module)

Follow `.claude/rules/gradle.md` — Module Wiring, copying the nearest feature module rather than
inventing new configuration.

### Phase 4 — Generate files from templates

Templates live in `references/templates/`. Placeholders:

| Placeholder | Meaning | Example |
|---|---|---|
| `{{Name}}` | PascalCase destination name (NavKey, MVI classes, Screen prefix) | `Works` |
| `{{name}}` | lowercase destination directory / package segment | `works` |
| `{{feature}}` | lowercase feature module name (Gradle path, package, entries function) | `works` |
| `{{Feature}}` | PascalCase feature name (navigation file names only) | `Works` |

Base path `KOTLIN = app/feature/{{feature}}/src/commonMain/kotlin/io/github/kei_1111/app/feature/{{feature}}`:

| Template | Target |
|---|---|
| `NavigationRoute.kt.template` | `KOTLIN/navigation/{{Feature}}NavigationRoute.kt` (or add to the existing file) |
| `NavigationExtensions.kt.template` | `KOTLIN/navigation/{{Feature}}NavigationExtensions.kt` (omit when no navigation extension is needed) |
| `Navigation.kt.template` | `KOTLIN/navigation/{{Feature}}Navigation.kt` — Screen kind (or add the entry to the existing `{{feature}}Entries()`) |
| `DialogNavigation.kt.template` | `KOTLIN/navigation/{{Feature}}Navigation.kt` — Dialog kind (or add the entry to the existing `{{feature}}Entries()`) |
| `ScreenRoot.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ScreenRoot.kt` |
| `Screen.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Screen.kt` |
| `DialogRoot.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}DialogRoot.kt` — Dialog kind only |
| `Dialog.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Dialog.kt` — Dialog kind only |
| `DesktopContent.kt.template` | `KOTLIN/destination/{{name}}/content/{{Name}}DesktopContent.kt` — Screen kind only |
| `MobileContent.kt.template` | `KOTLIN/destination/{{name}}/content/{{Name}}MobileContent.kt` — Screen kind only |
| `ViewModel.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ViewModel.kt` |
| `ViewModelState.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}ViewModelState.kt` |
| `State.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}State.kt` |
| `Intent.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Intent.kt` |
| `Effect.kt.template` | `KOTLIN/destination/{{name}}/{{Name}}Effect.kt` |

Add only the supporting files required by the chosen behavior. Directory placement is canonical in
`.claude/rules/ui-implementation.md` — `destination/<name>/` Directory Layout; preview fixtures,
when needed, follow `.claude/rules/preview.md` and `ProfilePreviewFixtures.kt`.

The Screen templates derive the breakpoint without storing it. Only when UI state must reset or
otherwise change at the breakpoint, add the complete `UpdateLayout` path by following the current
Profile destination across Screen, Intent, ViewModel, and ViewModelState. The templates retain the
project's nullable Effect lifecycle fields but omit `MviEffect` from the Root until Prerequisite #4
identifies a real Effect variant; add the handler from the closest current Root only then. If the
screen has no UI-originated intents, remove the `onIntent` parameter chain consistently by following
Splash.

**Dialog kind** — use the Dialog templates instead of the Screen/Content templates, then follow
`.claude/rules/navigation.md` — Dialog Destinations and Cross-Destination Results. Reference:
`destination/searcheverywhere/`.

Templates are minimal skeletons. First generate a compilable destination contract with no real
behavior. Every `// PLACEHOLDER:` comment marks an insertion point and must be replaced or deleted
before handoff.

After that skeleton compiles, implement each testable behavior through the `tdd` skill before
writing its production logic. The behavior-free contract is the allowed bootstrap; pure rendering
continues without a test-first assertion. Pull concrete UseCase/model types, Intent/Effect variants,
and layout sections from the Phase 2 references while implementing; never keep an arbitrary
reference variant that the prerequisites do not require.

### Phase 5 — Complete navigation and app wiring

Follow `.claude/rules/navigation.md` — Adding a New Destination. The templates own the new-file
shape; when adding to an existing feature, merge the new destination into its existing navigation
files instead of replacing them. Use only the navigation and result paths confirmed in the
prerequisites.

Run the `update-docs` skill over the completed change before the checklist.

### Phase 6 — Checklist

Run through `references/checklists/screen.md` (Screen kind) or
`references/checklists/overlay.md` (Dialog kind) to spot misses.

### Phase 7 — Verification (completion criteria)

When the destination adds or changes testable logic, include the completed `tdd` cycle and its
module-derived test task. The remaining completion checks do not replace that test run.

Run every applicable change-type row from `.claude/rules/working-agreement.md` — Build And
Validation. Include the TDD result above and the browser verification required by that table; do
not substitute a feature-only compile for app-wiring validation.

## References

- `.claude/rules/*.md` (via `scripts/list_matching_rules.sh`) — project conventions and validation requirements
- `docs/ArchitectureOverview.md` — MVI, DI, data flow, and navigation
- `docs/ModuleOverview.md` — module responsibilities and dependencies
