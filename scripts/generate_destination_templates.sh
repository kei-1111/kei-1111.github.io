#!/usr/bin/env bash

set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

golden_root="template/src/commonMain/kotlin/io/github/kei_1111/template"
template_root="ai-docs/skills/create-destination/references/templates"
temp_dir="$(mktemp -d)"
trap 'rm -rf "$temp_dir"' EXIT

screen_mappings=(
  "navigation/screen/TemplateNavigationRoute.kt|NavigationRoute.kt.template"
  "navigation/screen/TemplateNavigation.kt|Navigation.kt.template"
  "navigation/screen/TemplateNavigationExtensions.kt|NavigationExtensions.kt.template"
  "destination/screen/GoldenScreenRoot.kt|ScreenRoot.kt.template"
  "destination/screen/GoldenScreen.kt|Screen.kt.template"
  "destination/screen/GoldenState.kt|State.kt.template"
  "destination/screen/GoldenViewModelState.kt|ViewModelState.kt.template"
  "destination/screen/GoldenIntent.kt|Intent.kt.template"
  "destination/screen/GoldenEffect.kt|Effect.kt.template"
  "destination/screen/GoldenViewModel.kt|ViewModel.kt.template"
  "destination/screen/content/GoldenMobileContent.kt|MobileContent.kt.template"
  "destination/screen/content/GoldenDesktopContent.kt|DesktopContent.kt.template"
)

dialog_mappings=(
  "navigation/dialog/TemplateNavigationRoute.kt|NavigationRoute.kt.template"
  "navigation/dialog/TemplateNavigation.kt|DialogNavigation.kt.template"
  "navigation/dialog/TemplateNavigationExtensions.kt|NavigationExtensions.kt.template"
  "destination/dialog/GoldenDialogRoot.kt|DialogRoot.kt.template"
  "destination/dialog/GoldenDialog.kt|Dialog.kt.template"
  "destination/dialog/GoldenState.kt|State.kt.template"
  "destination/dialog/GoldenViewModelState.kt|ViewModelState.kt.template"
  "destination/dialog/GoldenIntent.kt|Intent.kt.template"
  "destination/dialog/GoldenEffect.kt|Effect.kt.template"
  "destination/dialog/GoldenViewModel.kt|ViewModel.kt.template"
)

derive_template() {
  local kind="$1"
  local golden_file="$2"
  local output_file="$3"

  NAV_ROOT="io.github.kei_1111.template.navigation.$kind" \
  DEST_ROOT="io.github.kei_1111.template.destination.$kind" \
  perl -pe '
    s/\Q$ENV{NAV_ROOT}\E/io.github.kei_1111.app.feature.{{feature}}.navigation/g;
    s/\Q$ENV{DEST_ROOT}\E/io.github.kei_1111.app.feature.{{feature}}.destination.{{name}}/g;
    s/Golden/{{Name}}/g;
    s/Template/{{Feature}}/g;
    s/template/{{feature}}/g;
  ' "$golden_file" > "$output_file"
}

derive_mappings() {
  local kind="$1"
  shift

  mkdir -p "$temp_dir/$kind"

  local mapping
  local golden_path
  local template_name
  for mapping in "$@"; do
    IFS='|' read -r golden_path template_name <<< "$mapping"
    derive_template \
      "$kind" \
      "$golden_root/$golden_path" \
      "$temp_dir/$kind/$template_name"
  done
}

derive_mappings screen "${screen_mappings[@]}"
derive_mappings dialog "${dialog_mappings[@]}"

shared_templates=(
  "NavigationRoute.kt.template"
  "NavigationExtensions.kt.template"
  "State.kt.template"
  "ViewModelState.kt.template"
  "Intent.kt.template"
  "Effect.kt.template"
  "ViewModel.kt.template"
)

for template_name in "${shared_templates[@]}"; do
  if ! cmp -s "$temp_dir/screen/$template_name" "$temp_dir/dialog/$template_name"; then
    echo "Shared golden sources differ for $template_name" >&2
    diff -u "$temp_dir/screen/$template_name" "$temp_dir/dialog/$template_name" || true
    exit 1
  fi
done

canonical_outputs=(
  "screen/NavigationRoute.kt.template|NavigationRoute.kt.template"
  "screen/Navigation.kt.template|Navigation.kt.template"
  "screen/NavigationExtensions.kt.template|NavigationExtensions.kt.template"
  "screen/ScreenRoot.kt.template|ScreenRoot.kt.template"
  "screen/Screen.kt.template|Screen.kt.template"
  "screen/State.kt.template|State.kt.template"
  "screen/ViewModelState.kt.template|ViewModelState.kt.template"
  "screen/Intent.kt.template|Intent.kt.template"
  "screen/Effect.kt.template|Effect.kt.template"
  "screen/ViewModel.kt.template|ViewModel.kt.template"
  "screen/MobileContent.kt.template|MobileContent.kt.template"
  "screen/DesktopContent.kt.template|DesktopContent.kt.template"
  "dialog/DialogNavigation.kt.template|DialogNavigation.kt.template"
  "dialog/DialogRoot.kt.template|DialogRoot.kt.template"
  "dialog/Dialog.kt.template|Dialog.kt.template"
)

case "${1:-}" in
  "")
    for mapping in "${canonical_outputs[@]}"; do
      IFS='|' read -r derived_path template_name <<< "$mapping"
      cp "$temp_dir/$derived_path" "$template_root/$template_name"
    done
    ;;
  --check)
    status=0
    for mapping in "${canonical_outputs[@]}"; do
      IFS='|' read -r derived_path template_name <<< "$mapping"
      if ! diff -u "$template_root/$template_name" "$temp_dir/$derived_path"; then
        echo "Destination template differs: $template_name" >&2
        status=1
      fi
    done
    exit "$status"
    ;;
  *)
    echo "Usage: $0 [--check]" >&2
    exit 2
    ;;
esac
