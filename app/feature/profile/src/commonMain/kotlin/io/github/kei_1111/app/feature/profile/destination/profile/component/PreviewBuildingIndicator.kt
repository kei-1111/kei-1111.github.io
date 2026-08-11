@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.prefersReducedMotion
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileAnimations
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage

private const val SPINNER_SWEEP_ANGLE = 100f
private const val BUILDING_BAR_BAND_RATIO = 0.3f
private const val BUILDING_BAR_STATIC_FRACTION = 0.5f

/**
 * 選択ページのデータ取得待ちのあいだ Preview ペインへ表示する「ビルド中」インジケータ。
 * 実 AS の Compose Preview 再ビルド待ち表示を模す（スピナー + ラベル + 不定進捗バー）。
 */
@Composable
internal fun PreviewBuildingIndicator(
    page: EditorPage,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = remember { prefersReducedMotion() }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BuildingSpinner(reducedMotion = reducedMotion)
            BuildingLabel(page = page)
            BuildingIndeterminateBar(reducedMotion = reducedMotion)
        }
    }
}

@Composable
private fun BuildingLabel(
    page: EditorPage,
    modifier: Modifier = Modifier,
) {
    Text(
        text = page.previewName?.let { "Building $it…" } ?: "Rendering ${page.fileName}…",
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(
            fontSize = ProfileDimensions.ChromeLabelFontSize,
            color = KeiTheme.colors.textSecondary,
        ),
    )
}

@Composable
private fun BuildingSpinner(
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = KeiTheme.colors.focusBorder
    // 毎フレームの再コンポーズを避けるため State のまま持ち、draw 時に読む（rememberCaretBlink と同じ方針）
    val angle = spinnerAngle(reducedMotion)
    Canvas(modifier = modifier.size(ProfileDimensions.PreviewSpinnerSize)) {
        rotate(angle.value) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = SPINNER_SWEEP_ANGLE,
                useCenter = false,
                style = Stroke(width = ProfileDimensions.PreviewSpinnerStrokeWidth.toPx()),
            )
        }
    }
}

@Composable
private fun spinnerAngle(reducedMotion: Boolean): State<Float> {
    if (reducedMotion) return rememberUpdatedState(0f)
    return rememberInfiniteTransition(label = "PreviewSpinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(ProfileAnimations.PreviewSpinnerCycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PreviewSpinnerAngle",
    )
}

@Composable
private fun BuildingIndeterminateBar(
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val track = KeiTheme.colors.outline
    val active = KeiTheme.colors.focusBorder
    Box(
        modifier = modifier
            .width(ProfileDimensions.PreviewBuildingBarWidth)
            .height(ProfileDimensions.PreviewBuildingBarHeight)
            .clip(KeiTheme.shapes.chip)
            .background(track),
    ) {
        if (reducedMotion) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(BUILDING_BAR_STATIC_FRACTION)
                    .background(active),
            )
        } else {
            // 毎フレームの再コンポーズを避けるため State のまま持ち、offset のラムダ（layout 時）で読む
            val progress = buildingBarProgress()
            val bandWidth = ProfileDimensions.PreviewBuildingBarWidth * BUILDING_BAR_BAND_RATIO
            val travel = ProfileDimensions.PreviewBuildingBarWidth + bandWidth
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = ((travel * progress.value) - bandWidth).roundToPx(), y = 0) }
                    .width(bandWidth)
                    .fillMaxHeight()
                    .background(active),
            )
        }
    }
}

@Composable
private fun buildingBarProgress(): State<Float> =
    rememberInfiniteTransition(label = "PreviewBuildingBar").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(ProfileAnimations.PreviewBuildingBarCycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PreviewBuildingBarProgress",
    )

@Preview
@Composable
private fun PreviewBuildingIndicatorPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 200.dp)
                .background(KeiTheme.colors.island),
        ) {
            PreviewBuildingIndicator(page = EditorPage.Profile)
        }
    }
}

@Preview
@Composable
private fun PreviewBuildingIndicatorReadmePreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 200.dp)
                .background(KeiTheme.colors.island),
        ) {
            PreviewBuildingIndicator(page = EditorPage.Readme)
        }
    }
}
