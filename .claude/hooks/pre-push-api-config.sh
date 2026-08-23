#!/usr/bin/env bash
# PreToolUse(Bash) hook: block `git push` while HEAD's API_BASE_URL differs from the production
# origin pinned by ApiConfigTest. Exit 0 allows the tool call; exit 2 blocks it and surfaces stderr
# to Claude before the same mismatch reaches CI.
set -u

parse_kotlin_string_constant() {
  local constant_name=$1
  sed -nE "s/^[[:space:]]*(internal[[:space:]]+|private[[:space:]]+)?const[[:space:]]+val[[:space:]]+${constant_name}[[:space:]]*=[[:space:]]*\"([^\"]*)\"[[:space:]]*$/\\2/p"
}

verify_api_config_pin() {
  local api_base_url=$1
  local pinned_api_base_url=$2

  if [ -z "$api_base_url" ] || [ -z "$pinned_api_base_url" ]; then
    echo "Cannot parse API_BASE_URL or its production pin from HEAD; update the hook with their declarations." >&2
    return 2
  fi

  if [ "$api_base_url" != "$pinned_api_base_url" ]; then
    echo "ApiConfig.kt at HEAD does not match the production origin pinned by ApiConfigTest. Update both intentionally or restore the production URL, commit, then push again." >&2
    return 2
  fi

  return 0
}

main() {
  local payload
  local api_config
  local api_config_test
  local content
  local test_content
  local api_base_url
  local pinned_api_base_url

  payload=$(cat)

  # Substring heuristic keeps the hook dependency-free (no jq); anything not pushing passes through.
  case "$payload" in
    *"git push"*) ;;
    *) return 0 ;;
  esac

  cd "${CLAUDE_PROJECT_DIR:?}" || return 2

  api_config='app/core/api/src/commonMain/kotlin/io/github/kei_1111/app/core/api/network/ApiConfig.kt'
  api_config_test='app/core/api/src/commonTest/kotlin/io/github/kei_1111/app/core/api/network/ApiConfigTest.kt'
  content=$(git show "HEAD:$api_config" 2>/dev/null) || {
    echo "Cannot verify API_BASE_URL because $api_config is missing from HEAD." >&2
    return 2
  }
  test_content=$(git show "HEAD:$api_config_test" 2>/dev/null) || {
    echo "Cannot verify API_BASE_URL because $api_config_test is missing from HEAD." >&2
    return 2
  }

  api_base_url=$(printf '%s\n' "$content" | parse_kotlin_string_constant API_BASE_URL)
  pinned_api_base_url=$(printf '%s\n' "$test_content" |
    parse_kotlin_string_constant PRODUCTION_API_BASE_URL)

  verify_api_config_pin "$api_base_url" "$pinned_api_base_url"
}

if [ "${BASH_SOURCE[0]:-}" = "$0" ]; then
  main
fi
