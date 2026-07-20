@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember", "TooManyFunctions")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.common.logging.LogLevel
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.VerticalResizeCursor
import io.github.kei_1111.app.core.utils.visitorDeviceLabel
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.logcatLineFor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

/** Logcat がワークスペース高に占められる最大比。上段のエディタ行の最小高を確保する。 */
private const val MAX_LOGCAT_HEIGHT_FRACTION = 0.7f

/** リサイズドラッグ中だけ、渡されたカーソルを子孫より優先して適用する。 */
internal fun Modifier.resizeCursorOverride(cursor: PointerIcon?): Modifier =
    if (cursor != null) pointerHoverIcon(cursor, overrideDescendants = true) else this

/** Logcat 高の下限〜上限（ワークスペース比）の px 範囲。親領域の高さが未測定・過小のうちは null。 */
private fun logcatHeightBoundsPx(workspaceHeightPx: Int, density: Density): ClosedFloatingPointRange<Float>? {
    val maxHeightPx = workspaceHeightPx * MAX_LOGCAT_HEIGHT_FRACTION
    val minHeightPx = with(density) { ProfileDimensions.LogcatPanelMinHeight.toPx() }
    return if (maxHeightPx <= minHeightPx) null else minHeightPx..maxHeightPx
}

/** 描画に使う Logcat の高さ。永続値はビューポート縮小やブレークポイント跨ぎでワークスペース高を超えうるため、描画時にも範囲へ収める。 */
internal fun clampedLogcatPanelHeight(
    height: Dp,
    workspaceHeightPx: Int,
    density: Density,
): Dp {
    val bounds = logcatHeightBoundsPx(workspaceHeightPx, density) ?: return height
    return with(density) { height.toPx().coerceIn(bounds.start, bounds.endInclusive).toDp() }
}

/** ドラッグ量を適用した Logcat の高さ。親領域の高さが未測定のうちは変更しない。 */
internal fun resizedLogcatPanelHeight(
    current: Dp,
    dragDelta: Float,
    workspaceHeightPx: Int,
    density: Density,
): Dp {
    val bounds = logcatHeightBoundsPx(workspaceHeightPx, density) ?: return current
    return with(density) { (current.toPx() - dragDelta).coerceIn(bounds.start, bounds.endInclusive).toDp() }
}

/** ドラッグで Logcat の高さを変えるハンドル。島間ギャップそのものをつかみ領域にする（デスク上なので罫線は描かない）。 */
@Composable
internal fun LogcatDragHandle(
    onDrag: (Float) -> Unit,
    onChangeDragCursor: (PointerIcon?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ProfileDimensions.IslandGap)
            .pointerHoverIcon(VerticalResizeCursor)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState(onDrag),
                onDragStarted = { onChangeDragCursor(VerticalResizeCursor) },
                onDragStopped = { onChangeDragCursor(null) },
            ),
    )
}

/** 実 AS New UI の Logcat ツールウィンドウを模したパネル。 */
@Composable
internal fun LogcatPanel(
    entries: ImmutableList<LogEntry>,
    onClickHide: () -> Unit,
    onClickClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    // 実 AS の「末尾へスクロール」トグル相当。スクロール位置と同じく View ローカルの一時状態
    var followTail by remember { mutableStateOf(true) }
    LaunchedEffect(entries.size, followTail) {
        if (followTail) {
            verticalScrollState.scrollTo(verticalScrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .clip(KeiTheme.shapes.island)
            .background(KeiTheme.colors.island),
    ) {
        LogcatHeader(
            onClickHide = onClickHide,
            modifier = Modifier.fillMaxWidth(),
        )
        LogcatToolbar(
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LogcatIconStrip(
                followTail = followTail,
                onClickClear = onClickClear,
                onClickFollowTail = { followTail = !followTail },
                onClickScrollTop = { followTail = false },
                scrollState = verticalScrollState,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
                ) {
                    entries.forEach { entry ->
                        Text(
                            text = logcatLineFor(entry, KeiTheme.colors),
                            style = KeiTheme.typography.code,
                            softWrap = false,
                            maxLines = 1,
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
    }
}

/** 「Logcat」タイトル・選択タブピル・「+」・右端の ⋮ と最小化を並べたヘッダー行。 */
@Composable
private fun LogcatHeader(
    onClickHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Logcat",
            style = KeiTheme.typography.chrome.copy(
                color = KeiTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        LogcatTab(onClose = onClickHide)
        Spacer(modifier = Modifier.width(10.dp))
        // 実 AS の「新しい Logcat タブを追加」。装飾のみ
        Text(
            text = "+",
            style = KeiTheme.typography.chrome.copy(fontSize = 14.sp),
        )
        Spacer(modifier = Modifier.weight(1f))
        KeiIcon(
            icon = KeiTheme.icons.moreVertical,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
        Spacer(modifier = Modifier.width(2.dp))
        ChromeIconButton(
            icon = KeiTheme.icons.logcatMinimize,
            contentDescription = "Logcatを隠す",
            iconSize = ProfileDimensions.ChromeIconSize,
            onClick = onClickHide,
        )
    }
}

/** エディタの選択タブと同じ青ピルで描く Logcat タブ。✕ でツールウィンドウを閉じる。 */
@Composable
private fun LogcatTab(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .background(KeiTheme.colors.tabSelected)
            .border(1.dp, KeiTheme.colors.tabSelectedBorder, KeiTheme.shapes.row)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Logcat",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textPrimary),
        )
        KeiIcon(
            icon = KeiTheme.icons.closeSmall,
            contentDescription = "Logcatを閉じる",
            modifier = Modifier
                .size(12.dp)
                .clip(KeiTheme.shapes.chip)
                .clickable(onClick = onClose),
        )
    }
}

/** デバイスセレクタとフィルタ欄を並べたツールバー行。実 AS の見た目だけを模した装飾。 */
@Composable
private fun LogcatToolbar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeviceSelector(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.width(8.dp))
        FilterField(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        HelpBadge()
    }
}

/** 接続デバイスのドロップダウン風表示。訪問者のブラウザを「接続中のデバイス」に見立てる。 */
@Composable
private fun DeviceSelector(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .border(1.dp, KeiTheme.colors.muted, KeiTheme.shapes.row)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.toolWindowDeviceManager,
            contentDescription = null,
            tint = KeiTheme.colors.mutedHigh,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = remember { visitorDeviceLabel() },
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textPrimary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        KeiIcon(
            icon = KeiTheme.icons.chevronDown,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
        )
    }
}

/** `package:mine` チップ入りのフィルタ欄。 */
@Composable
private fun FilterField(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .border(1.dp, KeiTheme.colors.muted, KeiTheme.shapes.row)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.logcatFilter,
            contentDescription = null,
            tint = KeiTheme.colors.mutedHigh,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(KeiTheme.shapes.chip)
                .background(KeiTheme.colors.licenseBadge)
                .padding(horizontal = 4.dp, vertical = 1.dp),
        ) {
            Text(
                text = "package:mine",
                style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.syntaxString),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        KeiIcon(
            icon = KeiTheme.icons.closeSmall,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Cc",
            style = KeiTheme.typography.chrome,
        )
        Spacer(modifier = Modifier.width(6.dp))
        KeiIcon(
            icon = KeiTheme.icons.logcatStar,
            contentDescription = null,
            tint = KeiTheme.colors.mutedHigh,
            modifier = Modifier.size(14.dp),
        )
    }
}

/** フィルタ欄の右にあるヘルプの「?」バッジ。装飾のみ。 */
@Composable
private fun HelpBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(16.dp)
            .border(1.dp, KeiTheme.colors.mutedHigh, KeiTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "?",
            style = KeiTheme.typography.chrome.copy(fontSize = 9.sp),
        )
    }
}

/**
 * ログ表示域の左に縦に並ぶツールバー。クリア・末尾追従・上下スクロールは機能し、
 * 残り（一時停止・再起動）は実 AS の見た目を模した装飾。
 */
@Composable
private fun LogcatIconStrip(
    followTail: Boolean,
    onClickClear: () -> Unit,
    onClickFollowTail: () -> Unit,
    onClickScrollTop: () -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChromeIconButton(
            icon = KeiTheme.icons.logcatClear,
            contentDescription = "ログをクリア",
            iconSize = ProfileDimensions.ChromeIconSize,
            onClick = onClickClear,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.logcatPause,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.logcatRestart,
            contentDescription = null,
            iconSize = ProfileDimensions.ChromeIconSize,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.logcatScrollEnd,
            contentDescription = "末尾へ自動スクロール",
            active = followTail,
            iconSize = ProfileDimensions.ChromeIconSize,
            onClick = onClickFollowTail,
        )
        ChromeIconButton(
            icon = KeiTheme.icons.logcatScrollUp,
            contentDescription = "先頭へスクロール",
            iconSize = ProfileDimensions.ChromeIconSize,
            onClick = {
                onClickScrollTop()
                scope.launch { scrollState.scrollTo(0) }
            },
        )
        ChromeIconButton(
            icon = KeiTheme.icons.logcatScrollDown,
            contentDescription = "末尾へスクロール",
            iconSize = ProfileDimensions.ChromeIconSize,
            onClick = {
                scope.launch { scrollState.scrollTo(scrollState.maxValue) }
            },
        )
        Spacer(modifier = Modifier.weight(1f))
        KeiIcon(
            icon = KeiTheme.icons.chevronRight,
            contentDescription = null,
            modifier = Modifier
                .size(12.dp)
                .padding(bottom = 2.dp),
        )
    }
}

@Preview
@Composable
private fun LogcatPanelPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 900.dp, height = ProfileDimensions.LogcatPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            LogcatPanel(
                entries = persistentListOf(
                    LogEntry("2026-07-19 18:48:32.139", LogLevel.Info, "Navigation", "navigate to ProfileScreen"),
                    LogEntry("2026-07-19 18:48:32.198", LogLevel.Debug, "KeiThemeController", "isDark=false"),
                    LogEntry("2026-07-19 18:48:32.770", LogLevel.Debug, "EditorPane", "select tab README.md"),
                    LogEntry("2026-07-19 18:48:33.765", LogLevel.Warn, "EditorPane", "all tabs closed"),
                    LogEntry("2026-07-19 18:48:36.454", LogLevel.Error, "LicensesRepository", "failed to load third-party licenses"),
                ),
                onClickHide = {},
                onClickClear = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
