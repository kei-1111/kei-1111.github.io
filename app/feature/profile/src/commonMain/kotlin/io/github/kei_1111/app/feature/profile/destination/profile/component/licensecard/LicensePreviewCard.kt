@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component.licensecard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.KeiThemeController
import io.github.kei_1111.app.feature.profile.destination.profile.component.githubcard.SectionLabel
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewThirdPartyLicenses
import io.github.kei_1111.app.feature.profile.theme.ProfileAnimations
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.rememberHoverState
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.LicenseType
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.ic_license
import kei_1111.app.feature.profile.generated.resources.ic_license_light
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource

/**
 * サードパーティライセンス型の縦長プレビューカード（280x600）。
 * [licenses] が未到着（null）の間はヘッダのみ描画する（licenses は flowOf で即時発行されるため実質発生しない）。
 * 行タップで選択したライセンスの全文シート（[LicenseSheetOverlay]）をカード内に重ねて表示する。
 */
@Composable
internal fun LicensePreviewCard(
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(ProfileDimensions.LicenseCardWidth)
            .height(ProfileDimensions.LicenseCardHeight)
            // GitHubPreviewCard と同じ角の立った矩形。clipToBounds はシートのスライドをカード境界でマスクする
            .clipToBounds()
            .background(KeiTheme.colors.cardBackground)
            .border(1.dp, KeiTheme.colors.outline),
    ) {
        CardContent(
            licenses = licenses,
            selectedLicense = selectedLicense,
            onClickLicense = onClickLicense,
            modifier = Modifier.fillMaxSize(),
        )
        if (licenses != null) {
            LicenseSheetOverlay(
                license = selectedLicense,
                texts = licenses.texts,
                onDismiss = onDismissLicense,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
private fun CardContent(
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    onClickLicense: (LicenseEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CardHeader(
            modifier = Modifier.padding(
                start = ProfileDimensions.LicenseCardPadding,
                top = 16.dp,
                end = ProfileDimensions.LicenseCardPadding,
                bottom = 10.dp,
            ),
        )
        if (licenses != null) {
            SectionList(
                licenses = licenses,
                selectedLicense = selectedLicense,
                onClickLicense = onClickLicense,
                modifier = Modifier.weight(1f),
            )
            NonAffiliationNote(
                modifier = Modifier.padding(
                    start = ProfileDimensions.LicenseCardPadding,
                    end = ProfileDimensions.LicenseCardPadding,
                    top = 10.dp,
                    bottom = 12.dp,
                ),
            )
        }
    }
}

/** ICONS / FONTS / APP / SERVER のセクション一覧。全件表示のためカード内で縦スクロールする。 */
@Composable
private fun SectionList(
    licenses: ThirdPartyLicenses,
    selectedLicense: LicenseEntry?,
    onClickLicense: (LicenseEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = listOf(
        "ICONS" to licenses.icons,
        "FONTS" to licenses.fonts,
        "APP" to licenses.app,
        "SERVER" to licenses.server,
    )
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ProfileDimensions.LicenseCardPadding),
        verticalArrangement = Arrangement.spacedBy(ProfileDimensions.GitHubCardSectionGap),
    ) {
        sections.forEach { (title, entries) ->
            LicenseSection(
                title = title,
                entries = entries,
                selectedLicense = selectedLicense,
                onClickLicense = onClickLicense,
            )
        }
    }
}

@Composable
private fun CardHeader(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(KeiTheme.shapes.card)
                .background(KeiTheme.colors.gitHubItem),
            contentAlignment = Alignment.Center,
        ) {
            // 実 IntelliJ New UI の Documentation ツールウィンドウアイコン（expui/toolwindows/documentation）。
            // 明暗で焼き込み色が異なるためテーマに応じて切り替え、tint はかけない
            Icon(
                painter = painterResource(
                    if (KeiThemeController.isDark) Res.drawable.ic_license else Res.drawable.ic_license_light,
                ),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Licenses",
                style = KeiTheme.typography.chrome.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = KeiTheme.colors.textPrimary,
                ),
            )
            Text(
                text = "このサイトで使用しているOSS",
                style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.syntaxString),
            )
        }
    }
}

@Composable
private fun LicenseSection(
    title: String,
    entries: ImmutableList<LicenseEntry>,
    selectedLicense: LicenseEntry?,
    onClickLicense: (LicenseEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SectionLabel(text = title)
        entries.forEach { entry ->
            LicenseRow(
                entry = entry,
                selected = entry == selectedLicense,
                onClickLicense = onClickLicense,
            )
        }
    }
}

@Composable
private fun LicenseRow(
    entry: LicenseEntry,
    selected: Boolean,
    onClickLicense: (LicenseEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    val background by animateColorAsState(
        targetValue = if (selected || hoverState.hovered) KeiTheme.colors.gitHubItemHover else KeiTheme.colors.gitHubItem,
        animationSpec = tween(ProfileAnimations.HoverTransitionMillis),
    )
    val borderColor = if (selected) KeiTheme.colors.selectionPill else Color.Transparent
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(KeiTheme.shapes.githubItem)
            .background(background)
            .border(1.dp, borderColor, KeiTheme.shapes.githubItem)
            .hoverable(hoverState.interactionSource)
            .clickable { onClickLicense(entry) }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = entry.name,
                style = KeiTheme.typography.chrome.copy(fontSize = 10.sp, color = KeiTheme.colors.syntaxLink),
            )
            Text(
                text = entry.owner,
                style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.textSecondary),
            )
        }
        LicenseBadge(type = entry.type)
    }
}

@Composable
private fun LicenseBadge(
    type: LicenseType,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(KeiTheme.colors.licenseBadge)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = type.id,
            style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = KeiTheme.colors.syntaxString),
        )
    }
}

@Composable
private fun NonAffiliationNote(modifier: Modifier = Modifier) {
    Text(
        text = "Not affiliated with or endorsed by Google or JetBrains.",
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = KeiTheme.colors.mutedHigh),
    )
}

@Preview
@Composable
private fun LicensePreviewCardPreview() {
    KeiTheme {
        LicensePreviewCard(
            licenses = PreviewThirdPartyLicenses,
            selectedLicense = null,
            onClickLicense = {},
            onDismissLicense = {},
        )
    }
}
