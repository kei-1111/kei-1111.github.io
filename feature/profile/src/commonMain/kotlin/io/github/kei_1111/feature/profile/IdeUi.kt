@file:Suppress("MagicNumber", "MatchingDeclarationName")

package io.github.kei_1111.feature.profile

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
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

/** IDE レイアウト共通の寸法トークン。 */
data object IdeDimens {
    val DeskPadding = 10.dp
    val IslandGap = 7.dp
    val RailWidth = 30.dp // 実 AS 実測: アイコンピル 30px がレール幅いっぱい
    val RailMargin = 5.dp // 実 AS 実測: ウィンドウ端 → ピル左端 5px
    val TreeWidth = 248.dp

    // GitHub プロフィールカード
    val GitHubCardWidth = 280.dp
    val GitHubCardHeight = 600.dp
}
