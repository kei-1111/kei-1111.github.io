@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme
import io.github.kei_1111.app.core.utils.prefersReducedMotion

private const val BAND_WIDTH_RATIO = 0.4f

/**
 * [Modifier.skeletonShimmer] の掃引進捗を複数バー間で共有するための状態。
 * バーごとに個別の InfiniteTransition を持つと位相がずれるため、スケルトン1個につき
 * 1つだけ [rememberSkeletonShimmer] で生成し、各バーへ渡す。
 */
internal class SkeletonShimmer internal constructor(
    internal val progress: State<Float>,
    internal val isStatic: Boolean,
)

/**
 * スケルトン1個ぶんの掃引アニメーションを保持する（減モーション時は静止）。
 * [animated] が false のとき（取得失敗などで進行していないとき）も静止させる。
 */
@Composable
internal fun rememberSkeletonShimmer(animated: Boolean = true): SkeletonShimmer {
    val reducedMotion = remember { prefersReducedMotion() }
    val isStatic = reducedMotion || !animated
    val progress = if (isStatic) {
        rememberUpdatedState(0f)
    } else {
        rememberInfiniteTransition(label = "SkeletonShimmer").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(ProfileAnimations.ShimmerCycleMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "SkeletonShimmerProgress",
        )
    }
    return SkeletonShimmer(progress = progress, isStatic = isStatic)
}

/**
 * シマー骨組みバー1本を描画する： [KeiColorScheme.skeletonBone] の下地に
 * [shimmer] の進捗で左→右へ掃引する [KeiColorScheme.skeletonHighlight] の帯を重ねる。
 * 減モーション時は帯なしの静止表示。
 */
internal fun Modifier.skeletonShimmer(
    shimmer: SkeletonShimmer,
    colors: KeiColorScheme,
    shape: Shape,
): Modifier = clip(shape).drawWithCache {
    val bone = colors.skeletonBone
    if (shimmer.isStatic) {
        onDrawBehind { drawRect(bone) }
    } else {
        val highlight = colors.skeletonHighlight
        val bandWidth = size.width * BAND_WIDTH_RATIO
        val travel = size.width + bandWidth
        onDrawBehind {
            drawRect(bone)
            val bandCenter = shimmer.progress.value * travel - bandWidth / 2f
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(bone, highlight, bone),
                    start = Offset(bandCenter - bandWidth / 2f, 0f),
                    end = Offset(bandCenter + bandWidth / 2f, 0f),
                ),
            )
        }
    }
}
