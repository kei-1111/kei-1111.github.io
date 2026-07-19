package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import kei_1111.app.core.designsystem.generated.resources.Res
import kei_1111.app.core.designsystem.generated.resources.ic_chevron_down_dark
import kei_1111.app.core.designsystem.generated.resources.ic_chevron_down_light
import kei_1111.app.core.designsystem.generated.resources.ic_chevron_right_dark
import kei_1111.app.core.designsystem.generated.resources.ic_chevron_right_light
import kei_1111.app.core.designsystem.generated.resources.ic_class_kotlin_dark
import kei_1111.app.core.designsystem.generated.resources.ic_class_kotlin_light
import kei_1111.app.core.designsystem.generated.resources.ic_close_small_dark
import kei_1111.app.core.designsystem.generated.resources.ic_close_small_light
import kei_1111.app.core.designsystem.generated.resources.ic_editor_only_dark
import kei_1111.app.core.designsystem.generated.resources.ic_editor_only_light
import kei_1111.app.core.designsystem.generated.resources.ic_editor_preview_dark
import kei_1111.app.core.designsystem.generated.resources.ic_editor_preview_light
import kei_1111.app.core.designsystem.generated.resources.ic_exclude_root_dark
import kei_1111.app.core.designsystem.generated.resources.ic_exclude_root_light
import kei_1111.app.core.designsystem.generated.resources.ic_expand_to_fit_dark
import kei_1111.app.core.designsystem.generated.resources.ic_expand_to_fit_light
import kei_1111.app.core.designsystem.generated.resources.ic_folder_dark
import kei_1111.app.core.designsystem.generated.resources.ic_folder_light
import kei_1111.app.core.designsystem.generated.resources.ic_gradle
import kei_1111.app.core.designsystem.generated.resources.ic_ignored_dark
import kei_1111.app.core.designsystem.generated.resources.ic_ignored_light
import kei_1111.app.core.designsystem.generated.resources.ic_inspections_ok_dark
import kei_1111.app.core.designsystem.generated.resources.ic_inspections_ok_light
import kei_1111.app.core.designsystem.generated.resources.ic_kotlin_dark
import kei_1111.app.core.designsystem.generated.resources.ic_kotlin_gradle_script_dark
import kei_1111.app.core.designsystem.generated.resources.ic_kotlin_gradle_script_light
import kei_1111.app.core.designsystem.generated.resources.ic_kotlin_light
import kei_1111.app.core.designsystem.generated.resources.ic_layout_dark
import kei_1111.app.core.designsystem.generated.resources.ic_layout_light
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_clear
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_down
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_filter
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_minimize
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_pause
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_restart
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_scroll_end
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_star
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_up
import kei_1111.app.core.designsystem.generated.resources.ic_manifest_file_dark
import kei_1111.app.core.designsystem.generated.resources.ic_manifest_file_light
import kei_1111.app.core.designsystem.generated.resources.ic_markdown_dark
import kei_1111.app.core.designsystem.generated.resources.ic_markdown_light
import kei_1111.app.core.designsystem.generated.resources.ic_merge
import kei_1111.app.core.designsystem.generated.resources.ic_more_vertical_dark
import kei_1111.app.core.designsystem.generated.resources.ic_more_vertical_light
import kei_1111.app.core.designsystem.generated.resources.ic_package_dark
import kei_1111.app.core.designsystem.generated.resources.ic_package_light
import kei_1111.app.core.designsystem.generated.resources.ic_pan_dark
import kei_1111.app.core.designsystem.generated.resources.ic_pan_light
import kei_1111.app.core.designsystem.generated.resources.ic_preview_only_dark
import kei_1111.app.core.designsystem.generated.resources.ic_preview_only_light
import kei_1111.app.core.designsystem.generated.resources.ic_properties_dark
import kei_1111.app.core.designsystem.generated.resources.ic_properties_light
import kei_1111.app.core.designsystem.generated.resources.ic_resources_root_dark
import kei_1111.app.core.designsystem.generated.resources.ic_resources_root_light
import kei_1111.app.core.designsystem.generated.resources.ic_source_root_dark
import kei_1111.app.core.designsystem.generated.resources.ic_source_root_light
import kei_1111.app.core.designsystem.generated.resources.ic_theme_dark
import kei_1111.app.core.designsystem.generated.resources.ic_theme_light
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_bookmarks
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_commit
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_debug
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_device_manager
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_logcat
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_notifications
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_project
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_terminal
import kei_1111.app.core.designsystem.generated.resources.ic_ui_check_dark
import kei_1111.app.core.designsystem.generated.resources.ic_ui_check_light
import kei_1111.app.core.designsystem.generated.resources.ic_zoom_in_dark
import kei_1111.app.core.designsystem.generated.resources.ic_zoom_in_light
import kei_1111.app.core.designsystem.generated.resources.ic_zoom_out_dark
import kei_1111.app.core.designsystem.generated.resources.ic_zoom_out_light
import org.jetbrains.compose.resources.DrawableResource

/** 多色の焼き込みアート。dark/light 両方必須（片方欠落はコンパイルエラー）。描画は tint=Color.Unspecified。 */
@Immutable
data class ThemedIcon(val dark: DrawableResource, val light: DrawableResource)

/** 状態で再着色するモノクロ1枚もの。描画時に呼出側が tint を渡す。 */
@Immutable
data class TintedIcon(val resource: DrawableResource)

@Immutable
data class KeiIcons(
    // --- ThemedIcon: 多色ペア ---
    val classKotlin: ThemedIcon,
    val kotlin: ThemedIcon,
    val kotlinGradleScript: ThemedIcon,
    val excludeRoot: ThemedIcon,
    val folder: ThemedIcon,
    val ignored: ThemedIcon,
    val manifestFile: ThemedIcon,
    val packageNode: ThemedIcon,
    val resourcesRoot: ThemedIcon,
    val sourceRoot: ThemedIcon,
    val uiCheck: ThemedIcon,
    val markdown: ThemedIcon,
    val inspectionsOk: ThemedIcon,
    // --- ThemedIcon: 灰ペア ---
    val chevronDown: ThemedIcon,
    val chevronRight: ThemedIcon,
    val closeSmall: ThemedIcon,
    val moreVertical: ThemedIcon,
    val properties: ThemedIcon,
    val layout: ThemedIcon,
    val zoomIn: ThemedIcon,
    val zoomOut: ThemedIcon,
    val pan: ThemedIcon,
    val expandToFit: ThemedIcon,
    val editorOnly: ThemedIcon,
    val previewOnly: ThemedIcon,
    val editorPreview: ThemedIcon,
    // --- TintedIcon: モノクロ（呼出側 tint）---
    val toolWindowProject: TintedIcon,
    val toolWindowCommit: TintedIcon,
    val toolWindowBookmarks: TintedIcon,
    val toolWindowDebug: TintedIcon,
    val toolWindowLogcat: TintedIcon,
    val toolWindowTerminal: TintedIcon,
    val toolWindowNotifications: TintedIcon,
    val toolWindowDeviceManager: TintedIcon,
    val gradle: TintedIcon,
    val merge: TintedIcon,
    // --- Logcat ツールウィンドウ（呼出側 tint）---
    val logcatClear: TintedIcon,
    val logcatPause: TintedIcon,
    val logcatRestart: TintedIcon,
    val logcatScrollEnd: TintedIcon,
    val logcatScrollUp: TintedIcon,
    val logcatScrollDown: TintedIcon,
    val logcatFilter: TintedIcon,
    val logcatStar: TintedIcon,
    val logcatMinimize: TintedIcon,
    // --- テーマトグル（反転アフォーダンス、呼出側 tint）---
    val themeLight: TintedIcon,
    val themeDark: TintedIcon,
)

val keiIcons: KeiIcons = KeiIcons(
    classKotlin = ThemedIcon(Res.drawable.ic_class_kotlin_dark, Res.drawable.ic_class_kotlin_light),
    kotlin = ThemedIcon(Res.drawable.ic_kotlin_dark, Res.drawable.ic_kotlin_light),
    kotlinGradleScript = ThemedIcon(Res.drawable.ic_kotlin_gradle_script_dark, Res.drawable.ic_kotlin_gradle_script_light),
    excludeRoot = ThemedIcon(Res.drawable.ic_exclude_root_dark, Res.drawable.ic_exclude_root_light),
    folder = ThemedIcon(Res.drawable.ic_folder_dark, Res.drawable.ic_folder_light),
    ignored = ThemedIcon(Res.drawable.ic_ignored_dark, Res.drawable.ic_ignored_light),
    manifestFile = ThemedIcon(Res.drawable.ic_manifest_file_dark, Res.drawable.ic_manifest_file_light),
    packageNode = ThemedIcon(Res.drawable.ic_package_dark, Res.drawable.ic_package_light),
    resourcesRoot = ThemedIcon(Res.drawable.ic_resources_root_dark, Res.drawable.ic_resources_root_light),
    sourceRoot = ThemedIcon(Res.drawable.ic_source_root_dark, Res.drawable.ic_source_root_light),
    uiCheck = ThemedIcon(Res.drawable.ic_ui_check_dark, Res.drawable.ic_ui_check_light),
    markdown = ThemedIcon(Res.drawable.ic_markdown_dark, Res.drawable.ic_markdown_light),
    inspectionsOk = ThemedIcon(Res.drawable.ic_inspections_ok_dark, Res.drawable.ic_inspections_ok_light),
    chevronDown = ThemedIcon(Res.drawable.ic_chevron_down_dark, Res.drawable.ic_chevron_down_light),
    chevronRight = ThemedIcon(Res.drawable.ic_chevron_right_dark, Res.drawable.ic_chevron_right_light),
    closeSmall = ThemedIcon(Res.drawable.ic_close_small_dark, Res.drawable.ic_close_small_light),
    moreVertical = ThemedIcon(Res.drawable.ic_more_vertical_dark, Res.drawable.ic_more_vertical_light),
    properties = ThemedIcon(Res.drawable.ic_properties_dark, Res.drawable.ic_properties_light),
    layout = ThemedIcon(Res.drawable.ic_layout_dark, Res.drawable.ic_layout_light),
    zoomIn = ThemedIcon(Res.drawable.ic_zoom_in_dark, Res.drawable.ic_zoom_in_light),
    zoomOut = ThemedIcon(Res.drawable.ic_zoom_out_dark, Res.drawable.ic_zoom_out_light),
    pan = ThemedIcon(Res.drawable.ic_pan_dark, Res.drawable.ic_pan_light),
    expandToFit = ThemedIcon(Res.drawable.ic_expand_to_fit_dark, Res.drawable.ic_expand_to_fit_light),
    editorOnly = ThemedIcon(Res.drawable.ic_editor_only_dark, Res.drawable.ic_editor_only_light),
    previewOnly = ThemedIcon(Res.drawable.ic_preview_only_dark, Res.drawable.ic_preview_only_light),
    editorPreview = ThemedIcon(Res.drawable.ic_editor_preview_dark, Res.drawable.ic_editor_preview_light),
    toolWindowProject = TintedIcon(Res.drawable.ic_toolwindow_project),
    toolWindowCommit = TintedIcon(Res.drawable.ic_toolwindow_commit),
    toolWindowBookmarks = TintedIcon(Res.drawable.ic_toolwindow_bookmarks),
    toolWindowDebug = TintedIcon(Res.drawable.ic_toolwindow_debug),
    toolWindowLogcat = TintedIcon(Res.drawable.ic_toolwindow_logcat),
    toolWindowTerminal = TintedIcon(Res.drawable.ic_toolwindow_terminal),
    toolWindowNotifications = TintedIcon(Res.drawable.ic_toolwindow_notifications),
    toolWindowDeviceManager = TintedIcon(Res.drawable.ic_toolwindow_device_manager),
    gradle = TintedIcon(Res.drawable.ic_gradle),
    merge = TintedIcon(Res.drawable.ic_merge),
    logcatClear = TintedIcon(Res.drawable.ic_logcat_clear),
    logcatPause = TintedIcon(Res.drawable.ic_logcat_pause),
    logcatRestart = TintedIcon(Res.drawable.ic_logcat_restart),
    logcatScrollEnd = TintedIcon(Res.drawable.ic_logcat_scroll_end),
    logcatScrollUp = TintedIcon(Res.drawable.ic_logcat_up),
    logcatScrollDown = TintedIcon(Res.drawable.ic_logcat_down),
    logcatFilter = TintedIcon(Res.drawable.ic_logcat_filter),
    logcatStar = TintedIcon(Res.drawable.ic_logcat_star),
    logcatMinimize = TintedIcon(Res.drawable.ic_logcat_minimize),
    themeLight = TintedIcon(Res.drawable.ic_theme_light),
    themeDark = TintedIcon(Res.drawable.ic_theme_dark),
)
