@file:Suppress("MagicNumber", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.splash.destination.splash.component

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.prefersReducedMotion
import io.github.kei_1111.app.feature.splash.theme.SplashAnimations
import io.github.kei_1111.app.feature.splash.theme.SplashDimensions
import kotlin.math.abs

/**
 * 幅 30〜45% の緑バーが左から右へ流れ続ける不確定プログレスバー。
 * ビルド失敗時はアニメーションを止め、全幅の赤バーを固定表示する。
 * 幅は呼び出し側の modifier で指定する(デスクトップ: fillMaxWidth / モバイル: 固定幅)。
 */
@Composable
internal fun SplashProgressBar(
    isBuildFailed: Boolean,
    modifier: Modifier = Modifier,
) {
    // 「視覚効果を減らす」設定時はアニメーションを止め、バーを中央に固定表示する
    val isReducedMotion = remember { prefersReducedMotion() }
    val progress: Float
    if (isReducedMotion || isBuildFailed) {
        progress = 0.5f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "SplashProgressBar")
        progress = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = SplashAnimations.ProgressBarCycleMillis,
                    easing = EaseInOut,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "SplashProgressBarFraction",
        ).value
    }

    val colors = KeiTheme.colors
    Box(
        modifier = modifier
            .height(SplashDimensions.ProgressBarHeight)
            .clip(RoundedCornerShape(percent = 50))
            .drawBehind {
                val cornerRadius = CornerRadius(size.height / 2f)
                drawRoundRect(
                    color = colors.splashProgressTrack,
                    cornerRadius = cornerRadius,
                )

                if (isBuildFailed) {
                    drawRoundRect(
                        color = colors.splashProgressBarFailed,
                        cornerRadius = cornerRadius,
                    )
                    return@drawBehind
                }

                val leftFraction = SplashAnimations.ProgressBarStartFraction +
                    (SplashAnimations.ProgressBarEndFraction - SplashAnimations.ProgressBarStartFraction) * progress
                val widthFraction = SplashAnimations.ProgressBarMinWidthFraction +
                    (SplashAnimations.ProgressBarMaxWidthFraction - SplashAnimations.ProgressBarMinWidthFraction) *
                    (1f - abs(2f * progress - 1f))
                drawRoundRect(
                    color = colors.splashProgressBar,
                    topLeft = Offset(size.width * leftFraction, 0f),
                    size = Size(size.width * widthFraction, size.height),
                    cornerRadius = cornerRadius,
                )
            },
    )
}

@Preview
@Composable
private fun SplashProgressBarPreview() {
    KeiTheme {
        SplashProgressBar(
            isBuildFailed = false,
            modifier = Modifier
                .padding(8.dp)
                .width(200.dp),
        )
    }
}
