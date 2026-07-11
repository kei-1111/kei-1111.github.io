# Git Workflow

Project Git/GitHub conventions for kei-1111.github.io.

## Commit Message Convention

Follows [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/) for the structure, but the description is written in **Japanese**.

### Format

```
<type>: <説明>
<type>(scope): <説明>
```

- **type**: Required. Indicates the kind of change (English, lowercase)
- **scope**: Optional. Scope of the change — a module-ish segment (English, lowercase). Observed scopes: `profile`, `splash`, `core`, `designsystem`, `app`, `utils`, `deps`
- **説明 (description)**: Required. Concise description of the change, written in Japanese

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

- **Language**: type/scope in English; the description in Japanese
- **Style**: end the description in noun form (体言止め — e.g. 追加 / 修正 / 削除 / 解消 / 統一), matching the existing log
- **Content**: be specific and clear about what changed
- **Length**: one concise line (existing log entries run roughly 20–40 Japanese characters)

### Commit Granularity

Commit at a granularity that allows cherry-picking without issues.

- **Self-contained**: Each commit completes one logical change
- **Independent**: Does not depend on subsequent commits

### Examples

Real examples from the project log:

```
feat(profile): ProjectTree を横スクロール可能にしファイル名の見切れを解消
fix(profile): note リンクのアイコンを現行の公式ロゴに修正
refactor(splash): onTimeoutをUpdatePageVisibilityのタイムアウトコルーチンへinline化
chore(designsystem): 未使用のカラートークン6件と Durations.Short/Medium を削除
docs: CLAUDE.mdとUI実装ルールを追加
```

## Branch Naming Convention

### Format

```
<type>/#<issue番号>
```

The branch type mirrors the Type of the corresponding Issue.

### Types

- `feature/`: New feature (`[Feature]` Issues)
- `fix/`: Bug fix (`[Bug]` Issues)
- `refactor/`: Refactoring (`[Refactor]` Issues)
- `other/`: Issues filed via the Other Issue form (`[Other]`, `[Documentation]`, `[Research]`, etc.)

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
[<Type>]: <タイトル>
```

The title is written in Japanese. Issues are created from one of the three Issue forms in `.github/ISSUE_TEMPLATE/`:

| Form | Title prefix | Notes |
|------|--------------|-------|
| `bug_report.yml` | `[Bug]: ` | Fields: 再現手順 / 期待する動作 / 実際の動作 / 補足情報 |
| `feature_request.yml` | `[Feature]: ` | Fields: 新機能の説明 / 詳細な説明 / 補足情報 |
| `other.yml` | `[<Type>]: ` | Type is chosen from `Refactor` / `Documentation` / `Research` / `Other`. Fields: 概要 / 詳細な説明 / 補足情報 |

All three forms require a priority dropdown (`High Priority` / `Medium Priority` / `Low Priority`).

### Rules

- **Clarity**: Make it immediately clear what needs to be done
- **Specificity**: Avoid abstract descriptions; be concrete
- **Single responsibility**: Each Issue focuses on one responsibility

### Examples

Real examples from the project:

```
[Feature]: 新デザインに変更
[Bug]: note リンクのアイコンが公式ロゴと異なる
[Refactor]: UiConfigの削除
[Other]: AI用ドキュメントの整備
```

## Pull Request Convention

### Title

```
[<Type>]: <タイトル>
```

- **Issue-linked**: Use the corresponding Issue title verbatim as the PR title
- **Consistency**: Link with the branch name (`<type>/#<issue番号>`)
- **Language**: Japanese (same as the Issue title)

### Body

Follow `.github/PULL_REQUEST_TEMPLATE.md`, written in Japanese:

- `## 概要` — always
- `## 実施Issue` — always (link the Issue)
- `## 原因と対処` — bug fixes only
- `## UI変更` — UI changes only (Before/After image table)

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
