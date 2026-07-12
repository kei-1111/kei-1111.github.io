# AI documentation

This directory holds the AI-tooling assets shared between Claude Code and Codex CLI.

## Sources of truth

| Asset | Location | Loaded by |
|---|---|---|
| Codex project rules | `/AGENTS.md` | Codex (always) |
| Claude project entrypoint | `/CLAUDE.md` | Claude Code (always) |
| Claude conditional rules | `/.claude/rules/*.md` | Claude Code (path-scoped) |
| Shared agent procedures (canonical) | `/ai-doc/agents/` | Both — see below |
| Claude subagents (thin wrappers) | `/.claude/agents/*.md` | Claude Code |
| Shared skills (canonical) | `/ai-doc/skills/` | Both, via symlinks |
| Claude-only skills | `/.claude/skills/` (real directories) | Claude Code |
| Codex-only skills | `/.codex/skills/` (real directories) | Codex |
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
product-specific skills, which stay real directories on their own side (e.g.
`.claude/skills/ask-codex/` and `.claude/skills/review-by-codex/` are Claude-only;
`.codex/skills/review-by-claude/` is Codex-only).

## Shared agent procedures

`ai-doc/agents/<name>/SKILL.md` holds the canonical procedure for delegated implementation/review
work, written product-neutral in the same Agent Skills format as shared skills. Each product
consumes it through its native vehicle:

```
.codex/skills/<name>      -> ../../ai-doc/agents/<name>   (Codex runs it inline as a skill)
.claude/agents/<name>.md     real file — Claude subagent wrapper
```

The Claude wrapper adds product-specific frontmatter (`model`, `tools`) and swaps the conventions
step to the path-scoped `.claude/rules/*.md`. Do NOT symlink these into `.claude/skills/` — on the
Claude side the subagent is the consumption vehicle, not a skill.

## Maintenance

- Keep shared skills product-neutral: no product-specific tool names, configuration syntax,
  or references to product-specific rule directories. Frontmatter should normally contain only
  the Agent Skills standard `name` and `description` fields; add other fields only after verifying
  support in both tools.
- Add product-specific behavior in `.claude/` or `.codex/`, not here.
- When adding or renaming a shared skill, create/update BOTH symlinks
  (`.claude/skills/` and `.codex/skills/`) and verify each tool sees it — Claude: the
  skill appears in the `/` menu; Codex: `codex debug prompt-input "hi"` lists it under
  `## Skills`.
- Do NOT enumerate skill names in `AGENTS.md` / `CLAUDE.md` — both tools auto-discover
  skills, and each skill's `name`/`description` frontmatter is the single source of
  truth. A hand-maintained list only drifts.
- When adding or renaming a shared agent procedure, update the `.codex/skills/` symlink and the
  `.claude/agents/` wrapper together.
- When the architecture changes, update `AGENTS.md` and the applicable
  `.claude/rules/*.md` together.
- `.codex/config.toml` is honored only for trusted repositories; trust is granted
  per-machine in `~/.codex/config.toml` (`[projects."<abs-path>"] trust_level`), which is
  personal configuration and never committed here.
