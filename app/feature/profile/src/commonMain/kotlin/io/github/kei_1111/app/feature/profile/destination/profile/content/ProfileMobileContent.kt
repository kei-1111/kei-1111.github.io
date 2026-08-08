@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.content

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.ProfileIntent
import io.github.kei_1111.app.feature.profile.destination.profile.ProfileState
import io.github.kei_1111.app.feature.profile.destination.profile.component.BottomToolWindowHost
import io.github.kei_1111.app.feature.profile.destination.profile.component.EditorCodeArea
import io.github.kei_1111.app.feature.profile.destination.profile.component.EditorPreviewIsland
import io.github.kei_1111.app.feature.profile.destination.profile.component.LeftToolRail
import io.github.kei_1111.app.feature.profile.destination.profile.component.PreviewPane
import io.github.kei_1111.app.feature.profile.destination.profile.component.ProjectTree
import io.github.kei_1111.app.feature.profile.destination.profile.component.StatusBar
import io.github.kei_1111.app.feature.profile.destination.profile.component.TitleBar
import io.github.kei_1111.app.feature.profile.destination.profile.component.UsageCodeArea
import io.github.kei_1111.app.feature.profile.destination.profile.component.readmeSource
import io.github.kei_1111.app.feature.profile.destination.profile.component.resizeCursorOverride
import io.github.kei_1111.app.feature.profile.destination.profile.model.BottomTool
import io.github.kei_1111.app.feature.profile.destination.profile.model.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.model.profileCode
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.destination.profile.theme.deskBackground
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.app.feature.profile.model.isReadOnly
import io.github.kei_1111.shared.model.LicenseEntry

/**
 * 900px 未満：ツリー表示中はエディタ + プレビューの島の上を全幅のツリー島で覆う
 * （島はコンポーズし続け、ズームやスクロール状態を保持する）。
 * 実 AS 同様アニメーションなしで切り替える。
 */
@Composable
internal fun ProfileMobileContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .deskBackground(KeiTheme.colors),
    ) {
        TitleBar(
            onClickToggleTheme = onToggleTheme,
            onClickToggleLanguage = onToggleLanguage,
            languageToggleEnabled = state.languageToggleEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProfileDimensions.DeskPadding, vertical = 8.dp),
            onClickBuild = { onIntent(ProfileIntent.ResetEditorCode) },
            onClickSearch = { onIntent(ProfileIntent.OpenSearchEverywhere) },
        )
        MobileWorkspace(
            state = state,
            onClickToggleTree = { onIntent(ProfileIntent.ToggleTree(WindowLayout.Mobile)) },
            onClickToggleLogcat = { onIntent(ProfileIntent.ToggleLogcat) },
            onClickClearLogcat = { onIntent(ProfileIntent.ClearLogcat) },
            onChangeLogcatPanelHeight = { onIntent(ProfileIntent.UpdateLogcatPanelHeight(it)) },
            onClickToggleTodo = { onIntent(ProfileIntent.ToggleTodo) },
            onChangeTodoPanelHeight = { onIntent(ProfileIntent.UpdateTodoPanelHeight(it)) },
            onClickToggleTerminal = { onIntent(ProfileIntent.ToggleTerminal) },
            onChangeTerminalInput = { onIntent(ProfileIntent.UpdateTerminalInput(it)) },
            onExecuteTerminalCommand = { onIntent(ProfileIntent.ExecuteTerminalCommand) },
            onChangeTerminalPanelHeight = { onIntent(ProfileIntent.UpdateTerminalPanelHeight(it)) },
            onClickPageFromTree = { onIntent(ProfileIntent.UpdateSelectedPageFromTree(it, WindowLayout.Mobile)) },
            onClickPage = { onIntent(ProfileIntent.UpdateSelectedPage(it)) },
            onClosePage = { onIntent(ProfileIntent.ClosePage(it)) },
            onChangeViewMode = { onIntent(ProfileIntent.UpdateViewMode(it, WindowLayout.Mobile)) },
            onChangeCode = { page, code ->
                onIntent(
                    if (page == EditorPage.Readme) {
                        ProfileIntent.UpdateReadmeCode(code)
                    } else {
                        ProfileIntent.UpdateProfileCode(code)
                    },
                )
            },
            onClickUrl = { onIntent(ProfileIntent.OpenUrl(it)) },
            onClickLicense = { onIntent(ProfileIntent.UpdateSelectedLicense(it)) },
            onDismissLicense = { onIntent(ProfileIntent.UpdateSelectedLicense(null)) },
            onChangeWorksSheetVisible = { onIntent(ProfileIntent.UpdateWorksSheetVisibility(it)) },
            onClickRetry = { onIntent(ProfileIntent.RetryBackendData) },
            modifier = Modifier.weight(1f),
        )
        StatusBar(
            page = state.selectedPage,
            readOnly = state.selectedPage?.isReadOnly == true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProfileDimensions.DeskPadding + 4.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun MobileWorkspace(
    state: ProfileState,
    onClickToggleTree: () -> Unit,
    onClickToggleLogcat: () -> Unit,
    onClickClearLogcat: () -> Unit,
    onChangeLogcatPanelHeight: (Dp) -> Unit,
    onClickToggleTodo: () -> Unit,
    onChangeTodoPanelHeight: (Dp) -> Unit,
    onClickToggleTerminal: () -> Unit,
    onChangeTerminalInput: (String) -> Unit,
    onExecuteTerminalCommand: () -> Unit,
    onChangeTerminalPanelHeight: (Dp) -> Unit,
    onClickPageFromTree: (EditorPage) -> Unit,
    onClickPage: (EditorPage) -> Unit,
    onClosePage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onChangeCode: (EditorPage, String) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onChangeWorksSheetVisible: (Boolean) -> Unit,
    onClickRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // 右レールが無いため、右余白はタイトルバーのテーマ切替ボタン右端（DeskPadding）に揃える
            .padding(start = ProfileDimensions.RailMargin, end = ProfileDimensions.DeskPadding),
    ) {
        LeftToolRail(
            treeOpen = state.mobileTreeOpen,
            onClickToggleTree = onClickToggleTree,
            logcatOpen = state.openBottomTool == BottomTool.Logcat,
            onClickToggleLogcat = onClickToggleLogcat,
            todoOpen = state.openBottomTool == BottomTool.Todo,
            onClickToggleTodo = onClickToggleTodo,
            terminalOpen = state.openBottomTool == BottomTool.Terminal,
            onClickToggleTerminal = onClickToggleTerminal,
        )
        Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
        MobileEditorArea(
            state = state,
            onClickPage = onClickPage,
            onClosePage = onClosePage,
            onChangeViewMode = onChangeViewMode,
            onChangeCode = onChangeCode,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            onChangeWorksSheetVisible = onChangeWorksSheetVisible,
            onClickRetry = onClickRetry,
            onClickPageFromTree = onClickPageFromTree,
            onClickHideLogcat = onClickToggleLogcat,
            onClickClearLogcat = onClickClearLogcat,
            onChangeLogcatPanelHeight = onChangeLogcatPanelHeight,
            onClickHideTodo = onClickToggleTodo,
            onChangeTodoPanelHeight = onChangeTodoPanelHeight,
            onClickToggleTerminal = onClickToggleTerminal,
            onChangeTerminalInput = onChangeTerminalInput,
            onExecuteTerminalCommand = onExecuteTerminalCommand,
            onChangeTerminalPanelHeight = onChangeTerminalPanelHeight,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

/**
 * ツリー表示中はエディタ + プレビューの島の上を全幅のツリー島で覆う
 * （島はコンポーズし続け、ズームやスクロール状態を保持する）。
 * 実 AS 同様アニメーションなしで切り替える領域。
 */
@Composable
private fun MobileEditorArea(
    state: ProfileState,
    onClickPage: (EditorPage) -> Unit,
    onClosePage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onChangeCode: (EditorPage, String) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onChangeWorksSheetVisible: (Boolean) -> Unit,
    onClickRetry: () -> Unit,
    onClickPageFromTree: (EditorPage) -> Unit,
    onClickHideLogcat: () -> Unit,
    onClickClearLogcat: () -> Unit,
    onChangeLogcatPanelHeight: (Dp) -> Unit,
    onClickHideTodo: () -> Unit,
    onChangeTodoPanelHeight: (Dp) -> Unit,
    onClickToggleTerminal: () -> Unit,
    onChangeTerminalInput: (String) -> Unit,
    onExecuteTerminalCommand: () -> Unit,
    onChangeTerminalPanelHeight: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    var areaHeightPx by remember { mutableIntStateOf(0) }
    // 細いハンドル外へポインタが出ても resize カーソルを維持する
    var draggingResizeCursor by remember { mutableStateOf<PointerIcon?>(null) }
    Column(
        modifier = modifier
            .onSizeChanged { areaHeightPx = it.height }
            .resizeCursorOverride(draggingResizeCursor),
    ) {
        MobileEditorIsland(
            state = state,
            onClickPage = onClickPage,
            onClosePage = onClosePage,
            onChangeViewMode = onChangeViewMode,
            onChangeCode = onChangeCode,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            onChangeWorksSheetVisible = onChangeWorksSheetVisible,
            onClickRetry = onClickRetry,
            onClickPageFromTree = onClickPageFromTree,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        BottomToolWindowHost(
            openTool = state.openBottomTool,
            workspaceHeightPx = areaHeightPx,
            onChangeDragCursor = { draggingResizeCursor = it },
            logEntries = state.logEntries,
            logcatPanelHeight = state.logcatPanelHeight,
            onChangeLogcatPanelHeight = onChangeLogcatPanelHeight,
            onClickHideLogcat = onClickHideLogcat,
            onClickClearLogcat = onClickClearLogcat,
            issues = state.issues,
            issuesLoadFailed = state.issuesLoadFailed,
            todoPanelHeight = state.todoPanelHeight,
            onChangeTodoPanelHeight = onChangeTodoPanelHeight,
            onClickIssue = { onClickUrl(it.url) },
            onClickRetry = onClickRetry,
            onClickHideTodo = onClickHideTodo,
            terminalLines = state.terminalLines,
            terminalInput = state.terminalInput,
            terminalPanelHeight = state.terminalPanelHeight,
            onChangeTerminalPanelHeight = onChangeTerminalPanelHeight,
            onChangeTerminalInput = onChangeTerminalInput,
            onExecuteTerminalCommand = onExecuteTerminalCommand,
            onClickHideTerminal = onClickToggleTerminal,
        )
    }
}

@Composable
private fun MobileEditorIsland(
    state: ProfileState,
    onClickPage: (EditorPage) -> Unit,
    onClosePage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onChangeCode: (EditorPage, String) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onChangeWorksSheetVisible: (Boolean) -> Unit,
    onClickRetry: () -> Unit,
    onClickPageFromTree: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile
    Box(modifier = modifier) {
        // ツリー表示中も島をコンポーズし続けて zoom/スクロール状態を保持する
        // （if で外すと remember が破棄される）。後描画のツリーが全面を覆うのでタップもツリーが受ける
        EditorPreviewIsland(
            openPages = state.openPages,
            selectedPage = state.selectedPage,
            onClickPage = onClickPage,
            onClosePage = onClosePage,
            viewMode = state.mobileViewMode,
            onChangeViewMode = onChangeViewMode,
            showSplitButton = false,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (state.mobileTreeOpen) 0f else 1f),
        ) {
            val selectedPage = state.selectedPage
            if (selectedPage == null) {
                UsageCodeArea(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                if (state.mobileViewMode == EditorViewMode.CodeOnly) {
                    EditorCodeArea(
                        page = selectedPage,
                        profile = profile,
                        licenses = state.licenses,
                        works = state.works,
                        worksLoadFailed = state.worksLoadFailed,
                        editorCode = if (selectedPage == EditorPage.Readme) {
                            state.readmeEditorCode
                        } else {
                            state.profileEditorCode
                        },
                        editable = true,
                        onChangeCode = { onChangeCode(selectedPage, it) },
                        codeHasError = selectedPage == EditorPage.Profile && state.profileCodeError,
                        editorResetTick = state.editorResetTickFor(selectedPage),
                        locked = selectedPage.isReadOnly,
                        profileLoadFailed = state.profileLoadFailed,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                } else {
                    PreviewPane(
                        page = selectedPage,
                        profile = profile,
                        contributions = state.contributions,
                        licenses = state.licenses,
                        works = state.works,
                        selectedLicense = state.selectedLicense,
                        worksSheetOpen = state.worksSheetOpen,
                        onClickUrl = onClickUrl,
                        onClickLicense = onClickLicense,
                        onDismissLicense = onDismissLicense,
                        onChangeWorksSheetVisible = onChangeWorksSheetVisible,
                        onClickRetry = onClickRetry,
                        upToDate = selectedPage != EditorPage.Profile || !state.profileCodeError,
                        readmeBlocks = state.readmeBlocks,
                        fitToWidth = true,
                        profileLoadFailed = state.profileLoadFailed,
                        contributionsLoadFailed = state.contributionsLoadFailed,
                        worksLoadFailed = state.worksLoadFailed,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
            }
        }
        if (state.mobileTreeOpen) {
            ProjectTree(
                selectedPage = state.selectedPage,
                onClickPage = onClickPageFromTree,
                // ツリーの空き領域（行リストの外）はポインタ入力ノードを持たず、タップが
                // 下の非表示の島の interactive 要素へ素通りするため、全域で入力を受けて遮蔽する
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures {} },
                scrollable = true,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileMobileContentPreview() {
    KeiTheme {
        // weight ベースの固定レイアウトは無限制約下で測定できないため、Preview では有限サイズを与える
        Box(modifier = Modifier.size(width = 390.dp, height = 820.dp)) {
            ProfileMobileContent(
                state = ProfileState(
                    profile = PreviewGitHubProfile,
                    profileEditorCode = profileCode(PreviewGitHubProfile, KeiLanguage.Ja),
                    readmeEditorCode = readmeSource(KeiLanguage.Ja),
                ),
                onIntent = {},
                onToggleTheme = {},
                onToggleLanguage = {},
            )
        }
    }
}
