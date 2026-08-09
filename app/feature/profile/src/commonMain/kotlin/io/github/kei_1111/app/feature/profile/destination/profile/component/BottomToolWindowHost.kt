package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.model.BottomTool
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubIssue
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.shared.model.GitHubPullRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 下部ツールウィンドウ（Logcat / TODO / Terminal / Git）のホストスロット。
 * 「一度に開くのは1つ」の不変条件をここで一度だけ表現し、ドラッグハンドルと
 * アクティブパネルの描画・ドラッグリサイズの高さ管理を担う。
 * 実 AS 同様、開閉は即時（アニメーションなし）。島間ギャップのドラッグで高さを変えられる。
 */
@Suppress("ModifierMissing") // 親 Column にハンドルとパネルの2兄弟を並べるスロットで、単一ルートを持たない
@Composable
internal fun BottomToolWindowHost(
    openTool: BottomTool?,
    workspaceHeightPx: Int,
    onChangeDragCursor: (PointerIcon?) -> Unit,
    logEntries: ImmutableList<LogEntry>,
    logcatPanelHeight: Dp,
    onChangeLogcatPanelHeight: (Dp) -> Unit,
    onClickHideLogcat: () -> Unit,
    onClickClearLogcat: () -> Unit,
    issues: GitHubIssues?,
    issuesLoadFailed: Boolean,
    todoPanelHeight: Dp,
    onChangeTodoPanelHeight: (Dp) -> Unit,
    onClickIssue: (GitHubIssue) -> Unit,
    onClickRetry: () -> Unit,
    onClickHideTodo: () -> Unit,
    terminalLines: ImmutableList<TerminalLine>,
    terminalInput: String,
    terminalPanelHeight: Dp,
    onChangeTerminalPanelHeight: (Dp) -> Unit,
    onChangeTerminalInput: (String) -> Unit,
    onExecuteTerminalCommand: () -> Unit,
    onClickHideTerminal: () -> Unit,
    changelog: GitHubChangelog?,
    changelogLoadFailed: Boolean,
    changelogPanelHeight: Dp,
    onChangeChangelogPanelHeight: (Dp) -> Unit,
    onClickPullRequest: (GitHubPullRequest) -> Unit,
    onClickHideChangelog: () -> Unit,
) {
    if (openTool == null) return
    val density = LocalDensity.current
    val persistedHeight = when (openTool) {
        BottomTool.Logcat -> logcatPanelHeight
        BottomTool.Todo -> todoPanelHeight
        BottomTool.Terminal -> terminalPanelHeight
        BottomTool.Changelog -> changelogPanelHeight
    }
    val onChangePanelHeight = when (openTool) {
        BottomTool.Logcat -> onChangeLogcatPanelHeight
        BottomTool.Todo -> onChangeTodoPanelHeight
        BottomTool.Terminal -> onChangeTerminalPanelHeight
        BottomTool.Changelog -> onChangeChangelogPanelHeight
    }
    // State 経由の値はリコンポジション待ちで同一フレーム内の連続デルタに追従できないため、
    // ローカルで累積し、永続化用に ViewModel へも通知する
    var panelHeight by remember(openTool, persistedHeight) { mutableStateOf(persistedHeight) }
    BottomPanelDragHandle(
        onDrag = { delta ->
            panelHeight = resizedBottomPanelHeight(
                current = panelHeight,
                dragDelta = delta,
                workspaceHeightPx = workspaceHeightPx,
                density = density,
            )
        },
        onDragStopped = { onChangePanelHeight(panelHeight) },
        onChangeDragCursor = onChangeDragCursor,
    )
    val panelModifier = Modifier
        .fillMaxWidth()
        .height(clampedBottomPanelHeight(panelHeight, workspaceHeightPx, density))
    when (openTool) {
        BottomTool.Logcat -> LogcatPanel(
            entries = logEntries,
            onClickHide = onClickHideLogcat,
            onClickClear = onClickClearLogcat,
            modifier = panelModifier,
        )

        BottomTool.Todo -> TodoPanel(
            issues = issues,
            issuesLoadFailed = issuesLoadFailed,
            onClickIssue = onClickIssue,
            onClickRetry = onClickRetry,
            onClickHide = onClickHideTodo,
            modifier = panelModifier,
        )

        BottomTool.Terminal -> TerminalPanel(
            lines = terminalLines,
            input = terminalInput,
            onChangeInput = onChangeTerminalInput,
            onExecuteCommand = onExecuteTerminalCommand,
            onClickHide = onClickHideTerminal,
            modifier = panelModifier,
        )

        BottomTool.Changelog -> ChangelogPanel(
            changelog = changelog,
            changelogLoadFailed = changelogLoadFailed,
            onClickPullRequest = onClickPullRequest,
            onClickRetry = onClickRetry,
            onClickHide = onClickHideChangelog,
            modifier = panelModifier,
        )
    }
}

@Preview
@Composable
private fun BottomToolWindowHostPreview() {
    KeiTheme {
        Column(modifier = Modifier.width(480.dp)) {
            BottomToolWindowHost(
                openTool = BottomTool.Terminal,
                workspaceHeightPx = 2000,
                onChangeDragCursor = {},
                logEntries = persistentListOf(),
                logcatPanelHeight = ProfileDimensions.LogcatPanelHeight,
                onChangeLogcatPanelHeight = {},
                onClickHideLogcat = {},
                onClickClearLogcat = {},
                issues = null,
                issuesLoadFailed = false,
                todoPanelHeight = ProfileDimensions.TodoPanelHeight,
                onChangeTodoPanelHeight = {},
                onClickIssue = {},
                onClickRetry = {},
                onClickHideTodo = {},
                terminalLines = persistentListOf(),
                terminalInput = "",
                terminalPanelHeight = ProfileDimensions.TerminalPanelHeight,
                onChangeTerminalPanelHeight = {},
                onChangeTerminalInput = {},
                onExecuteTerminalCommand = {},
                onClickHideTerminal = {},
                changelog = null,
                changelogLoadFailed = false,
                changelogPanelHeight = ProfileDimensions.ChangelogPanelHeight,
                onChangeChangelogPanelHeight = {},
                onClickPullRequest = {},
                onClickHideChangelog = {},
            )
        }
    }
}
