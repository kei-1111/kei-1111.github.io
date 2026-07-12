# AI documentation

This directory holds the AI-tooling assets shared between Claude Code and Codex CLI.

## Sources of truth

| Asset | Location | Loaded by |
|---|---|---|
| Codex project rules | `/AGENTS.md` | Codex (always) |
| Claude project entrypoint | `/CLAUDE.md` | Claude Code (always) |
| Claude conditional rules | `/.claude/rules/*.md` | Claude Code (path-scoped) |
| Shared skills (canonical) | `/ai-doc/skills/` | Both, via symlinks |
| Claude-only skills | `/.claude/skills/` (real directories) | Claude Code |
| Claude settings | `/.claude/settings.json` | Claude Code |
| Codex project config | `/.codex/config.toml` | Codex (trusted repos only) |

## Shared skills

The canonical copy of each shared skill lives in `ai-doc/skills/<name>/` (Agent Skills
standard: `SKILL.md` with `name` / `description` frontmatter). Both tools discover them
through per-skill symlinks:

```
.claude/skills/<name> -> ../../ai-doc/skills/<name>
.codex/skills/<name>  -> ../../ai-doc/skills/<name>
```

Do NOT symlink the whole `skills/` directory — per-skill links keep room for
product-specific skills (e.g. `.claude/skills/ask-codex/` is Claude-only and stays a
real directory).

## Maintenance

- Keep shared skills product-neutral: no Claude-only tool names (e.g. `AskUserQuestion`)
  and no Claude `@import` syntax in skill bodies. Frontmatter is limited to the keys both
  tools accept: `name`, `description`, `allowed-tools`, `metadata`, `license`.
- Add product-specific behavior in `.claude/` or `.codex/`, not here.
- When adding or renaming a shared skill, create/update BOTH symlinks
  (`.claude/skills/` and `.codex/skills/`) and verify each tool sees it — Claude: the
  skill appears in the `/` menu; Codex: `codex debug prompt-input "hi"` lists it under
  `## Skills`.
- When the architecture changes, update `AGENTS.md` and the applicable
  `.claude/rules/*.md` together.
- `.codex/config.toml` is honored only for trusted repositories; trust is granted
  per-machine in `~/.codex/config.toml` (`[projects."<abs-path>"] trust_level`), which is
  personal configuration and never committed here.
