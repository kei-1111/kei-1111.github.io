@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.rememberHoverState
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) = Scrollbar(scrollState = scrollState, orientation = Orientation.Vertical, modifier = modifier)

@Composable
internal fun HorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) = Scrollbar(scrollState = scrollState, orientation = Orientation.Horizontal, modifier = modifier)

/**
 * 実 AS エディタ風のオーバーレイスクロールバー。
 * commonMain には CMP の Scrollbar API（Skiko 専用）が無いため、[ScrollState] から自前描画する。
 */
@Composable
private fun Scrollbar(
    scrollState: ScrollState,
    orientation: Orientation,
    modifier: Modifier = Modifier,
) {
    // 初回測定前は maxValue が Int.MAX_VALUE のため、実際のスクロール範囲が確定するまで描画しない
    if (scrollState.maxValue == 0 || scrollState.maxValue == Int.MAX_VALUE) return

    val isVertical = orientation == Orientation.Vertical
    BoxWithConstraints(
        modifier = modifier.then(
            if (isVertical) {
                Modifier
                    .fillMaxHeight()
                    .width(ProfileDimensions.ScrollbarThickness)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(ProfileDimensions.ScrollbarThickness)
            },
        ),
    ) {
        val viewport = if (isVertical) constraints.maxHeight.toFloat() else constraints.maxWidth.toFloat()
        val thumbLength = max(
            viewport * viewport / (viewport + scrollState.maxValue),
            with(LocalDensity.current) { ProfileDimensions.ScrollbarMinThumbLength.toPx() },
        )
        val trackRange = viewport - thumbLength
        if (trackRange <= 0f) return@BoxWithConstraints
        val thumbLengthDp = with(LocalDensity.current) { thumbLength.toDp() }
        val hoverState = rememberHoverState()

        // scrollState.value はここ（配置ラムダ）でのみ読む。
        // コンポジションで読むとスクロール毎に再コンポーズされるため
        Box(
            modifier = Modifier
                .offset {
                    // 配置ラムダは冒頭のガードを通らず再実行されるため、コンテンツ縮小で
                    // maxValue が 0 になった瞬間の 0/0 (NaN) をここでも防ぐ
                    val maxValue = scrollState.maxValue
                    val thumbOffset = if (maxValue in 1 until Int.MAX_VALUE) {
                        (scrollState.value.toFloat() / maxValue * trackRange).roundToInt()
                    } else {
                        0
                    }
                    if (isVertical) IntOffset(0, thumbOffset) else IntOffset(thumbOffset, 0)
                }
                .then(
                    if (isVertical) {
                        Modifier
                            .height(thumbLengthDp)
                            .fillMaxWidth()
                    } else {
                        Modifier
                            .width(thumbLengthDp)
                            .fillMaxHeight()
                    },
                )
                .clip(KeiTheme.shapes.pill)
                .background(
                    if (hoverState.hovered) {
                        KeiTheme.colors.scrollbarThumbHover
                    } else {
                        KeiTheme.colors.scrollbarThumb
                    },
                )
                .hoverable(hoverState.interactionSource)
                .draggable(
                    orientation = orientation,
                    state = rememberDraggableState { delta ->
                        scrollState.dispatchRawDelta(delta * scrollState.maxValue / trackRange)
                    },
                ),
        )
    }
}

@Preview
@Composable
private fun VerticalScrollbarPreview() {
    KeiTheme {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 180.dp)
                .background(KeiTheme.colors.island),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(8) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(KeiTheme.colors.deskChip),
                    )
                }
            }
            VerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Preview
@Composable
private fun HorizontalScrollbarPreview() {
    KeiTheme {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 80.dp)
                .background(KeiTheme.colors.island),
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(width = 96.dp, height = 32.dp)
                            .background(KeiTheme.colors.deskChip),
                    )
                }
            }
            HorizontalScrollbar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}
