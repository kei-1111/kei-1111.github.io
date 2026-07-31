@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember", "TooManyFunctions")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubIssues
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.shared.model.GitHubIssue
import io.github.kei_1111.shared.model.GitHubIssues
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.todo_hide
import kei_1111.app.feature.profile.generated.resources.todo_retry
import org.jetbrains.compose.resources.stringResource

/** Issue を実 AS の TODO 項目風に描くコメント文字列。type はタイトル先頭の `[Type]:` から server が分離済み。 */
private fun todoCommentFor(issue: GitHubIssue): String =
    "// TODO: " + (issue.type?.let { "[$it] " } ?: "") + issue.title

/**
 * 実 AS New UI の TODO ツールウィンドウを模したパネル。リポジトリの実 open Issue を
 * `// TODO:` 項目として一覧し、クリックで GitHub の Issue を新しいタブに開く。
 * 実 AS のプレビューペインは同じ内容の重複表示になるため持たず、ツリー 1 ペイン構成にする。
 */
@Composable
internal fun TodoPanel(
    issues: GitHubIssues?,
    issuesLoadFailed: Boolean,
    onClickIssue: (GitHubIssue) -> Unit,
    onClickRetry: () -> Unit,
    onClickHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .testTag(TestTags.Profile.TODO_PANEL)
            .clip(KeiTheme.shapes.island)
            .background(KeiTheme.colors.island),
    ) {
        TodoHeader(
            onClickHide = onClickHide,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            TodoIconStrip()
            when {
                issuesLoadFailed -> TodoFailedRow(
                    onClickRetry = onClickRetry,
                    modifier = Modifier.fillMaxSize(),
                )

                issues == null -> TodoLoadingRow(modifier = Modifier.fillMaxSize())

                else -> TodoTree(
                    issues = issues,
                    onClickIssue = onClickIssue,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

/** 「TODO」タイトル・タブ列（Project のみ選択、他は装飾）・右端の ⋮ と最小化を並べたヘッダー行。 */
@Composable
private fun TodoHeader(
    onClickHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "TODO",
            style = KeiTheme.typography.chrome.copy(
                color = KeiTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        // タブ列は余白全体を受け持ち、狭幅では装飾タブをクリップして右端の ⋮ / 最小化を常に確保する。
        // タブは装飾（押せない）なので、押せない要素の共通透過率で薄くして操作可否を伝える
        Row(
            modifier = Modifier
                .weight(1f)
                .clipToBounds()
                .alpha(KeiTheme.colors.nonClickableAlpha),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TodoSelectedTab(text = "Project")
            Spacer(modifier = Modifier.width(10.dp))
            // 実 AS のスコープ切替タブ。Project 以外は装飾のみ
            TodoInactiveTab(text = "Current File")
            TodoInactiveTab(text = "Scope Based")
            TodoInactiveTab(text = "Changes Changelist")
        }
        KeiIcon(
            icon = KeiTheme.icons.moreVertical,
            contentDescription = null,
            modifier = Modifier
                .size(ProfileDimensions.ChromeIconSize)
                .alpha(KeiTheme.colors.nonClickableAlpha),
        )
        Spacer(modifier = Modifier.width(2.dp))
        ChromeIconButton(
            icon = KeiTheme.icons.toolWindowHide,
            contentDescription = stringResource(Res.string.todo_hide),
            iconSize = ProfileDimensions.ChromeIconSize,
            onClick = onClickHide,
        )
    }
}

/** 選択中スコープのタブ。実 AS の TODO ではダークは青ピル＋青枠（エディタタブと同面）、ライトはグレーピル＋グレー枠。 */
@Composable
private fun TodoSelectedTab(
    text: String,
    modifier: Modifier = Modifier,
) {
    val isDark = KeiTheme.colors.isDark
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .background(if (isDark) KeiTheme.colors.tabSelected else KeiTheme.colors.selectionPill)
            .border(
                width = 1.dp,
                color = if (isDark) KeiTheme.colors.tabSelectedBorder else KeiTheme.colors.muted,
                shape = KeiTheme.shapes.row,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textPrimary),
            // 狭幅では clipToBounds で水平方向にだけ切る。折り返すとヘッダー行が縦に膨らむ
            softWrap = false,
            maxLines = 1,
        )
    }
}

/** 非選択スコープのタブ。装飾のみ。 */
@Composable
private fun TodoInactiveTab(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(
            text = text,
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textPrimary),
            softWrap = false,
            maxLines = 1,
        )
    }
}

/** ツリー表示域の左に縦に並ぶツールバー。実 AS の見た目を模した装飾。 */
@Composable
private fun TodoIconStrip(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChromeIconButton(
            icon = KeiTheme.icons.up,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.down,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.filter,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.expandAll,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.collapseAll,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.show,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.previewVertically,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
    }
}

/** 取得失敗時の警告 + 再試行行。contributions のエラー行と同じイディオム。 */
@Composable
private fun TodoFailedRow(
    onClickRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.warning,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "failed to load open issues — ",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
        )
        Text(
            text = "retry",
            modifier = Modifier.clickable(
                onClickLabel = stringResource(Res.string.todo_retry),
                onClick = onClickRetry,
            ),
            style = KeiTheme.typography.chrome.copy(
                color = KeiTheme.colors.syntaxLink,
                textDecoration = TextDecoration.Underline,
            ),
        )
    }
}

/** 取得中のプレースホルダ行。 */
@Composable
private fun TodoLoadingRow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Loading TODO items…",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
        )
    }
}

/** 左ペインのツリー。サマリ行の下に Issue を TODO 項目として並べる。 */
@Composable
private fun TodoTree(
    issues: GitHubIssues,
    onClickIssue: (GitHubIssue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KeiIcon(
                    icon = KeiTheme.icons.chevronDown,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                // サーバは first: 50 で切るため、totalCount でなく実際に描画する件数を数える
                val shownCount = issues.issues.size
                Text(
                    text = "Found $shownCount TODO ${if (shownCount == 1) "item" else "items"} " +
                        "in kei-1111.github.io",
                    style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textPrimary),
                    softWrap = false,
                    maxLines = 1,
                )
            }
            issues.issues.forEachIndexed { index, issue ->
                TodoTreeRow(
                    issue = issue,
                    // 実 AS の選択行を先頭項目で再現する（クリックは選択ではなく GitHub を開く）
                    selected = index == 0,
                    onClick = { onClickIssue(issue) },
                )
            }
        }
        VerticalScrollbar(
            scrollState = verticalScrollState,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
        HorizontalScrollbar(
            scrollState = horizontalScrollState,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

/** ツリーの 1 Issue 行。Issue 番号を実 AS の行番号に見立てて前置する。 */
@Composable
private fun TodoTreeRow(
    issue: GitHubIssue,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    val background = when {
        selected -> KeiTheme.colors.selectionPill
        hoverState.hovered -> KeiTheme.colors.chip
        else -> KeiTheme.colors.island
    }
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .background(background)
            .hoverable(hoverState.interactionSource)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${issue.number}",
            style = KeiTheme.typography.code.copy(color = KeiTheme.colors.muted),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = todoCommentFor(issue),
            style = todoCommentStyle(),
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** TODO コメントの TextStyle。実 AS 同様イタリック、ライトは Default スキームに合わせ太字も付く。 */
@Composable
private fun todoCommentStyle() = KeiTheme.typography.code.copy(
    color = KeiTheme.colors.syntaxTodo,
    fontStyle = FontStyle.Italic,
    fontWeight = if (KeiTheme.colors.isDark) FontWeight.Normal else FontWeight.Bold,
)

@Preview
@Composable
private fun TodoPanelPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 900.dp, height = ProfileDimensions.TodoPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            TodoPanel(
                issues = PreviewGitHubIssues,
                issuesLoadFailed = false,
                onClickIssue = {},
                onClickRetry = {},
                onClickHide = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun TodoPanelLoadingPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 900.dp, height = ProfileDimensions.TodoPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            TodoPanel(
                issues = null,
                issuesLoadFailed = false,
                onClickIssue = {},
                onClickRetry = {},
                onClickHide = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun TodoPanelFailedPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 900.dp, height = ProfileDimensions.TodoPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            TodoPanel(
                issues = null,
                issuesLoadFailed = true,
                onClickIssue = {},
                onClickRetry = {},
                onClickHide = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
