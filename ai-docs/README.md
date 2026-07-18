# AI documentation

This directory holds the AI-tooling assets shared between Claude Code and Codex CLI.

## Sources of truth

| Asset | Location | Loaded by |
|---|---|---|
| Codex project rules | `/AGENTS.md` | Codex (always) |
| Claude project entrypoint | `/CLAUDE.md` | Claude Code (always) |
| Claude conditional rules | `/.claude/rules/*.md` | Claude Code (path-scoped) |
| Skills (canonical, grouped) | `/ai-docs/skills/<group>/<name>/` | The product(s) holding a symlink |
| Agent procedures (canonical, grouped) | `/ai-docs/agents/<group>/<name>/` | Both — see below |
| Claude subagents (thin wrappers) | `/.claude/agents/*.md` | Claude Code |
| Codex subagents (thin wrappers) | `/.codex/agents/*.toml` | Codex |
| Claude settings | `/.claude/settings.json` | Claude Code |
| Codex project config | `/.codex/config.toml` | Codex (trusted repos only) |

## Skills

The canonical copy of every skill — shared or product-specific — lives in
`ai-docs/skills/<group>/<name>/` (Agent Skills standard: `SKILL.md` with `name` / `description`
frontmatter), grouped by domain:

| Group | Scope |
|---|---|
| `github/` | GitHub operations (commits, issues, PRs, review triage) |
| `implementation/` | Implementing changes in this project |
| `docs/` | Project documentation maintenance |
| `cross-agent/` | Second opinions, cross reviews, and cross-model delegation between the products |

Each product discovers a skill through a per-skill symlink; which sides hold the link
determines which product uses it:

```
.claude/skills/<name> -> ../../ai-docs/skills/<group>/<name>
.codex/skills/<name>  -> ../../ai-docs/skills/<group>/<name>
```

Consumer-side entries stay flat — Claude Code does not discover nested
`skills/<group>/<name>/` directories (verified), so grouping lives only under `ai-docs/`.
Do NOT symlink a whole group directory — per-skill links are what select which product
uses which skill.

## Agent procedures

`ai-docs/agents/<group>/<name>/SKILL.md` holds the canonical procedure for delegated
implementation/review work, written in the same Agent Skills format as skills and grouped by
the same domain taxonomy (currently `implementation/` and `cross-agent/`). Each product
consumes it through its native subagent mechanism, via thin wrappers that point at the
canonical file:

```
.claude/agents/<name>.md     frontmatter (`model`, `tools`) + "Read <canonical> and follow it"
.codex/agents/<name>.toml    `name`/`description`/`sandbox_mode` + `developer_instructions` pointing at the same file
```

Codex agent names must be snake_case (`rules_reviewer`) — one invalidly named agent silently
disables ALL custom agents. The Claude wrapper additionally swaps the conventions step to the
path-scoped `.claude/rules/*.md`. An agent procedure that only makes sense from one product
(e.g. `cross-agent/codex-implementer` — Claude delegating implementation to GPT-5.6 Sol via the
Codex CLI) gets a wrapper only on that side. Do NOT expose agent procedures as skills (no
symlinks into `.claude/skills/` or `.codex/skills/`) — the subagent is the consumption vehicle.

## Maintenance

- Keep any skill linked from BOTH products product-neutral: no product-specific tool names,
  configuration syntax, or references to product-specific rule directories as the agent's own
  conventions (naming them as maintenance targets is fine). Single-product skills
  (e.g. `cross-agent/*`) may be product-specific but are linked from one side only.
- Frontmatter should normally contain only the Agent Skills standard `name` and `description`
  fields; add other fields only after verifying support in both tools.
- When adding or renaming a skill, create/update the symlink on every product side that uses
  it and verify each tool sees it — Claude: the skill appears in the `/` menu; Codex:
  `codex debug prompt-input "hi"` lists it under `## Skills`.
- When adding or renaming an agent procedure, update every wrapper that consumes it
  (`.claude/agents/*.md` / `.codex/agents/*.toml`) together.
- `scripts/check_ai_docs.sh` (run by CI on every PR) verifies this structure mechanically:
  symlinks resolve and match their target directory names, every canonical directory holds a
  `SKILL.md` whose frontmatter `name` matches the directory, wrappers reference existing
  canonical files, and Codex agent names are snake_case. Run it after any add/rename, alongside
  the per-product discovery checks above.
- Do NOT enumerate skill names in `AGENTS.md` / `CLAUDE.md` — both tools auto-discover
  skills, and each skill's `name`/`description` frontmatter is the single source of
  truth. A hand-maintained list only drifts.
- When the architecture changes, update `AGENTS.md` and the applicable
  `.claude/rules/*.md` together.
- `.codex/config.toml` is honored only for trusted repositories; trust is granted
  per-machine in `~/.codex/config.toml` (`[projects."<abs-path>"] trust_level`), which is
  personal configuration and never committed here.
