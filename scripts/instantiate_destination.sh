#!/usr/bin/env bash

set -euo pipefail

export LC_ALL=C

cd "$(git rev-parse --show-toplevel)"

golden_root="template/src/commonMain/kotlin/io/github/kei_1111/template"

screen_mappings=(
  "navigation/TemplateScreenNavigationRoute.kt|navigation/{Feature}NavigationRoute.kt"
  "navigation/TemplateScreenNavigation.kt|navigation/{Feature}Navigation.kt"
  "navigation/TemplateScreenNavigationExtensions.kt|navigation/{Feature}NavigationExtensions.kt"
  "destination/screen/GoldenScreenRoot.kt|destination/{name}/{Name}ScreenRoot.kt"
  "destination/screen/GoldenScreen.kt|destination/{name}/{Name}Screen.kt"
  "destination/screen/GoldenState.kt|destination/{name}/{Name}State.kt"
  "destination/screen/GoldenViewModelState.kt|destination/{name}/{Name}ViewModelState.kt"
  "destination/screen/GoldenIntent.kt|destination/{name}/{Name}Intent.kt"
  "destination/screen/GoldenEffect.kt|destination/{name}/{Name}Effect.kt"
  "destination/screen/GoldenViewModel.kt|destination/{name}/{Name}ViewModel.kt"
  "destination/screen/content/GoldenMobileContent.kt|destination/{name}/content/{Name}MobileContent.kt"
  "destination/screen/content/GoldenDesktopContent.kt|destination/{name}/content/{Name}DesktopContent.kt"
)

dialog_mappings=(
  "navigation/TemplateDialogNavigationRoute.kt|navigation/{Feature}NavigationRoute.kt"
  "navigation/TemplateDialogNavigation.kt|navigation/{Feature}Navigation.kt"
  "navigation/TemplateDialogNavigationExtensions.kt|navigation/{Feature}NavigationExtensions.kt"
  "destination/dialog/GoldenDialogDialogRoot.kt|destination/{name}/{Name}DialogRoot.kt"
  "destination/dialog/GoldenDialogDialog.kt|destination/{name}/{Name}Dialog.kt"
  "destination/dialog/GoldenDialogState.kt|destination/{name}/{Name}State.kt"
  "destination/dialog/GoldenDialogViewModelState.kt|destination/{name}/{Name}ViewModelState.kt"
  "destination/dialog/GoldenDialogIntent.kt|destination/{name}/{Name}Intent.kt"
  "destination/dialog/GoldenDialogEffect.kt|destination/{name}/{Name}Effect.kt"
  "destination/dialog/GoldenDialogViewModel.kt|destination/{name}/{Name}ViewModel.kt"
)

shared_outputs=(
  "navigation/{Feature}NavigationRoute.kt"
  "navigation/{Feature}NavigationExtensions.kt"
  "destination/{name}/{Name}State.kt"
  "destination/{name}/{Name}ViewModelState.kt"
  "destination/{name}/{Name}Intent.kt"
  "destination/{name}/{Name}Effect.kt"
  "destination/{name}/{Name}ViewModel.kt"
)

usage() {
  echo "Usage: $0 <screen|dialog> <feature> <Name> [<Feature>]" >&2
  echo "       $0 --check-sync" >&2
  exit 2
}

require_pascal() {
  local label="$1"
  local value="$2"
  case "$value" in
    [A-Z]*) ;;
    *) echo "$label must be PascalCase [A-Za-z0-9]" >&2; exit 2 ;;
  esac
  case "$value" in
    *[!A-Za-z0-9]*) echo "$label must be PascalCase [A-Za-z0-9]" >&2; exit 2 ;;
  esac
}

reject_kotlin_hard_keyword() {
  local label="$1"
  local value="$2"
  case " as break class continue do else false for fun if in interface is null object package return super this throw true try typealias typeof val var when while " in
    *" $value "*) echo "$label must not be a Kotlin hard keyword: $value" >&2; exit 2 ;;
  esac
}

fill_target() {
  local target="$1"
  target="${target//\{Feature\}/$feature_pascal}"
  target="${target//\{Name\}/$name_pascal}"
  printf '%s' "${target//\{name\}/$name_lower}"
}

instantiate_kind() {
  local kind="$1"
  local out_root="$2"
  local mode="$3"

  local name_token="Golden"
  local feature_upper_token="TemplateScreen"
  local feature_lower_token="templateScreen"
  if [ "$kind" = "dialog" ]; then
    name_token="GoldenDialog"
    feature_upper_token="TemplateDialog"
    feature_lower_token="templateDialog"
  fi

  local mappings_name="${kind}_mappings[@]"
  local mapping golden_path target_rel target_path
  for mapping in "${!mappings_name}"; do
    IFS='|' read -r golden_path target_rel <<< "$mapping"
    target_path="$out_root/$(fill_target "$target_rel")"
    if [ "$mode" = "write" ] && [ -e "$target_path" ]; then
      echo "SKIP (exists): $target_path"
      continue
    fi
    mkdir -p "$(dirname "$target_path")"
    NAV_ROOT="io.github.kei_1111.template.navigation" \
    NAV_PACKAGE="io.github.kei_1111.app.feature.$feature_lower.navigation" \
    DEST_ROOT="io.github.kei_1111.template.destination.$kind" \
    DEST_PACKAGE="io.github.kei_1111.app.feature.$feature_lower.destination.$name_lower" \
    NAME_TOKEN="$name_token" NAME_VALUE="$name_pascal" \
    FEATURE_UPPER_TOKEN="$feature_upper_token" FEATURE_UPPER_VALUE="$feature_pascal" \
    FEATURE_LOWER_TOKEN="$feature_lower_token" FEATURE_LOWER_VALUE="$feature_lower" \
    perl -pe '
      BEGIN {
        %map = (
          $ENV{NAV_ROOT} => $ENV{NAV_PACKAGE},
          $ENV{DEST_ROOT} => $ENV{DEST_PACKAGE},
          $ENV{NAME_TOKEN} => $ENV{NAME_VALUE},
          $ENV{FEATURE_UPPER_TOKEN} => $ENV{FEATURE_UPPER_VALUE},
          $ENV{FEATURE_LOWER_TOKEN} => $ENV{FEATURE_LOWER_VALUE},
        );
        $tokens = join "|", map { quotemeta } sort { length($b) <=> length($a) } keys %map;
      }
      s/($tokens)/$map{$1}/g;
    ' "$golden_root/$golden_path" > "$target_path"
    if [ "$mode" = "write" ]; then
      echo "CREATE: $target_path"
    fi
  done
}

check_sync() {
  local temp_dir
  temp_dir="$(mktemp -d)"
  trap "rm -rf -- $(printf '%q' "$temp_dir")" EXIT

  feature_lower="syncfeature"
  feature_pascal="SyncFeature"
  name_pascal="SyncDestination"
  name_lower="syncdestination"

  instantiate_kind screen "$temp_dir/screen" derive
  instantiate_kind dialog "$temp_dir/dialog" derive

  local status=0
  local shared rel
  for shared in "${shared_outputs[@]}"; do
    rel="$(fill_target "$shared")"
    if ! diff -u "$temp_dir/screen/$rel" "$temp_dir/dialog/$rel"; then
      echo "Shared golden sources differ for $(basename "$rel")" >&2
      status=1
    fi
  done
  if [ "$status" -eq 0 ]; then
    echo "Golden MVI skeletons are in sync"
  fi
  exit "$status"
}

if [ "${1:-}" = "--check-sync" ]; then
  [ "$#" -eq 1 ] || usage
  check_sync
fi

[ "$#" -ge 3 ] && [ "$#" -le 4 ] || usage

kind="$1"
case "$kind" in
  screen|dialog) ;;
  *) usage ;;
esac

feature_lower="$2"
case "$feature_lower" in
  [a-z]*) ;;
  *) echo "feature must be lowercase [a-z][a-z0-9]*" >&2; exit 2 ;;
esac
case "$feature_lower" in
  *[!a-z0-9]*) echo "feature must be lowercase [a-z][a-z0-9]*" >&2; exit 2 ;;
esac

name_pascal="$3"
require_pascal Name "$name_pascal"

if [ "$#" -eq 4 ]; then
  require_pascal Feature "$4"
fi
feature_pascal="${4:-$(printf '%s' "${feature_lower:0:1}" | tr '[:lower:]' '[:upper:]')${feature_lower:1}}"
name_lower="$(printf '%s' "$name_pascal" | tr '[:upper:]' '[:lower:]')"

reject_kotlin_hard_keyword feature "$feature_lower"
reject_kotlin_hard_keyword Name "$name_lower"

module_root="app/feature/$feature_lower/src/commonMain/kotlin/io/github/kei_1111/app/feature/$feature_lower"
instantiate_kind "$kind" "$module_root" write
