@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.theme.AppTheme
import io.github.kei_1111.core.designsystem.theme.IdeColors
import io.github.kei_1111.feature.profile.ChromeTextStyle
import io.github.kei_1111.feature.profile.IdeDimens
import io.github.kei_1111.feature.profile.PortfolioContent
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.ic_chevron_down_dark
import org.jetbrains.compose.resources.painterResource

/**
 * タイトルバー。デスク（グラデーション領域）の上に直接、左にプロジェクト名ピルのみを置く。
 * デスクからの余白は親が設定する。
 */
@Composable
internal fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectPill()
    }
}

@Composable
private fun ProjectPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(IdeDimens.PillShape)
            .background(IdeColors.DeskChip)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(PortfolioContent.profileIcon),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(18.dp)
                .clip(IdeDimens.ChipShape),
        )
        Text(
            text = "kei-1111 portfolio",
            style = ChromeTextStyle(
                fontSize = 12,
                weight = FontWeight.Bold,
                color = IdeColors.TextPrimary,
            ),
        )
        Icon(
            painter = painterResource(Res.drawable.ic_chevron_down_dark),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Unspecified,
        )
    }
}

@Preview
@Composable
private fun TitleBarPreview() {
    AppTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(IdeColors.Desk)
                .padding(8.dp),
        ) {
            TitleBar()
        }
    }
}
