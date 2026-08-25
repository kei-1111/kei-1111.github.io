package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import kei_1111.app.core.designsystem.generated.resources.Res
import kei_1111.app.core.designsystem.generated.resources.ic_add
import kei_1111.app.core.designsystem.generated.resources.ic_build
import kei_1111.app.core.designsystem.generated.resources.ic_chevron_down
import kei_1111.app.core.designsystem.generated.resources.ic_chevron_right
import kei_1111.app.core.designsystem.generated.resources.ic_class_kotlin
import kei_1111.app.core.designsystem.generated.resources.ic_close_small
import kei_1111.app.core.designsystem.generated.resources.ic_collapse_all
import kei_1111.app.core.designsystem.generated.resources.ic_delete
import kei_1111.app.core.designsystem.generated.resources.ic_down
import kei_1111.app.core.designsystem.generated.resources.ic_editor_only
import kei_1111.app.core.designsystem.generated.resources.ic_editor_preview
import kei_1111.app.core.designsystem.generated.resources.ic_exclude_root
import kei_1111.app.core.designsystem.generated.resources.ic_expand_all
import kei_1111.app.core.designsystem.generated.resources.ic_expand_to_fit
import kei_1111.app.core.designsystem.generated.resources.ic_filter
import kei_1111.app.core.designsystem.generated.resources.ic_folder
import kei_1111.app.core.designsystem.generated.resources.ic_gradle
import kei_1111.app.core.designsystem.generated.resources.ic_ignored
import kei_1111.app.core.designsystem.generated.resources.ic_info
import kei_1111.app.core.designsystem.generated.resources.ic_inspections_error
import kei_1111.app.core.designsystem.generated.resources.ic_inspections_ok
import kei_1111.app.core.designsystem.generated.resources.ic_kotlin
import kei_1111.app.core.designsystem.generated.resources.ic_kotlin_gradle_script
import kei_1111.app.core.designsystem.generated.resources.ic_layout
import kei_1111.app.core.designsystem.generated.resources.ic_lock
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_clear
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_down
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_filter
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_pause
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_restart
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_scroll_end
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_star
import kei_1111.app.core.designsystem.generated.resources.ic_logcat_up
import kei_1111.app.core.designsystem.generated.resources.ic_manifest_file
import kei_1111.app.core.designsystem.generated.resources.ic_markdown
import kei_1111.app.core.designsystem.generated.resources.ic_merge
import kei_1111.app.core.designsystem.generated.resources.ic_more_vertical
import kei_1111.app.core.designsystem.generated.resources.ic_open_in_tool_window
import kei_1111.app.core.designsystem.generated.resources.ic_package
import kei_1111.app.core.designsystem.generated.resources.ic_pan
import kei_1111.app.core.designsystem.generated.resources.ic_pin
import kei_1111.app.core.designsystem.generated.resources.ic_preview_only
import kei_1111.app.core.designsystem.generated.resources.ic_preview_vertically
import kei_1111.app.core.designsystem.generated.resources.ic_properties
import kei_1111.app.core.designsystem.generated.resources.ic_refresh
import kei_1111.app.core.designsystem.generated.resources.ic_resources_root
import kei_1111.app.core.designsystem.generated.resources.ic_search
import kei_1111.app.core.designsystem.generated.resources.ic_show
import kei_1111.app.core.designsystem.generated.resources.ic_source_root
import kei_1111.app.core.designsystem.generated.resources.ic_theme_dark
import kei_1111.app.core.designsystem.generated.resources.ic_theme_light
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_bookmarks
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_commit
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_debug
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_device_manager
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_hide
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_logcat
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_notifications
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_project
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_terminal
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_todo
import kei_1111.app.core.designsystem.generated.resources.ic_toolwindow_vcs
import kei_1111.app.core.designsystem.generated.resources.ic_translate
import kei_1111.app.core.designsystem.generated.resources.ic_ui_check
import kei_1111.app.core.designsystem.generated.resources.ic_undo
import kei_1111.app.core.designsystem.generated.resources.ic_up
import kei_1111.app.core.designsystem.generated.resources.ic_vcs_diff
import kei_1111.app.core.designsystem.generated.resources.ic_vcs_push
import kei_1111.app.core.designsystem.generated.resources.ic_vcs_revert
import kei_1111.app.core.designsystem.generated.resources.ic_vcs_update
import kei_1111.app.core.designsystem.generated.resources.ic_warning
import kei_1111.app.core.designsystem.generated.resources.ic_zoom_in
import kei_1111.app.core.designsystem.generated.resources.ic_zoom_out
import org.jetbrains.compose.resources.DrawableResource

/** 状態で再着色するモノクロ1枚もの。描画時に呼出側が tint を渡す。 */
@Immutable
data class TintedIcon(val resource: DrawableResource)

@Immutable
data class KeiIcons(
    val classKotlin: DrawableResource,
    val kotlin: DrawableResource,
    val kotlinGradleScript: DrawableResource,
    val excludeRoot: DrawableResource,
    val folder: DrawableResource,
    val ignored: DrawableResource,
    val manifestFile: DrawableResource,
    val packageNode: DrawableResource,
    val resourcesRoot: DrawableResource,
    val sourceRoot: DrawableResource,
    val uiCheck: DrawableResource,
    val markdown: DrawableResource,
    val inspectionsOk: DrawableResource,
    val inspectionsError: DrawableResource,
    val info: DrawableResource,
    val warning: DrawableResource,
    val chevronDown: DrawableResource,
    val chevronRight: DrawableResource,
    val closeSmall: DrawableResource,
    val moreVertical: DrawableResource,
    val properties: DrawableResource,
    val layout: DrawableResource,
    val zoomIn: DrawableResource,
    val zoomOut: DrawableResource,
    val pan: DrawableResource,
    val expandToFit: DrawableResource,
    val editorOnly: DrawableResource,
    val previewOnly: DrawableResource,
    val editorPreview: DrawableResource,
    val toolWindowProject: TintedIcon,
    val toolWindowCommit: TintedIcon,
    val toolWindowBookmarks: TintedIcon,
    val toolWindowDebug: TintedIcon,
    val toolWindowLogcat: TintedIcon,
    val toolWindowTerminal: TintedIcon,
    val toolWindowTodo: TintedIcon,
    val toolWindowNotifications: TintedIcon,
    val toolWindowDeviceManager: TintedIcon,
    val toolWindowVcs: TintedIcon,
    val add: TintedIcon,
    val vcsUpdate: TintedIcon,
    val vcsPush: TintedIcon,
    val vcsRevert: TintedIcon,
    val vcsDiff: TintedIcon,
    val delete: TintedIcon,
    val refresh: TintedIcon,
    val undo: TintedIcon,
    /** ツールウィンドウ共通の「隠す」（expUI general/hide）。Logcat / TODO のヘッダーで使用。 */
    val toolWindowHide: TintedIcon,
    val gradle: TintedIcon,
    val merge: TintedIcon,
    val build: TintedIcon,
    val lock: TintedIcon,
    val search: TintedIcon,
    val filter: TintedIcon,
    val pin: TintedIcon,
    val openInToolWindow: TintedIcon,
    val logcatClear: TintedIcon,
    val logcatPause: TintedIcon,
    val logcatRestart: TintedIcon,
    val logcatScrollEnd: TintedIcon,
    val logcatScrollUp: TintedIcon,
    val logcatScrollDown: TintedIcon,
    val logcatFilter: TintedIcon,
    val logcatStar: TintedIcon,
    val up: TintedIcon,
    val down: TintedIcon,
    val expandAll: TintedIcon,
    val collapseAll: TintedIcon,
    val show: TintedIcon,
    val previewVertically: TintedIcon,
    val translate: TintedIcon,
    // 反転アフォーダンス
    val themeLight: TintedIcon,
    val themeDark: TintedIcon,
)

val keiIcons: KeiIcons = KeiIcons(
    classKotlin = Res.drawable.ic_class_kotlin,
    kotlin = Res.drawable.ic_kotlin,
    kotlinGradleScript = Res.drawable.ic_kotlin_gradle_script,
    excludeRoot = Res.drawable.ic_exclude_root,
    folder = Res.drawable.ic_folder,
    ignored = Res.drawable.ic_ignored,
    manifestFile = Res.drawable.ic_manifest_file,
    packageNode = Res.drawable.ic_package,
    resourcesRoot = Res.drawable.ic_resources_root,
    sourceRoot = Res.drawable.ic_source_root,
    uiCheck = Res.drawable.ic_ui_check,
    markdown = Res.drawable.ic_markdown,
    inspectionsOk = Res.drawable.ic_inspections_ok,
    inspectionsError = Res.drawable.ic_inspections_error,
    info = Res.drawable.ic_info,
    warning = Res.drawable.ic_warning,
    chevronDown = Res.drawable.ic_chevron_down,
    chevronRight = Res.drawable.ic_chevron_right,
    closeSmall = Res.drawable.ic_close_small,
    moreVertical = Res.drawable.ic_more_vertical,
    properties = Res.drawable.ic_properties,
    layout = Res.drawable.ic_layout,
    zoomIn = Res.drawable.ic_zoom_in,
    zoomOut = Res.drawable.ic_zoom_out,
    pan = Res.drawable.ic_pan,
    expandToFit = Res.drawable.ic_expand_to_fit,
    editorOnly = Res.drawable.ic_editor_only,
    previewOnly = Res.drawable.ic_preview_only,
    editorPreview = Res.drawable.ic_editor_preview,
    toolWindowProject = TintedIcon(Res.drawable.ic_toolwindow_project),
    toolWindowCommit = TintedIcon(Res.drawable.ic_toolwindow_commit),
    toolWindowBookmarks = TintedIcon(Res.drawable.ic_toolwindow_bookmarks),
    toolWindowDebug = TintedIcon(Res.drawable.ic_toolwindow_debug),
    toolWindowLogcat = TintedIcon(Res.drawable.ic_toolwindow_logcat),
    toolWindowTerminal = TintedIcon(Res.drawable.ic_toolwindow_terminal),
    toolWindowTodo = TintedIcon(Res.drawable.ic_toolwindow_todo),
    toolWindowNotifications = TintedIcon(Res.drawable.ic_toolwindow_notifications),
    toolWindowDeviceManager = TintedIcon(Res.drawable.ic_toolwindow_device_manager),
    toolWindowVcs = TintedIcon(Res.drawable.ic_toolwindow_vcs),
    add = TintedIcon(Res.drawable.ic_add),
    vcsUpdate = TintedIcon(Res.drawable.ic_vcs_update),
    vcsPush = TintedIcon(Res.drawable.ic_vcs_push),
    vcsRevert = TintedIcon(Res.drawable.ic_vcs_revert),
    vcsDiff = TintedIcon(Res.drawable.ic_vcs_diff),
    delete = TintedIcon(Res.drawable.ic_delete),
    refresh = TintedIcon(Res.drawable.ic_refresh),
    undo = TintedIcon(Res.drawable.ic_undo),
    toolWindowHide = TintedIcon(Res.drawable.ic_toolwindow_hide),
    gradle = TintedIcon(Res.drawable.ic_gradle),
    merge = TintedIcon(Res.drawable.ic_merge),
    build = TintedIcon(Res.drawable.ic_build),
    lock = TintedIcon(Res.drawable.ic_lock),
    search = TintedIcon(Res.drawable.ic_search),
    filter = TintedIcon(Res.drawable.ic_filter),
    pin = TintedIcon(Res.drawable.ic_pin),
    openInToolWindow = TintedIcon(Res.drawable.ic_open_in_tool_window),
    logcatClear = TintedIcon(Res.drawable.ic_logcat_clear),
    logcatPause = TintedIcon(Res.drawable.ic_logcat_pause),
    logcatRestart = TintedIcon(Res.drawable.ic_logcat_restart),
    logcatScrollEnd = TintedIcon(Res.drawable.ic_logcat_scroll_end),
    logcatScrollUp = TintedIcon(Res.drawable.ic_logcat_up),
    logcatScrollDown = TintedIcon(Res.drawable.ic_logcat_down),
    logcatFilter = TintedIcon(Res.drawable.ic_logcat_filter),
    logcatStar = TintedIcon(Res.drawable.ic_logcat_star),
    up = TintedIcon(Res.drawable.ic_up),
    down = TintedIcon(Res.drawable.ic_down),
    expandAll = TintedIcon(Res.drawable.ic_expand_all),
    collapseAll = TintedIcon(Res.drawable.ic_collapse_all),
    show = TintedIcon(Res.drawable.ic_show),
    previewVertically = TintedIcon(Res.drawable.ic_preview_vertically),
    translate = TintedIcon(Res.drawable.ic_translate),
    themeLight = TintedIcon(Res.drawable.ic_theme_light),
    themeDark = TintedIcon(Res.drawable.ic_theme_dark),
)
