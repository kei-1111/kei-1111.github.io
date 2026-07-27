@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ProfileIconImage
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.language_toggle
import kei_1111.app.feature.profile.generated.resources.theme_toggle_to_dark
import kei_1111.app.feature.profile.generated.resources.theme_toggle_to_light
import kei_1111.app.feature.profile.generated.resources.title_bar_build
import kei_1111.app.feature.profile.generated.resources.title_bar_search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * タイトルバー。デスクの上に直接、左にプロジェクト名ピル、右にビルド・検索・言語・テーマ切替ボタンを置く。
 * デスクからの余白は親が設定する。
 * ライトテーマではデスクにグラデーションは無く、deskGlow は desk と同値。
 */
@Composable
internal fun TitleBar(
    onClickToggleTheme: () -> Unit,
    onClickToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier,
    languageToggleEnabled: Boolean = true,
    onClickBuild: (() -> Unit)? = null,
    onClickSearch: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectPill()
        Spacer(modifier = Modifier.weight(1f))
        if (onClickBuild != null) {
            BuildButton(onClick = onClickBuild)
        }
        if (onClickSearch != null) {
            SearchButton(onClick = onClickSearch)
        }
        LanguageToggleButton(onClick = onClickToggleLanguage, enabled = languageToggleEnabled)
        ThemeToggleButton(onClick = onClickToggleTheme)
    }
}

@Composable
private fun SearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChromeIconButton(
        icon = KeiTheme.icons.search,
        contentDescription = stringResource(Res.string.title_bar_search),
        modifier = modifier,
        iconSize = ProfileDimensions.TitleBarIconSize,
        onClick = onClick,
    )
}

@Composable
private fun BuildButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChromeIconButton(
        icon = KeiTheme.icons.build,
        contentDescription = stringResource(Res.string.title_bar_build),
        modifier = modifier,
        iconSize = ProfileDimensions.TitleBarIconSize,
        onClick = onClick,
    )
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
                .size(ProfileDimensions.TitleBarIconSize)
                .clip(KeiTheme.shapes.chip),
        )
        Text(
            text = "kei-1111 portfolio",
            style = KeiTheme.typography.chrome.copy(
                fontSize = ProfileDimensions.ChromeLabelFontSize,
                fontWeight = FontWeight.Bold,
                color = KeiTheme.colors.textPrimary,
            ),
        )
        KeiIcon(
            icon = KeiTheme.icons.chevronDown,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
    }
}

@Composable
private fun ThemeToggleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = KeiTheme.colors.isDark
    ChromeIconButton(
        icon = if (isDark) KeiTheme.icons.themeLight else KeiTheme.icons.themeDark,
        contentDescription = stringResource(
            if (isDark) Res.string.theme_toggle_to_light else Res.string.theme_toggle_to_dark,
        ),
        modifier = modifier.testTag(TestTags.Profile.TITLE_BAR_THEME_TOGGLE),
        iconSize = ProfileDimensions.TitleBarIconSize,
        onClick = onClick,
    )
}

/** [enabled] が false の間（編集済みバッファがあり言語切替が追従しない間）は非活性表示になる。 */
@Composable
private fun LanguageToggleButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    ChromeIconButton(
        icon = KeiTheme.icons.translate,
        contentDescription = stringResource(Res.string.language_toggle),
        modifier = modifier.testTag(TestTags.Profile.TITLE_BAR_LANGUAGE_TOGGLE),
        enabled = enabled,
        iconSize = ProfileDimensions.TitleBarIconSize,
        onClick = onClick,
    )
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
            TitleBar(onClickToggleTheme = {}, onClickToggleLanguage = {}, onClickBuild = {}, onClickSearch = {})
        }
    }
}
