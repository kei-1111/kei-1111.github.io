#!/usr/bin/env python3
"""Extract post-cutoff user-message digests from Claude Code session logs.

Writes one <project>__<session>.txt digest per matching session with at least one user
message at or after the cutoff. Messages before the cutoff, tool results, and system
reminders are dropped; slash-command invocations and interruption markers are retained.
Digests contain personal session text — write them to a fresh scratchpad directory,
never into a repository.

Usage:
  extract_session_digests.py --cutoff 2026-08-08T02:08:11 --match kei-1111-github-io \
      --out /path/to/scratchpad/digests [--projects-dir ~/.claude/projects]
"""

import argparse
import glob
import json
import os
import re
from datetime import datetime, timezone


def parse_ts(value):
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        return None
    return parsed if parsed.tzinfo else parsed.replace(tzinfo=timezone.utc)


def parse_args():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--cutoff", required=True, help="ISO timestamp; earlier messages are skipped")
    p.add_argument("--match", action="append", required=True,
                   help="substring a project directory name must contain (repeatable, OR)")
    p.add_argument("--out", required=True, help="output directory for the digest files")
    p.add_argument("--projects-dir", default=os.path.expanduser("~/.claude/projects"))
    return p.parse_args()


def user_texts(message):
    content = message.get("content")
    if isinstance(content, str):
        return [content]
    if isinstance(content, list):
        return [c.get("text", "") for c in content if isinstance(c, dict) and c.get("type") == "text"]
    return []


def digest_lines(path, cutoff):
    lines = []
    with open(path, encoding="utf-8", errors="replace") as fh:
        for raw in fh:
            try:
                entry = json.loads(raw)
            except ValueError:
                continue
            if entry.get("type") != "user":
                continue
            entry_ts = parse_ts(entry.get("timestamp") or "")
            if entry_ts is None or entry_ts < cutoff:
                continue
            ts = (entry.get("timestamp") or "")[:16]
            for text in user_texts(entry.get("message") or {}):
                text = text.strip()
                if not text:
                    continue
                command = re.search(r"<command-name>([^<]+)</command-name>", text)
                if command:
                    lines.append(f"[{ts}] /COMMAND: {command.group(1)}")
                elif text.startswith("<"):
                    continue
                elif "[Request interrupted" in text:
                    lines.append(f"[{ts}] *** INTERRUPTED: {text[:200]}")
                else:
                    flattened = re.sub(r"\s+", " ", text)[:600]
                    lines.append(f"[{ts}] USER: {flattened}")
    return lines


def main():
    args = parse_args()
    cutoff = parse_ts(args.cutoff)
    if cutoff is None:
        raise SystemExit(f"invalid --cutoff timestamp: {args.cutoff}")
    os.makedirs(args.out, exist_ok=True)
    with os.scandir(args.out) as entries:
        if next(entries, None) is not None:
            raise SystemExit(f"output directory must be empty: {args.out}")
    written = 0
    for project_dir in sorted(glob.glob(os.path.join(args.projects_dir, "*"))):
        name = os.path.basename(project_dir)
        if not any(m in name for m in args.match):
            continue
        for session in sorted(glob.glob(os.path.join(project_dir, "*.jsonl"))):
            lines = digest_lines(session, cutoff)
            if not lines:
                continue
            session_name = os.path.splitext(os.path.basename(session))[0]
            out_path = os.path.join(args.out, f"{name}__{session_name}.txt")
            with open(out_path, "w", encoding="utf-8") as out:
                out.write("\n".join(lines))
            print(f"{os.path.getsize(out_path):>8} {os.path.basename(out_path)}")
            written += 1
    print(f"total sessions: {written}")


if __name__ == "__main__":
    main()
