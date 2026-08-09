@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember", "TooManyFunctions")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubChangelog
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubPullRequest
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.changelog_hide
import kei_1111.app.feature.profile.generated.resources.changelog_retry
import org.jetbrains.compose.resources.stringResource

/**
 * 実 AS New UI の Git ツールウィンドウ（Log タブ）を模したパネル。
 * マージ済み PR を 1 行 = 1 コミットとして表示し、各行に main ラインと分岐/マージ弧を描く。
 * 広幅ではブランチツリーと詳細プレースホルダを左右に置く 3 ペイン構成、狭幅ではリストのみ。
 */
@Composable
internal fun ChangelogPanel(
    changelog: GitHubChangelog?,
    changelogLoadFailed: Boolean,
    onClickPullRequest: (GitHubPullRequest) -> Unit,
    onClickRetry: () -> Unit,
    onClickHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .testTag(TestTags.Profile.CHANGELOG_PANEL)
            .clip(KeiTheme.shapes.island)
            .background(KeiTheme.colors.island),
    ) {
        ChangelogHeader(
            onClickHide = onClickHide,
            modifier = Modifier.fillMaxWidth(),
        )
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val showSidePanes = maxWidth >= SidePanesMinPanelWidth
            // コンパクト判定はサイドペインを除いたコミットリストの実効幅で行う（パネル幅基準だと
            // 3 ペイン表示直後の帯域でタイトルが潰れる）。
            val commitListWidth =
                if (showSidePanes) maxWidth - BranchPaneWidth - DetailsPaneWidth else maxWidth
            val compactRows = commitListWidth < CompactRowsMaxListWidth
            Row(modifier = Modifier.fillMaxSize()) {
                if (showSidePanes) {
                    BranchTreePane(
                        modifier = Modifier
                            .width(BranchPaneWidth)
                            .fillMaxHeight(),
                    )
                    PaneDivider()
                }
                CommitListPane(
                    changelog = changelog,
                    changelogLoadFailed = changelogLoadFailed,
                    compactRows = compactRows,
                    onClickPullRequest = onClickPullRequest,
                    onClickRetry = onClickRetry,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                if (showSidePanes) {
                    PaneDivider()
                    DetailsPlaceholderPane(
                        modifier = Modifier
                            .width(DetailsPaneWidth)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

private val SidePanesMinPanelWidth = 700.dp
private val CompactRowsMaxListWidth = 560.dp
private val BranchPaneWidth = 170.dp
private val DetailsPaneWidth = 210.dp
private val CommitRowHeight = 24.dp
private val GraphCellWidth = 28.dp

@Composable
private fun ChangelogHeader(
    onClickHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Git",
            modifier = Modifier.semantics { heading() },
            style = KeiTheme.typography.chrome.copy(
                color = KeiTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        // タブ列は余白全体を受け持ち、狭幅では装飾タブをクリップして右端の ⋮ / 最小化を常に確保する。
        Row(
            modifier = Modifier
                .weight(1f)
                .clipToBounds()
                .alpha(KeiTheme.colors.nonClickableAlpha),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChangelogSelectedTab(text = "Log")
            Spacer(modifier = Modifier.width(10.dp))
            // 実 AS の Git ツールウィンドウのタブ。
            ChangelogInactiveTab(text = "Console")
            ChangelogInactiveTab(text = "Worktrees")
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
            contentDescription = stringResource(Res.string.changelog_hide),
            iconSize = ProfileDimensions.ChromeIconSize,
            onClick = onClickHide,
            modifier = Modifier.testTag(TestTags.Profile.CHANGELOG_HIDE),
        )
    }
}

/** 実 AS の Git ではダークは青ピル＋青枠（エディタタブと同面）、ライトはグレーピル＋グレー枠。 */
@Composable
private fun ChangelogSelectedTab(
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
            softWrap = false,
            maxLines = 1,
        )
    }
}

@Composable
private fun ChangelogInactiveTab(
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

/** 実 AS の見た目を模した装飾。 */
@Composable
private fun BranchTreePane(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .alpha(KeiTheme.colors.nonClickableAlpha),
    ) {
        BranchSearchField(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        BranchTreeRow(text = "HEAD (Current Branch)", showChevron = false)
        BranchTreeRow(text = "Local", showChevron = true)
        BranchTreeRow(text = "Remote", showChevron = true)
        BranchTreeRow(text = "Tags", showChevron = true)
    }
}

@Composable
private fun BranchSearchField(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .border(width = 1.dp, color = KeiTheme.colors.muted, shape = KeiTheme.shapes.row)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.search,
            contentDescription = null,
            tint = KeiTheme.colors.mutedHigh,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Branch or tag",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
            softWrap = false,
            maxLines = 1,
        )
    }
}

@Composable
private fun BranchTreeRow(
    text: String,
    showChevron: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showChevron) {
            KeiIcon(
                icon = KeiTheme.icons.chevronRight,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textPrimary),
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PaneDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(KeiTheme.colors.outline),
    )
}

@Composable
private fun CommitListPane(
    changelog: GitHubChangelog?,
    changelogLoadFailed: Boolean,
    compactRows: Boolean,
    onClickPullRequest: (GitHubPullRequest) -> Unit,
    onClickRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CommitToolbar(modifier = Modifier.fillMaxWidth())
        when {
            changelogLoadFailed -> ChangelogFailedRow(
                onClickRetry = onClickRetry,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            changelog == null -> ChangelogLoadingRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            else -> CommitList(
                changelog = changelog,
                compactRows = compactRows,
                onClickPullRequest = onClickPullRequest,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

/** 実 AS の見た目を模した装飾。 */
@Composable
private fun CommitToolbar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .alpha(KeiTheme.colors.nonClickableAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommitSearchLabel()
        Spacer(modifier = Modifier.weight(1f))
        CommitFilterLabel(text = "Branch")
        CommitFilterLabel(text = "User")
        CommitFilterLabel(text = "Date")
        CommitFilterLabel(text = "Paths")
    }
}

@Composable
private fun CommitSearchLabel(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.search,
            contentDescription = null,
            tint = KeiTheme.colors.mutedHigh,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Text or hash",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
            softWrap = false,
            maxLines = 1,
        )
    }
}

@Composable
private fun CommitFilterLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
            softWrap = false,
            maxLines = 1,
        )
        KeiIcon(
            icon = KeiTheme.icons.chevronDown,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
        )
    }
}

/** contributions のエラー行と同じイディオム。 */
@Composable
private fun ChangelogFailedRow(
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
            text = "failed to load merged pull requests — ",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
        )
        Text(
            text = "retry",
            modifier = Modifier.clickable(
                onClickLabel = stringResource(Res.string.changelog_retry),
                onClick = onClickRetry,
            ),
            style = KeiTheme.typography.chrome.copy(
                color = KeiTheme.colors.syntaxLink,
                textDecoration = TextDecoration.Underline,
            ),
        )
    }
}

@Composable
private fun ChangelogLoadingRow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Loading merged pull requests…",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
        )
    }
}

@Composable
private fun CommitList(
    changelog: GitHubChangelog,
    compactRows: Boolean,
    onClickPullRequest: (GitHubPullRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val verticalScrollState = rememberScrollState()
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
        ) {
            changelog.pullRequests.forEach { pullRequest ->
                CommitRow(
                    pullRequest = pullRequest,
                    compact = compactRows,
                    onClick = { onClickPullRequest(pullRequest) },
                )
            }
        }
        VerticalScrollbar(
            scrollState = verticalScrollState,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun CommitRow(
    pullRequest: GitHubPullRequest,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    val lanePalette = KeiTheme.colors.gitLanePalette
    val mainColor = lanePalette.first()
    val branchColor = branchLaneColor(lanePalette, pullRequest.number)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CommitRowHeight)
            .clip(KeiTheme.shapes.row)
            .background(if (hoverState.hovered) KeiTheme.colors.chip else KeiTheme.colors.island)
            .hoverable(hoverState.interactionSource)
            .clickable(onClick = onClick)
            .testTag(TestTags.Profile.changelogRow(pullRequest.number.toString()))
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommitGraphCell(
            mainColor = mainColor,
            branchColor = branchColor,
            modifier = Modifier
                .width(GraphCellWidth)
                .fillMaxHeight(),
        )
        pullRequest.type?.let { type ->
            TypeBadge(type = type)
            Spacer(modifier = Modifier.width(6.dp))
        }
        CommitTitle(
            title = pullRequest.title,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        BranchRefChip(
            headRefName = pullRequest.headRefName,
            laneColor = branchColor,
        )
        // 狭幅ではタイトルの可読域を確保するため author / 日付列を落とす。
        if (!compact) {
            Spacer(modifier = Modifier.width(10.dp))
            CommitByline(mergedAt = pullRequest.mergedAt)
        }
    }
}

@Composable
private fun CommitGraphCell(
    mainColor: Color,
    branchColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawPullRequestLane(mainColor = mainColor, branchColor = branchColor)
    }
}

@Composable
private fun CommitTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textPrimary),
        softWrap = false,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun CommitByline(
    mergedAt: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "kei-1111",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
            softWrap = false,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = formatMergedDate(mergedAt),
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
            softWrap = false,
            maxLines = 1,
        )
    }
}

@Composable
private fun TypeBadge(
    type: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .background(KeiTheme.colors.selectionPill)
            .padding(horizontal = 5.dp, vertical = 1.dp),
    ) {
        Text(
            text = type,
            style = KeiTheme.typography.chrome.copy(
                color = KeiTheme.colors.textSecondary,
                fontSize = 10.sp,
            ),
            softWrap = false,
            maxLines = 1,
        )
    }
}

/** 実 AS の Log 右側の ref ラベルを模す（レーン色のブランチ名 + マージアイコン）。 */
@Composable
private fun BranchRefChip(
    headRefName: String,
    laneColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.merge,
            contentDescription = null,
            tint = laneColor,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = headRefName,
            style = KeiTheme.typography.chrome.copy(color = laneColor),
            softWrap = false,
            maxLines = 1,
        )
    }
}

@Composable
private fun DetailsPlaceholderPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Select commit to view changes",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textSecondary),
            textAlign = TextAlign.Center,
        )
    }
}

/** ブランチ弧の色は PR 番号からパレット（先頭 = main ライン用を除く）へ決定的に割り当てる。 */
private fun branchLaneColor(lanePalette: List<Color>, pullRequestNumber: Int): Color =
    lanePalette[1 + pullRequestNumber.mod(lanePalette.size - 1)]

/**
 * 1 行分のグラフ: main の縦ラインと、行下端で分岐して行中央のマージコミットへ戻る弧。
 * branch-per-issue 運用により全 PR が「main から 1 ループ」なので、行ローカルの決定的描画で足りる。
 */
private fun DrawScope.drawPullRequestLane(mainColor: Color, branchColor: Color) {
    val mainX = 10.dp.toPx()
    val branchX = 22.dp.toPx()
    val centerY = size.height / 2f
    val strokeWidth = 1.5.dp.toPx()
    drawLine(
        color = mainColor,
        start = Offset(mainX, 0f),
        end = Offset(mainX, size.height),
        strokeWidth = strokeWidth,
    )
    val arc = Path().apply {
        moveTo(mainX, size.height)
        quadraticTo(branchX, size.height, branchX, (centerY + size.height) / 2f)
        quadraticTo(branchX, centerY, mainX, centerY)
    }
    drawPath(path = arc, color = branchColor, style = Stroke(width = strokeWidth))
    drawCircle(
        color = mainColor,
        radius = 3.dp.toPx(),
        center = Offset(mainX, centerY),
    )
}

/** `mergedAt`（ISO-8601）の日付部を "Aug 9, 2026" 形式へ整形する。相対時刻は時計依存になるため使わない。 */
private fun formatMergedDate(mergedAt: String): String {
    val datePart = mergedAt.substringBefore('T')
    val parts = datePart.split('-')
    val month = parts.getOrNull(1)?.toIntOrNull()?.let { MonthAbbreviations.getOrNull(it - 1) }
    val day = parts.getOrNull(2)?.toIntOrNull()
    return if (parts.size == 3 && month != null && day != null) "$month $day, ${parts[0]}" else datePart
}

private val MonthAbbreviations =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

@Preview
@Composable
private fun ChangelogPanelPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 1100.dp, height = ProfileDimensions.ChangelogPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            ChangelogPanel(
                changelog = PreviewGitHubChangelog,
                changelogLoadFailed = false,
                onClickPullRequest = {},
                onClickRetry = {},
                onClickHide = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun ChangelogPanelNarrowPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 500.dp, height = ProfileDimensions.ChangelogPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            ChangelogPanel(
                changelog = PreviewGitHubChangelog,
                changelogLoadFailed = false,
                onClickPullRequest = {},
                onClickRetry = {},
                onClickHide = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun ChangelogPanelLoadingPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 900.dp, height = ProfileDimensions.ChangelogPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            ChangelogPanel(
                changelog = null,
                changelogLoadFailed = false,
                onClickPullRequest = {},
                onClickRetry = {},
                onClickHide = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun ChangelogPanelFailedPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 900.dp, height = ProfileDimensions.ChangelogPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            ChangelogPanel(
                changelog = null,
                changelogLoadFailed = true,
                onClickPullRequest = {},
                onClickRetry = {},
                onClickHide = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
