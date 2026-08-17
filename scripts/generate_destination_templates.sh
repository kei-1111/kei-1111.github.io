#!/usr/bin/env bash

set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

golden_root="template/src/commonMain/kotlin/io/github/kei_1111/template"
template_root="ai-docs/skills/create-destination/references/templates"
temp_dir="$(mktemp -d)"
trap 'rm -rf "$temp_dir"' EXIT

screen_mappings=(
  "screen/navigation/TemplateNavigationRoute.kt|NavigationRoute.kt.template"
  "screen/navigation/TemplateNavigation.kt|Navigation.kt.template"
  "screen/navigation/TemplateNavigationExtensions.kt|NavigationExtensions.kt.template"
  "screen/destination/golden/GoldenScreenRoot.kt|ScreenRoot.kt.template"
  "screen/destination/golden/GoldenScreen.kt|Screen.kt.template"
  "screen/destination/golden/GoldenState.kt|State.kt.template"
  "screen/destination/golden/GoldenViewModelState.kt|ViewModelState.kt.template"
  "screen/destination/golden/GoldenIntent.kt|Intent.kt.template"
  "screen/destination/golden/GoldenEffect.kt|Effect.kt.template"
  "screen/destination/golden/GoldenViewModel.kt|ViewModel.kt.template"
  "screen/destination/golden/content/GoldenMobileContent.kt|MobileContent.kt.template"
  "screen/destination/golden/content/GoldenDesktopContent.kt|DesktopContent.kt.template"
)

dialog_mappings=(
  "dialog/navigation/TemplateNavigationRoute.kt|NavigationRoute.kt.template"
  "dialog/navigation/TemplateNavigation.kt|DialogNavigation.kt.template"
  "dialog/destination/golden/GoldenDialogRoot.kt|DialogRoot.kt.template"
  "dialog/destination/golden/GoldenDialog.kt|Dialog.kt.template"
  "dialog/destination/golden/GoldenState.kt|State.kt.template"
  "dialog/destination/golden/GoldenViewModelState.kt|ViewModelState.kt.template"
  "dialog/destination/golden/GoldenIntent.kt|Intent.kt.template"
  "dialog/destination/golden/GoldenEffect.kt|Effect.kt.template"
  "dialog/destination/golden/GoldenViewModel.kt|ViewModel.kt.template"
)

derive_template() {
  local package_root="$1"
  local golden_file="$2"
  local output_file="$3"

  PACKAGE_ROOT="$package_root" perl -pe '
    s/\Q$ENV{PACKAGE_ROOT}\E/io.github.kei_1111.app.feature.{{feature}}/g;
    s/Golden/{{Name}}/g;
    s/golden/{{name}}/g;
    s/Template/{{Feature}}/g;
    s/template/{{feature}}/g;
  ' "$golden_file" > "$output_file"
}

derive_mappings() {
  local package_root="$1"
  local subtree="$2"
  shift 2

  mkdir -p "$temp_dir/$subtree"

  local mapping
  local golden_path
  local template_name
  for mapping in "$@"; do
    IFS='|' read -r golden_path template_name <<< "$mapping"
    derive_template \
      "$package_root" \
      "$golden_root/$golden_path" \
      "$temp_dir/$subtree/$template_name"
  done
}

derive_mappings "io.github.kei_1111.template.screen" screen "${screen_mappings[@]}"
derive_mappings "io.github.kei_1111.template.dialog" dialog "${dialog_mappings[@]}"

shared_templates=(
  "NavigationRoute.kt.template"
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
