# Checklist — Dialog destination

Read `screen.md` first: everything there applies. This file lists only what differs for a dialog.
Reference: `destination/searcheverywhere/` and its entry in `ProfileNavigation.kt`.

- [ ] No `content/` split — `{Name}DialogRoot.kt` / `{Name}Dialog.kt` replace the ScreenRoot/Screen
      pair, and the Dialog owns only its panel (`InlineDialogSceneStrategy` supplies the full-window
      overlay, centering, and dialog semantics)
- [ ] No `UpdateLayout` / `currentLayout` unless the dialog really stores per-breakpoint state
- [ ] `entry<{Name}>(metadata = dialogTransition())` — omitting the metadata compiles and silently
      renders full-window, so confirm visually in a browser
- [ ] The Dialog does not use `fillMaxSize`, align itself, handle Escape, or implement an
      outside-click layer; `InlineDialogSceneStrategy` owns those behaviors
- [ ] After Escape and outside-click dismissal, the entry beneath is visible and operable through
      the a11y mirror in E2E
- [ ] Returning a result: type declared beside the producing `NavKey`, sender's Root calls
      `sendResult` then navigates back, receiver's `entry<>` block uses `ResultEffect<T>` to
      dispatch an **existing** Intent
- [ ] Opened by a listener outside Compose? The opening path checks the back stack top first
