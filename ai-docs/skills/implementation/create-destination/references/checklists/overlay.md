# Checklist — Dialog destination

Read `screen.md` first: everything there applies. This file lists only what differs for a dialog.
Reference: `destination/searcheverywhere/` and its entry in `ProfileNavigation.kt`.

- [ ] No `content/` split — `{Name}DialogRoot.kt` / `{Name}Dialog.kt` replace the ScreenRoot/Screen
      pair, and the Dialog owns only its panel (`DialogSceneStrategy` supplies the window and scrim)
- [ ] No `UpdateLayout` / `currentLayout` unless the dialog really stores per-breakpoint state
- [ ] `entry<{Name}>(metadata = dialogTransition())`, with `DialogProperties` only when the defaults
      need changing — omitting the metadata compiles and silently renders full-window, so confirm
      visually in a browser
- [ ] Esc dismisses through the same Intent as any other dismissal. Outside-click dismissal is
      `DialogProperties`, never a hand-rolled scrim — but a panel that fills the dialog window
      leaves no outside to click, so confirm in a browser which of the two the dialog actually has
- [ ] Returning a result: type declared beside the producing `NavKey`, sender's Root calls
      `sendResult` then navigates back, receiver's `entry<>` block uses `ResultEffect<T>` to
      dispatch an **existing** Intent
- [ ] Opened by a listener outside Compose? The opening path checks the back stack top first
