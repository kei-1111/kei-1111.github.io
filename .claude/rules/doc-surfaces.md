# Document Surfaces

This project's document inventory for post-implementation documentation maintenance. Fixed-name
contract: the shared `update-docs` skill reads `.claude/rules/doc-surfaces.md` by this exact name
in every repository; each project defines its own surface list here.

| Document | Check when |
|---|---|
| `AGENTS.md` | Conventions, architecture, or workflows it describes changed |
| `CLAUDE.md` | Its project summary or top-level guidance drifted |
| `.claude/rules/*.md` | A convention in the touched area changed; when a file-naming pattern changed, also check each rule's `paths:` frontmatter globs directly — rule injection shows only the body, so stale globs go unnoticed |
| The GitHub Issue being implemented | The implementation deviated from the Issue's stated approach (rule: `.claude/rules/git-workflow.md` — Issues) |
| `docs/ArchitectureOverview.md` / `docs/ModuleOverview.md` | Architecture or module structure changed |
| `README.md` | The user-facing project description changed |
| `ai-docs/README.md` | The AI asset layout or sharing rules changed |
| `ai-docs/**` skills and agents | A procedure they document changed |
| `.claude/agents/*.md` / `.codex/agents/*.toml` | An agent contract or its wrapper restrictions changed |
| `.claude/settings.json` | A documented hook or permission expectation changed |
| `scripts/*.sh` (headers) | A documented AI-tooling script's flags or behavior changed |
