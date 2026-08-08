# Checklist — Dialog destination

Complete `screen.md` first. These are the dialog-specific outcomes; implementation details are
canonical in `.claude/rules/navigation.md` and the dialog templates.

- [ ] The destination renders above the previous entry instead of replacing it full-window.
- [ ] The dialog owns only its panel; the shared scene strategy owns overlay positioning,
      semantics, Escape, and outside-click dismissal.
- [ ] Dismissing by every supported path reveals an operable underlying entry in the a11y mirror.
- [ ] Breakpoint state exists only when the dialog's behavior genuinely depends on it.
- [ ] When the dialog returns a result, the sender closes after sending it and the receiver handles
      it through an existing Intent.
- [ ] Any non-Compose opening path prevents duplicate back-stack entries.
