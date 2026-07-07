package io.github.kei_1111.feature.profile.theme

import io.github.kei_1111.core.designsystem.theme.KeiThemeController
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.ic_chevron_down_dark
import kei_1111.feature.profile.generated.resources.ic_chevron_down_light
import kei_1111.feature.profile.generated.resources.ic_chevron_right_dark
import kei_1111.feature.profile.generated.resources.ic_chevron_right_light
import kei_1111.feature.profile.generated.resources.ic_class_kotlin_dark
import kei_1111.feature.profile.generated.resources.ic_class_kotlin_light
import kei_1111.feature.profile.generated.resources.ic_close_small_dark
import kei_1111.feature.profile.generated.resources.ic_close_small_light
import kei_1111.feature.profile.generated.resources.ic_editor_only_dark
import kei_1111.feature.profile.generated.resources.ic_editor_only_light
import kei_1111.feature.profile.generated.resources.ic_editor_preview_dark
import kei_1111.feature.profile.generated.resources.ic_editor_preview_light
import kei_1111.feature.profile.generated.resources.ic_exclude_root_dark
import kei_1111.feature.profile.generated.resources.ic_exclude_root_light
import kei_1111.feature.profile.generated.resources.ic_folder_dark
import kei_1111.feature.profile.generated.resources.ic_folder_light
import kei_1111.feature.profile.generated.resources.ic_ignored_dark
import kei_1111.feature.profile.generated.resources.ic_ignored_light
import kei_1111.feature.profile.generated.resources.ic_inspections_ok_dark
import kei_1111.feature.profile.generated.resources.ic_inspections_ok_light
import kei_1111.feature.profile.generated.resources.ic_kotlin_dark
import kei_1111.feature.profile.generated.resources.ic_kotlin_gradle_script_dark
import kei_1111.feature.profile.generated.resources.ic_kotlin_gradle_script_light
import kei_1111.feature.profile.generated.resources.ic_kotlin_light
import kei_1111.feature.profile.generated.resources.ic_layout_dark
import kei_1111.feature.profile.generated.resources.ic_layout_light
import kei_1111.feature.profile.generated.resources.ic_manifest_file_dark
import kei_1111.feature.profile.generated.resources.ic_manifest_file_light
import kei_1111.feature.profile.generated.resources.ic_markdown_dark
import kei_1111.feature.profile.generated.resources.ic_markdown_light
import kei_1111.feature.profile.generated.resources.ic_more_vertical_dark
import kei_1111.feature.profile.generated.resources.ic_more_vertical_light
import kei_1111.feature.profile.generated.resources.ic_package_dark
import kei_1111.feature.profile.generated.resources.ic_package_light
import kei_1111.feature.profile.generated.resources.ic_preview_only_dark
import kei_1111.feature.profile.generated.resources.ic_preview_only_light
import kei_1111.feature.profile.generated.resources.ic_properties_dark
import kei_1111.feature.profile.generated.resources.ic_properties_light
import kei_1111.feature.profile.generated.resources.ic_reset_zoom_dark
import kei_1111.feature.profile.generated.resources.ic_reset_zoom_light
import kei_1111.feature.profile.generated.resources.ic_resources_root_dark
import kei_1111.feature.profile.generated.resources.ic_resources_root_light
import kei_1111.feature.profile.generated.resources.ic_source_root_dark
import kei_1111.feature.profile.generated.resources.ic_source_root_light
import kei_1111.feature.profile.generated.resources.ic_ui_check_dark
import kei_1111.feature.profile.generated.resources.ic_ui_check_light
import kei_1111.feature.profile.generated.resources.ic_zoom_in_dark
import kei_1111.feature.profile.generated.resources.ic_zoom_in_light
import kei_1111.feature.profile.generated.resources.ic_zoom_out_dark
import kei_1111.feature.profile.generated.resources.ic_zoom_out_light
import org.jetbrains.compose.resources.DrawableResource

/**
 * ダーク用アイコン（`ic_*_dark`）をキーに、ライトモード時に使う JetBrains Light 版
 * （`ic_*_light`）を対応付けるマップ。
 */
private val LightIconOf: Map<DrawableResource, DrawableResource> = mapOf(
    Res.drawable.ic_chevron_down_dark to Res.drawable.ic_chevron_down_light,
    Res.drawable.ic_chevron_right_dark to Res.drawable.ic_chevron_right_light,
    Res.drawable.ic_close_small_dark to Res.drawable.ic_close_small_light,
    Res.drawable.ic_more_vertical_dark to Res.drawable.ic_more_vertical_light,
    Res.drawable.ic_folder_dark to Res.drawable.ic_folder_light,
    Res.drawable.ic_package_dark to Res.drawable.ic_package_light,
    Res.drawable.ic_source_root_dark to Res.drawable.ic_source_root_light,
    Res.drawable.ic_exclude_root_dark to Res.drawable.ic_exclude_root_light,
    Res.drawable.ic_resources_root_dark to Res.drawable.ic_resources_root_light,
    Res.drawable.ic_properties_dark to Res.drawable.ic_properties_light,
    Res.drawable.ic_markdown_dark to Res.drawable.ic_markdown_light,
    Res.drawable.ic_kotlin_dark to Res.drawable.ic_kotlin_light,
    Res.drawable.ic_class_kotlin_dark to Res.drawable.ic_class_kotlin_light,
    Res.drawable.ic_kotlin_gradle_script_dark to Res.drawable.ic_kotlin_gradle_script_light,
    Res.drawable.ic_manifest_file_dark to Res.drawable.ic_manifest_file_light,
    Res.drawable.ic_ignored_dark to Res.drawable.ic_ignored_light,
    Res.drawable.ic_inspections_ok_dark to Res.drawable.ic_inspections_ok_light,
    Res.drawable.ic_editor_only_dark to Res.drawable.ic_editor_only_light,
    Res.drawable.ic_preview_only_dark to Res.drawable.ic_preview_only_light,
    Res.drawable.ic_editor_preview_dark to Res.drawable.ic_editor_preview_light,
    Res.drawable.ic_layout_dark to Res.drawable.ic_layout_light,
    Res.drawable.ic_zoom_in_dark to Res.drawable.ic_zoom_in_light,
    Res.drawable.ic_zoom_out_dark to Res.drawable.ic_zoom_out_light,
    Res.drawable.ic_reset_zoom_dark to Res.drawable.ic_reset_zoom_light,
    Res.drawable.ic_ui_check_dark to Res.drawable.ic_ui_check_light,
)

/**
 * テーマに応じたアイコンを返す。ライトモードでは対応するライト版（JetBrains Light アイコン）を、
 * ダークモードやマップ未登録のアイコンは渡された [dark] をそのまま返す。
 * `painterResource(themedIcon(Res.drawable.ic_xxx_dark))` の形で使う。
 */
fun themedIcon(dark: DrawableResource): DrawableResource =
    if (KeiThemeController.isDark) dark else LightIconOf[dark] ?: dark
