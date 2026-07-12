# Git Workflow

Project Git/GitHub conventions for kei-1111.github.io.

## Commit Message Convention

Follow [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/) and write the complete message in **English**.

### Format

```
<type>: <description>
<type>(scope): <description>
```

- **type**: Required. Indicates the kind of change (English, lowercase)
- **scope**: Optional. Scope of the change — a module-ish segment (English, lowercase). Observed scopes: `profile`, `splash`, `core`, `designsystem`, `app`, `utils`, `deps`
- **description**: Required. Concise imperative description of the change in English

### Types

| Type | Usage |
|------|-------|
| feat | New feature |
| fix | Bug fix |
| docs | Documentation only |
| refactor | Code change that is neither a fix nor a feature |
| perf | Performance improvement |
| build | Build system or external dependency changes |
| ci | CI configuration changes |
| chore | Other changes (deletions, version bumps, etc.) |

`test` also exists in Conventional Commits, but the repository currently has no tests, so it is unused in practice.

### Breaking Change

Indicate breaking changes with exclamation mark notation `feat!:` or footer `BREAKING CHANGE:`.

### Rules

- **Language**: write the entire message in English
- **Style**: use imperative mood without a trailing period
- **Content**: be specific and clear about what changed
- **Length**: one concise line

### Commit Granularity

Commit at a granularity that allows cherry-picking without issues.

- **Self-contained**: Each commit completes one logical change
- **Independent**: Does not depend on subsequent commits

### Examples

Real examples from the project log:

```
feat(profile): allow horizontal scrolling in ProjectTree
fix(profile): use the official note logo
refactor(splash): inline timeout handling into UpdatePageVisibility
chore(designsystem): remove unused color and duration tokens
docs: add Claude Code and UI implementation guidance
```

## Branch Naming Convention

### Format

```
<type>/#<issue-number>
```

The branch type mirrors the Type of the corresponding Issue.

### Types

- `feature/`: New feature (`[Feature]` Issues)
- `fix/`: Bug fix (`[Bug]` Issues)
- `refactor/`: Refactoring (`[Refactor]` Issues)
- `other/`: Documentation, research, performance, test, CI, chore, and other maintenance Issues

### Examples

```
feature/#18
fix/#20
refactor/#8
other/#32
```

## Issue Title Convention

### Format

```
[<Type>]: <title>
```

Write both the title and body in **English**, mirroring the headings of the matching Markdown template in `.github/ISSUE_TEMPLATE/`:

| Template | Title prefix | Body sections |
|---|---|---|
| `bug_report.md` | `[Bug]: ` | Summary / Steps to Reproduce / Expected / Actual Behavior |
| `feature_request.md` | `[Feature]: ` | Summary |
| `refactor.md` | `[Refactor]: ` | Summary / Scope / Expected Improvements |
| `docs.md` | `[Documentation]: ` | Summary / Target Documents |
| `research.md` | `[Research]: ` | Summary / Research Goal / Expected Output |
| `perf.md` | `[Performance]: ` | Summary / Current Issue / Expected Improvement |
| `test.md` | `[Test]: ` | Summary / Scope / Test Contents |
| `ci.md` | `[CI]: ` | Summary / Changes |
| `chore.md` | `[Chore]: ` | Summary / Changes |

### Rules

- **Clarity**: Make it immediately clear what needs to be done
- **Specificity**: Avoid abstract descriptions; be concrete
- **Conciseness**: Include only the context needed to understand and act on the Issue
- **Single responsibility**: Each Issue focuses on one responsibility

### Examples

Real examples from the project:

```
[Feature]: Add a new portfolio design
[Bug]: Use the official note link icon
[Refactor]: Remove UiConfig
[Documentation]: Add AI documentation
```

## Pull Request Convention

### Title

```
[<Type>]: <title>
```

- **Issue-linked**: Use the corresponding Issue title verbatim as the PR title
- **Consistency**: Link with the branch name (`<type>/#<issue-number>`)
- **Language**: English (same as the Issue title)

### Body

Follow `.github/PULL_REQUEST_TEMPLATE.md` and write the body in English:

- `## Summary` — always
- `## Related Issue` — always (link the Issue)
- `## Checklist` — always
- `## Cause and Fix` — bug fixes only
- `## UI Changes` — UI changes only (Before/After image table)

Keep the title and body concise. Include only the context needed to review the change, and avoid
repeating information already available in the related Issue or diff.

### Base branch

`main`. Every PR targets `main`.

## CI/CD

- **CI** (`.github/workflows/ci.yml`): every PR to `main` runs `./gradlew detekt` (JDK 21, temurin). Run `./gradlew detekt` locally before pushing — autoCorrect is enabled, so a first run may apply formatting fixes; re-run until it passes.
- **CD** (`.github/workflows/cd.yml`): when a PR to `main` is merged, `./gradlew wasmJsBrowserDistribution` builds the production bundle and `composeApp/build/dist/wasmJs/productionExecutable` is deployed to the `github-pages` branch via `JamesIves/github-pages-deploy-action@v4`.

Merging to `main` deploys immediately — a PR must build and pass detekt before merge.

## Best Practices

### Commits

- **Atomicity**: Only one logical change per commit
- **Frequency**: Commit regularly, even for small changes
- **Message**: Write so your future self and other developers can understand

### Branches

- **Lifespan**: Keep short-lived (a few days to ~1 week)
- **Sync**: Sync regularly with the main branch
- **Naming**: Use unique, descriptive names

### Pull Requests

- **Size**: Keep reviewable (up to ~500 lines)
- **Description**: Clearly describe the reason for changes and scope of impact

### Issues

- **Granularity**: Split at appropriate granularity
- **Updates**: Update progress regularly
- **Close**: Always close when completed

## Prohibited

- Direct push to main branch
- Force push on shared branches
- Massive file changes in a single PR
- Meaningless commit messages
