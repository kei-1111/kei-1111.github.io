---
paths:
  - "app/feature/**/src/commonMain/**/component/**/*.kt"
  - "app/feature/**/src/commonMain/**/content/**/*.kt"
  - "app/feature/**/src/commonMain/**/*Screen*.kt"
  - "app/feature/**/src/commonMain/**/*Dialog*.kt"
  - "app/feature/**/src/commonMain/**/preview/**"
  - "app/core/designsystem/**/*.kt"
---

# Preview Implementation Guide

## Rules

- Use the unified `androidx.compose.ui.tooling.preview.Preview` annotation directly in
  `commonMain`, always plain with **no parameters**.
- No shared preview infrastructure (`@ComponentPreviews` / `@ScreenPreviews` / `@PreviewWrapper`) — do not introduce it. Wrap content in `KeiTheme { ... }` by hand.
- A base preview is a `private` function named `{ComponentName}Preview`; additional states use
  `{ComponentName}{Variant}Preview`. Keep them at the bottom of the component's own file, with
  empty `{}` for callback parameters.
- A component whose layout needs bounded constraints (e.g. `verticalScroll` under
  `BoxWithConstraints`) gives its preview a fixed `Modifier.size(...)` box — Preview otherwise
  measures under infinite constraints. Use the current `ProfileDesktopContentPreview` and
  `PreviewPanePreview` as references.
- Copy the nearest current preview shape rather than a documentation snippet; callback signatures
  and theme wrappers remain executable in the component source.

## State for Screens/Content Previews

Screens and Desktop/Mobile Content that require a `State` build it from sample data in `preview/XxxPreviewFixtures.kt` — **never** a live `ViewModel`. `ProfilePreviewFixtures.kt` defines `PreviewProfile` / `PreviewContributionCalendar`; fixtures carry their own sample data because a feature module cannot depend on `app:core:data` (layering rule) and the real content lives only in the admin console's published objects. Component previews that only need a value (not a full `State`) pass the same fixtures directly.

## Rendering Requirements

Preview rendering is wired by the convention plugins and relies on the non-shipped Android target;
do not remove it. Its constraints are canonical in `.claude/rules/working-agreement.md` — Safety
And Maintenance, and its compile check in Build And Validation.
