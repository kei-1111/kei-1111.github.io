@file:Suppress("MagicNumber", "MatchingDeclarationName")

package io.github.kei_1111.feature.profile

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.core.designsystem.theme.IdeColors
import io.github.kei_1111.core.designsystem.theme.IdeJapaneseFamily
import io.github.kei_1111.core.designsystem.theme.JetBrainsMonoFamily
import io.github.kei_1111.core.designsystem.theme.ZenKakuGothicNewFamily

/**
 * ウィンドウ背景（デスク）。実 AS と同様、左上にブルーのグローが乗る。
 * グローの中心・半径は実 AS スクリーンショットの実測値（幅比）に合わせている。
 */
internal fun Modifier.deskBackground(): Modifier = drawWithCache {
    val brush = Brush.radialGradient(
        colors = listOf(IdeColors.DeskGlow, IdeColors.Desk),
        center = Offset(size.width * 0.07f, 0f),
        radius = (size.width * 0.36f).coerceAtLeast(1f),
    )
    onDrawBehind {
        drawRect(IdeColors.Desk)
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
    val IslandRadius = 12.dp
    val CardMaxWidth = 320.dp
    val CodeLineHeight = 22.sp // 実 AS 実測: 13px フォントで行ピッチ 22px

    val IslandShape = RoundedCornerShape(IslandRadius)
    val PillShape = RoundedCornerShape(7.dp)
    val ChipShape = RoundedCornerShape(4.dp)
    val RowShape = RoundedCornerShape(7.dp)
    val CardShape = RoundedCornerShape(10.dp)
    val BadgeShape = RoundedCornerShape(3.dp)

    // GitHub プロフィールカード
    val GitHubCardWidth = 280.dp
    val GitHubCardHeight = 600.dp
    val GitHubCardShape = RoundedCornerShape(14.dp)
    val GitHubItemShape = RoundedCornerShape(8.dp)
    val LinkTileShape = RoundedCornerShape(10.dp)
}

/**
 * IDE クローム用の等幅 UI テキスト。
 * 実 AS の UI フォントは Inter 13px だが、等幅の JetBrains Mono は同 px でも光学的に大きく
 * 見えるため、12px を等価サイズとして使う。
 */
@Composable
fun ChromeTextStyle(
    fontSize: Int = 12,
    weight: FontWeight = FontWeight.Normal,
    color: androidx.compose.ui.graphics.Color = IdeColors.TextSecondary,
) = TextStyle(
    fontFamily = JetBrainsMonoFamily(),
    fontWeight = weight,
    fontSize = fontSize.sp,
    color = color,
)

/** コード本文用の等幅スタイル。 */
@Composable
fun CodeTextStyle() = TextStyle(
    fontFamily = JetBrainsMonoFamily(),
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = IdeDimens.CodeLineHeight,
    color = IdeColors.TextCode,
)

/** GitHub プロフィールカード内の日本語テキスト用スタイル（Zen Kaku Gothic New）。 */
@Composable
fun GitHubJpTextStyle(
    fontSize: Int = 9,
    weight: FontWeight = FontWeight.Normal,
    color: androidx.compose.ui.graphics.Color = IdeColors.TextPrimary,
) = TextStyle(
    fontFamily = ZenKakuGothicNewFamily(),
    fontWeight = weight,
    fontSize = fontSize.sp,
    color = color,
)

/** カード内の日本語テキスト用スタイル。 */
@Composable
fun CardTextStyle(
    fontSize: Int = 13,
    weight: FontWeight = FontWeight.Medium,
    color: androidx.compose.ui.graphics.Color = IdeColors.TextPrimary,
) = TextStyle(
    fontFamily = IdeJapaneseFamily(),
    fontWeight = weight,
    fontSize = fontSize.sp,
    color = color,
)
