# AI documentation

This directory holds the AI-tooling assets shared between Claude Code and Codex CLI.

## Sources of truth

| Asset | Location | Loaded by |
|---|---|---|
| Codex project rules | `/AGENTS.md` | Codex (always) |
| Claude project entrypoint | `/CLAUDE.md` | Claude Code (always) |
| Shared project rules | `/.claude/rules/*.md` | Claude Code auto-loads (every session when no `paths:`, else path-scoped); Codex reads them via explicit references and `scripts/list_matching_rules.sh` |
| Skills (canonical) | `/ai-docs/skills/<name>/` | The product(s) holding a symlink |
| Agent procedures (canonical) | `/ai-docs/agents/<name>/` | Both — see below |
| Claude subagents (thin wrappers) | `/.claude/agents/*.md` | Claude Code |
| Codex subagents (thin wrappers) | `/.codex/agents/*.toml` | Codex |
| Claude settings | `/.claude/settings.json` | Claude Code |
| Codex project config | `/.codex/config.toml` | Codex (trusted repos only) |

## Skills

The canonical copy of every skill — shared or product-specific — lives flat in
`ai-docs/skills/<name>/` (Agent Skills standard: `SKILL.md` with `name` / `description`
frontmatter). There is deliberately no grouping layer: discovery is flat on both product
sides, so a group directory would be filing decoration that only invites misfiling — each
skill's `description` carries its domain.

Each product discovers a skill through a per-skill symlink; which sides hold the link
determines which product uses it:

```
.claude/skills/<name> -> ../../ai-docs/skills/<name>
.codex/skills/<name>  -> ../../ai-docs/skills/<name>
```

Do NOT symlink the whole `ai-docs/skills/` directory — per-skill links are what select
which product uses which skill.

## Agent procedures

`ai-docs/agents/<name>/SKILL.md` holds the canonical procedure for delegated
implementation/review work, written in the same flat Agent Skills format as skills. Each product
consumes it through its native subagent mechanism, via thin wrappers that point at the
canonical file:

```
.claude/agents/<name>.md     frontmatter (`model`, `tools`) + "Read <canonical> and follow it"
.codex/agents/<name>.toml    `name`/`description`/`sandbox_mode` + `developer_instructions` pointing at the same file
```

`tools` / `sandbox_mode` are restriction fields — omit them for full access; add them only
when the wrapper narrows what the agent may do (the read-only reviewers).

Codex agent names must be snake_case (`rules_reviewer`) — one invalidly named agent silently
disables ALL custom agents. The conventions step (`scripts/list_matching_rules.sh`) lives in the
canonical procedures themselves, so wrappers are pure pointers with no per-product override. An
agent procedure that only makes sense from one product
(e.g. `codex-implementer` — Claude delegating implementation to the
Codex CLI) gets a wrapper only on that side. Do NOT expose agent procedures as skills (no
symlinks into `.claude/skills/` or `.codex/skills/`) — the subagent is the consumption vehicle.

## Maintenance

- Keep any skill linked from BOTH products product-neutral: no unconditional product-specific
  steps — an explicit conditional branch that absorbs a capability difference is fine (e.g.
  "Claude Code: publish it as an Artifact; a product without artifact publishing writes the
  HTML file"). Referencing a `.claude/rules/*.md` file by explicit repo path is fine — both
  products can read a named file (Codex just does not auto-load them, so name the file rather
  than assuming it was loaded). Single-product skills (e.g. the `codex-*` skills) may be
  product-specific but are linked from one side only.
- Frontmatter should normally contain only the Agent Skills standard `name` and `description`
  fields; add other fields only after verifying support in both tools (the structure check
  additionally allows Claude's `allowed-tools`, used by Claude-only skills, and
  `user-invocable: false`, which hides an agent-internal step skill from Claude's `/` menu
  while keeping it model-invocable; Codex ignores the key).
- When adding a skill, scaffold it with `scripts/new_skill.sh <name> [--claude-only|--codex-only]
  [--internal]` — it creates the canonical directory, the SKILL.md skeleton, and the consumer
  symlinks, then runs the structure check. When renaming, update the symlink on every product
  side that uses it. Either way verify each tool sees it — Claude: the skill appears in the `/`
  menu (internal skills excepted); Codex: `codex debug prompt-input "hi"` lists it under
  `## Skills`.
- When adding or renaming an agent procedure, update every wrapper that consumes it
  (`.claude/agents/*.md` / `.codex/agents/*.toml`) together.
- A new `.claude/rules/*.md` either has no frontmatter (always loaded) or a frontmatter block
  containing exactly the `paths:` key (a non-empty list of glob strings) — CI rejects anything
  else.
- `scripts/check_ai_docs.sh` (run by CI on every PR) verifies this structure mechanically —
  the symlink graph, SKILL.md frontmatter and size limits, reference paths and eval fixtures
  (format only — trigger fixtures are not semantically executed), wrapper targets, Codex agent
  naming, rule frontmatter, and the known regression guards. The
  script itself is the canonical list of checks (this summary is deliberately not exhaustive).
  Run it after any add/rename, alongside the per-product discovery checks above.
- `scripts/check_ai_docs.sh` and `scripts/list_matching_rules.sh` (the rule-enumeration step
  in the implementer/reviewer contracts) require `python3` with PyYAML on `PATH`; both fail
  loudly when it is missing.
- Do NOT enumerate skill names in `AGENTS.md` / `CLAUDE.md` — both tools auto-discover
  skills, and each skill's `name`/`description` frontmatter is the single source of
  truth. A hand-maintained list only drifts.
- When the architecture changes, update the affected documents together — the surface list is
  canonical in `ai-docs/skills/update-docs/SKILL.md` — Document surfaces.
- `.codex/config.toml` is honored only for trusted repositories; trust is granted
  per-machine in `~/.codex/config.toml` (`[projects."<abs-path>"] trust_level`), which is
  personal configuration and never committed here.
