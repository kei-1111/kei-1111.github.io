# Checklist — Screen destination

Use this only after completing the `create-destination` workflow. It records completion outcomes;
the implementation details stay canonical in the rules, goldens, and SKILL phases they point to.
For a dialog, also complete `overlay.md`.

## Scope and structure

- [ ] Every decision from the SKILL prerequisites is reflected in the destination; no speculative
      effect, data dependency, navigation path, or result path was added.
- [ ] New-module wiring is complete when applicable, and the module satisfies
      `.claude/rules/gradle.md` plus `scripts/check_gradle_conventions.sh`.
- [ ] Generated files follow `.claude/rules/ui-implementation.md` — `destination/<name>/`
      Directory Layout and `.claude/rules/navigation.md` — Per-Feature File Layout.
- [ ] No golden `PLACEHOLDER` remains, and no unused variant-specific import or parameter remains.
- [ ] `scripts/check_destination_isolation.sh` passes without promoting destination-local code for
      convenience.

## Behavior and boundaries

- [ ] State transitions and one-shot effects satisfy `.claude/rules/mvi-architecture.md`; each
      required behavior is observable through State or Effect.
- [ ] Data access satisfies `.claude/rules/usecase.md` and `.claude/rules/data-layer.md`; the
      feature boundary has not been weakened.
- [ ] The destination is reachable through the intended entry, survives back-stack save/restore,
      and returns a result when the prerequisites require one.
- [ ] UI structure, theme tokens, localization, and previews satisfy the applicable UI, naming,
      and preview rules returned by `scripts/list_matching_rules.sh`.

## Verification

- [ ] Testable logic completed the `tdd` workflow and its module-derived test task.
- [ ] Every Phase 7 check passes after any formatter rewrite has been reviewed.
- [ ] User-visible behavior was verified in the browser; compilation alone was not reported as
      behavioral verification.
