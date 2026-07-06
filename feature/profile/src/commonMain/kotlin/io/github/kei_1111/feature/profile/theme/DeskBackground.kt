@file:Suppress("MagicNumber")

package io.github.kei_1111.feature.profile.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import io.github.kei_1111.core.designsystem.theme.keiColorScheme

/**
 * ウィンドウ背景（デスク）。実 AS と同様、左上にブルーのグローが乗る。
 * グローの中心・半径は実 AS スクリーンショットの実測値（幅比）に合わせている。
 */
internal fun Modifier.deskBackground(): Modifier = drawWithCache {
    val brush = Brush.radialGradient(
        colors = listOf(keiColorScheme.deskGlow, keiColorScheme.desk),
        center = Offset(size.width * 0.07f, 0f),
        radius = (size.width * 0.36f).coerceAtLeast(1f),
    )
    onDrawBehind {
        drawRect(keiColorScheme.desk)
        drawRect(brush)
    }
}
