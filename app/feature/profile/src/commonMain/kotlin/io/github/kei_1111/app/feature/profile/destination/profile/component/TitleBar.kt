@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.KeiThemeController
import io.github.kei_1111.app.core.designsystem.theme.ProfileIconImage
import org.jetbrains.compose.resources.painterResource

/**
 * タイトルバー。デスクの上に直接、左にプロジェクト名ピル、右にテーマ切替ボタンを置く。
 * デスクからの余白は親が設定する（ライトテーマではデスクにグラデーションは無く、deskGlow は desk と同値）。
 */
@Composable
internal fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectPill()
        Spacer(modifier = Modifier.weight(1f))
        ThemeToggleButton()
    }
}

@Composable
private fun ProjectPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(KeiTheme.colors.deskChip)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(ProfileIconImage),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(18.dp)
                .clip(KeiTheme.shapes.chip),
        )
        Text(
            text = "kei-1111 portfolio",
            style = KeiTheme.typography.chrome.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KeiTheme.colors.textPrimary,
            ),
        )
        KeiIcon(
            icon = KeiTheme.icons.chevronDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ThemeToggleButton(modifier: Modifier = Modifier) {
    val isDark = KeiThemeController.isDark
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(KeiTheme.shapes.pill)
            .background(if (hovered) KeiTheme.colors.deskChip else Color.Transparent)
            .hoverable(interaction)
            .clickable { KeiThemeController.toggle() },
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = if (isDark) KeiTheme.icons.themeLight else KeiTheme.icons.themeDark,
            contentDescription = if (isDark) "ライトモードに切り替え" else "ダークモードに切り替え",
            tint = KeiTheme.colors.mutedHigh,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Preview
@Composable
private fun TitleBarPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .background(KeiTheme.colors.desk)
                .padding(8.dp),
        ) {
            TitleBar()
        }
    }
}
