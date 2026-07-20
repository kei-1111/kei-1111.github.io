# Checklist — Dialog destination (dialog / palette)

A dialog is a `NavKey` on the same flat back stack as a Screen, drawn **above** the entry beneath
it (which stays composed). Everything in `screen.md` applies except the sections restated below;
read that file first and use this one for the differences.

Reference implementation: `app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/searcheverywhere/`
and its entry in `ProfileNavigation.kt`.

## Files — differences from a Screen

- [ ] NO `content/{Name}DesktopContent.kt` / `{Name}MobileContent.kt` — a dialog has no
      breakpoint branch and no Desktop/Mobile split
- [ ] `{Name}DialogRoot.kt` / `{Name}Dialog.kt` use presentation-based naming
- [ ] `DialogSceneStrategy` owns the window and scrim; `{Name}Dialog.kt` owns only the panel,
      sized from `BoxWithConstraints` constraints
- [ ] `{Name}Intent` includes a dismiss intent whose Effect navigates back; Esc uses this path,
      while outside-click dismissal is handled by `DialogSceneStrategy`
- [ ] No `UpdateLayout` / `currentLayout` unless the dialog genuinely stores per-breakpoint state
      (a centered panel that sizes itself from constraints does not)

## Scene wiring — MANDATORY, and invisible to the compiler

- [ ] `AppNavDisplay` includes the built-in `DialogSceneStrategy` in `sceneStrategies`
- [ ] `entry<{Name}>(metadata = dialogTransition())` in `{Feature}Navigation.kt`; use
      `DialogProperties` only when its defaults need changing
- [ ] Verified **visually in a browser** that it floats over the previous entry — skipping either
      metadata or the strategy compiles cleanly and silently renders full-window

## Result hand-back (when the dialog returns data)

- [ ] A dedicated result type is declared beside the producing NavKey in
      `navigation/{Feature}NavigationRoute.kt`; `ResultEventBus` keys it by reified `typeOf<T>()`
- [ ] Sender: an Effect whose DialogRoot calls `sendResult(...)` and then navigates back
- [ ] Receiver: `ResultEffect<ResultType>(LocalResultEventBus.current) { ... }` inside the receiving
      `entry<>` block, dispatching an **existing** Intent —
      no second copy of a reducer that already exists
- [ ] The bus comes from `LocalResultEventBus`, not Metro injection

## Dismissal and keyboard

- [ ] Esc dismisses; when the dialog owns a text field, that is handled in its `onPreviewKeyEvent`
      alongside the other keys rather than in a separate focus-dependent handler
- [ ] Outside-click dismissal uses `DialogProperties`; no feature-owned scrim is added
- [ ] If the dialog is opened by a global gesture handled outside Compose (a browser-level key
      listener), the opening path is guarded so it only fires on the intended destination — see
      `AppNavDisplay`'s check that the back stack top is the expected entry

## UI rules

- [ ] Colors/typography/shapes only from `KeiTheme.*`
- [ ] Panel dimensions that are structural (width cap, height fraction, row height, header height)
      live in the destination's `theme/{Name}Dimensions.kt`, not inline under
      `@file:Suppress("MagicNumber")`; a token shared by two destinations moves up to the
      feature-level `theme/`
- [ ] Selection colors follow the project rule in the UI implementation guide — when mirroring a
      real Android Studio surface requires deviating, update that rule in the same change instead
      of leaving the code and the rule disagreeing
