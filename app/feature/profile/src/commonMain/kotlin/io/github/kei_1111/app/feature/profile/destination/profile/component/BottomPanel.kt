package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.VerticalResizeCursor
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions

/** リサイズドラッグ中だけ、渡されたカーソルを子孫より優先して適用する。 */
internal fun Modifier.resizeCursorOverride(cursor: PointerIcon?): Modifier =
    if (cursor != null) pointerHoverIcon(cursor, overrideDescendants = true) else this

/** 下部パネル高の下限〜上限（ワークスペース比）の px 範囲。親領域の高さが未測定・過小のうちは null。 */
private fun bottomPanelHeightBoundsPx(workspaceHeightPx: Int, density: Density): ClosedFloatingPointRange<Float>? {
    val maxHeightPx = workspaceHeightPx * ProfileDimensions.MaxBottomPanelHeightFraction
    val minHeightPx = with(density) { ProfileDimensions.BottomPanelMinHeight.toPx() }
    return if (maxHeightPx <= minHeightPx) null else minHeightPx..maxHeightPx
}

/** 描画に使う下部パネルの高さ。永続値はビューポート縮小やブレークポイント跨ぎでワークスペース高を超えうるため、描画時にも範囲へ収める。 */
internal fun clampedBottomPanelHeight(
    height: Dp,
    workspaceHeightPx: Int,
    density: Density,
): Dp {
    val bounds = bottomPanelHeightBoundsPx(workspaceHeightPx, density) ?: return height
    return with(density) { height.toPx().coerceIn(bounds.start, bounds.endInclusive).toDp() }
}

/** ドラッグ量を適用した下部パネルの高さ。親領域の高さが未測定のうちは変更しない。 */
internal fun resizedBottomPanelHeight(
    current: Dp,
    dragDelta: Float,
    workspaceHeightPx: Int,
    density: Density,
): Dp {
    val bounds = bottomPanelHeightBoundsPx(workspaceHeightPx, density) ?: return current
    return with(density) { (current.toPx() - dragDelta).coerceIn(bounds.start, bounds.endInclusive).toDp() }
}

/** ドラッグで下部パネルの高さを変えるハンドル。島間ギャップそのものをつかみ領域にする（デスク上なので罫線は描かない）。 */
@Composable
internal fun BottomPanelDragHandle(
    onDrag: (Float) -> Unit,
    onDragStopped: () -> Unit,
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
                onDragStopped = {
                    onChangeDragCursor(null)
                    onDragStopped()
                },
            ),
    )
}

@Preview
@Composable
private fun BottomPanelDragHandlePreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(8.dp).width(240.dp)) {
            BottomPanelDragHandle(
                onDrag = {},
                onDragStopped = {},
                onChangeDragCursor = {},
            )
        }
    }
}
