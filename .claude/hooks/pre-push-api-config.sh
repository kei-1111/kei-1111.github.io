#!/usr/bin/env bash
# PreToolUse(Bash) hook: block `git push` while HEAD's ApiConfig.kt points API_BASE_URL at a
# local origin — ApiConfigTest pins the value on CI, but this stops the push before that
# round-trip. Exit 0 allows the tool call; exit 2 blocks it and surfaces stderr to Claude.
set -u

payload=$(cat)

# Substring heuristic keeps the hook dependency-free (no jq); anything not pushing passes through.
case "$payload" in
  *"git push"*) ;;
  *) exit 0 ;;
esac

cd "${CLAUDE_PROJECT_DIR:?}" || exit 2

api_config='app/core/api/src/commonMain/kotlin/io/github/kei_1111/app/core/api/network/ApiConfig.kt'
content=$(git show "HEAD:$api_config" 2>/dev/null) || exit 0

if printf '%s' "$content" | grep -Eqi 'API_BASE_URL[^"]*"[^"]*(localhost|127\.0\.0\.1|0\.0\.0\.0)'; then
  echo "ApiConfig.kt at HEAD points API_BASE_URL at a local origin — the deployed client would break. Restore the production URL, commit, then push again." >&2
  exit 2
fi
exit 0
