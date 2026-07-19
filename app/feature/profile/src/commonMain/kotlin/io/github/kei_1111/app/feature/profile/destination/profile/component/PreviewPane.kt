@file:Suppress("MagicNumber", "LongMethod", "ModifierMissing", "TooManyFunctions", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ThemedIcon
import io.github.kei_1111.app.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.app.feature.profile.destination.profile.component.githubcard.GitHubPreviewCard
import io.github.kei_1111.app.feature.profile.destination.profile.component.licensecard.LicensePreviewCard
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewContributionCalendar
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewThirdPartyLicenses
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlin.math.roundToInt

/** Fit 表示時の最大拡大率。ペイン幅が許す範囲でここまで等倍拡大する。 */
private const val PREVIEW_MAX_SCALE = 1.5f

/** ズームボタン1回あたりの倍率変化。 */
private const val ZOOM_STEP = 1.25f
private const val MIN_ZOOM = 0.25f
private const val MAX_ZOOM = 3f

/** 名前行の chevron の張り出し幅（chevron 16dp + 間隔 6dp）。名前テキストとカード左端を揃える。 */
private val NAME_CHEVRON_OUTDENT = 22.dp

/**
 * 島2の右半分：Compose Preview ペイン。コード(左)と対応する内容を描画する。
 * [fitToWidth] を true にすると Fit 時に高さを無視して幅のみに合わせる
 * （Mobile 用。縦方向はペインの縦スクロールで見る前提）。
 */
@Composable
internal fun PreviewPane(
    page: EditorPage,
    profile: GitHubProfile,
    contributions: ContributionCalendar?,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    modifier: Modifier = Modifier,
    fitToWidth: Boolean = false,
    upToDate: Boolean = true,
) {
    // null = Fit（ペイン幅に合わせる）。値があれば手動ズーム倍率。
    var fixedScale by remember { mutableStateOf<Float?>(null) }
    var effectiveScale by remember { mutableFloatStateOf(1f) }

    Column(modifier = modifier.fillMaxSize()) {
        PreviewHeader(upToDate = upToDate)
        PreviewViewport(
            page = page,
            profile = profile,
            contributions = contributions,
            licenses = licenses,
            selectedLicense = selectedLicense,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            fixedScale = fixedScale,
            fitToWidth = fitToWidth,
            effectiveScale = effectiveScale,
            onChangeEffectiveScale = { if (effectiveScale != it) effectiveScale = it },
            onClickZoomIn = { fixedScale = (effectiveScale * ZOOM_STEP).coerceAtMost(MAX_ZOOM) },
            onClickZoomOut = { fixedScale = (effectiveScale / ZOOM_STEP).coerceAtLeast(MIN_ZOOM) },
            onClickFit = { fixedScale = null },
        )
    }
}

/** ヘッダ下の、ズーム可能な表示領域。スクロール表示にズームコントロールを重ねる。 */
@Composable
private fun PreviewViewport(
    page: EditorPage,
    profile: GitHubProfile,
    contributions: ContributionCalendar?,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    fixedScale: Float?,
    fitToWidth: Boolean,
    effectiveScale: Float,
    onChangeEffectiveScale: (Float) -> Unit,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onClickFit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = this.maxWidth - 32.dp
        val availableHeight = this.maxHeight - 16.dp
        PreviewScrollArea(
            page = page,
            profile = profile,
            contributions = contributions,
            licenses = licenses,
            selectedLicense = selectedLicense,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            fixedScale = fixedScale,
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            fitToWidth = fitToWidth,
            onChangeEffectiveScale = onChangeEffectiveScale,
        )
        ZoomControls(
            scalePercent = (effectiveScale * 100).roundToInt(),
            onClickZoomIn = onClickZoomIn,
            onClickZoomOut = onClickZoomOut,
            onClickFit = onClickFit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
        )
    }
}

/** 拡大したプレビューをスクロールして見る領域。 */
@Composable
private fun PreviewScrollArea(
    page: EditorPage,
    profile: GitHubProfile,
    contributions: ContributionCalendar?,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    fixedScale: Float?,
    availableWidth: Dp,
    availableHeight: Dp,
    fitToWidth: Boolean,
    onChangeEffectiveScale: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        ZoomedPreview(
            page = page,
            profile = profile,
            contributions = contributions,
            licenses = licenses,
            selectedLicense = selectedLicense,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            fixedScale = fixedScale,
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            fitToWidth = fitToWidth,
            onChangeEffectiveScale = onChangeEffectiveScale,
        )
    }
}

/**
 * プレビュー名の行・タイトル行（いずれも等倍のまま）と、等倍拡大したプレビューカードを
 * 縦に並べるレイアウト。全体がペインのスクロール対象になる。
 * カードは自然サイズで測定し、[fixedScale]（null なら [availableWidth] と [availableHeight] の
 * 両方に収まる Fit 倍率）で拡大表示する。縦方向は2つの行と隙間のぶんを差し引いて計算するため、
 * 初期表示でカード全体がペイン内に収まる。
 * [fitToWidth] が true の Fit は高さを無視して幅のみに合わせる（縦はペインのスクロールで見る）。
 * 各行の幅は拡大後のカード幅に合わせ、文字サイズは変えない。
 * 適用した倍率は [onChangeEffectiveScale] で通知する（ズームコントロールの％表示用）。
 */
@Composable
private fun ZoomedPreview(
    page: EditorPage,
    profile: GitHubProfile,
    contributions: ContributionCalendar?,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    fixedScale: Float?,
    availableWidth: Dp,
    availableHeight: Dp,
    fitToWidth: Boolean,
    onChangeEffectiveScale: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            PreviewNameRow(page = page)
            PreviewCardTitleRow()
            PreviewCard(
                page = page,
                profile = profile,
                contributions = contributions,
                licenses = licenses,
                selectedLicense = selectedLicense,
                onClickUrl = onClickUrl,
                onClickLicense = onClickLicense,
                onDismissLicense = onDismissLicense,
            )
        },
        modifier = modifier,
    ) { measurables, _ ->
        val (nameMeasurable, titleMeasurable, cardMeasurable) = measurables
        val card = cardMeasurable.measure(Constraints())
        val gap = 6.dp.roundToPx()
        // 実 AS と同様、名前行の chevron は左に張り出し、名前テキストとカード左端が揃う
        val indent = NAME_CHEVRON_OUTDENT.roundToPx()
        // 名前行・タイトル行はズームの影響を受けないため、intrinsic 測定で先に高さだけ得て縦 Fit の計算に使う
        val nameHeight = nameMeasurable.minIntrinsicHeight(availableWidth.roundToPx())
        val titleHeight = titleMeasurable.minIntrinsicHeight(availableWidth.roundToPx())
        val usableWidth = availableWidth.toPx() - indent
        val scale = when {
            fixedScale != null -> fixedScale
            card.width == 0 || card.height == 0 -> 1f
            fitToWidth -> minOf(
                PREVIEW_MAX_SCALE,
                usableWidth / card.width,
            ).coerceAtLeast(MIN_ZOOM)
            else -> minOf(
                PREVIEW_MAX_SCALE,
                usableWidth / card.width,
                (availableHeight.toPx() - nameHeight - titleHeight - gap) / card.height,
            ).coerceAtLeast(MIN_ZOOM)
        }
        onChangeEffectiveScale(scale)
        val scaledWidth = (card.width * scale).roundToInt()
        val scaledHeight = (card.height * scale).roundToInt()
        val nameWidth = scaledWidth + indent
        val name = nameMeasurable.measure(Constraints(minWidth = nameWidth, maxWidth = nameWidth))
        val title = titleMeasurable.measure(Constraints(minWidth = scaledWidth, maxWidth = scaledWidth))
        layout(nameWidth, name.height + title.height + gap + scaledHeight) {
            name.placeRelative(0, 0)
            title.placeRelative(indent, name.height)
            card.placeRelativeWithLayer(indent, name.height + title.height + gap) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

/** プレビュー名の行（chevron + プレビュー名）。カードと一緒にスクロールする。 */
@Composable
private fun PreviewNameRow(
    page: EditorPage,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.chevronDown,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = page.previewName,
            style = KeiTheme.typography.chrome.copy(
                fontSize = ProfileDimensions.ChromeLabelFontSize,
                fontWeight = FontWeight.Bold,
                color = KeiTheme.colors.textPrimary,
            ),
        )
    }
}

/** カード直上のタイトル行（プレビュー名 + メニュー）。ズームの影響を受けない。 */
@Composable
private fun PreviewCardTitleRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Dark - parameter 0",
            style = KeiTheme.typography.chrome.copy(fontSize = 11.sp, color = KeiTheme.colors.textSecondary),
        )
        Spacer(modifier = Modifier.weight(1f))
        KeiIcon(
            icon = KeiTheme.icons.moreVertical,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
    }
}

/** ページに対応するプレビューカード（ヘッダ・スクロールを含まない中身のみ）。 */
@Composable
private fun PreviewCard(
    page: EditorPage,
    profile: GitHubProfile,
    contributions: ContributionCalendar?,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (page) {
        EditorPage.Profile -> GitHubPreviewCard(
            profile = profile,
            contributions = contributions,
            onClickUrl = onClickUrl,
            modifier = modifier,
        )

        EditorPage.Licenses -> LicensePreviewCard(
            licenses = licenses,
            selectedLicense = selectedLicense,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            modifier = modifier,
        )
    }
}

/** ペイン最上部のツールバー（実 AS ではタブバーと同じ高さで下に境界線が走る）。 */
@Composable
private fun PreviewHeader(
    upToDate: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderIcon(KeiTheme.icons.layout)
            Spacer(modifier = Modifier.size(8.dp))
            HeaderIcon(KeiTheme.icons.uiCheck)
            Spacer(modifier = Modifier.weight(1f))
            InspectionsStatus(upToDate = upToDate)
        }
        HorizontalDivider(color = KeiTheme.colors.outline, thickness = 1.dp)
    }
}

/** インスペクション状態（チェックアイコン + ラベル）。 */
@Composable
private fun InspectionsStatus(
    upToDate: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeiIcon(
            icon = if (upToDate) KeiTheme.icons.inspectionsOk else KeiTheme.icons.warning,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = if (upToDate) "Up-to-date" else "Out-of-date",
            style = KeiTheme.typography.chrome.copy(
                fontSize = ProfileDimensions.ChromeLabelFontSize,
                color = KeiTheme.colors.textPrimary,
            ),
        )
    }
}

@Composable
private fun HeaderIcon(
    icon: ThemedIcon,
    modifier: Modifier = Modifier,
) {
    KeiIcon(
        icon = icon,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.ChromeIconSize),
    )
}

/** プレビュー右下のズームコントロール（縮小 / 倍率 / 拡大 / Fit）。 */
@Composable
private fun ZoomControls(
    scalePercent: Int,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onClickFit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(KeiTheme.colors.chip)
            .border(1.dp, KeiTheme.colors.outline, KeiTheme.shapes.pill)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ZoomButton(icon = KeiTheme.icons.zoomOut, onClick = onClickZoomOut)
        ZoomPercentLabel(scalePercent = scalePercent)
        ZoomButton(icon = KeiTheme.icons.zoomIn, onClick = onClickZoomIn)
        ZoomButton(icon = KeiTheme.icons.resetZoom, onClick = onClickFit)
    }
}

@Composable
private fun ZoomPercentLabel(
    scalePercent: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$scalePercent%",
        modifier = modifier.widthIn(min = 36.dp),
        style = KeiTheme.typography.chrome.copy(fontSize = 11.sp, color = KeiTheme.colors.textSecondary),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ZoomButton(
    icon: ThemedIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(KeiTheme.shapes.chip)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = icon,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
    }
}

@Preview
@Composable
private fun PreviewPanePreview() {
    KeiTheme {
        // verticalScroll は無限制約下で測定できないため、Preview では有限サイズを与える
        Box(
            modifier = Modifier
                .size(width = 420.dp, height = 640.dp)
                .background(KeiTheme.colors.island),
        ) {
            PreviewPane(
                page = EditorPage.Profile,
                profile = PreviewGitHubProfile,
                contributions = PreviewContributionCalendar,
                licenses = PreviewThirdPartyLicenses,
                selectedLicense = null,
                onClickUrl = {},
                onClickLicense = {},
                onDismissLicense = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewHeaderPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.island)) {
            PreviewHeader(upToDate = true)
        }
    }
}

@Preview
@Composable
private fun ZoomControlsPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.island).padding(8.dp)) {
            ZoomControls(
                scalePercent = 100,
                onClickZoomIn = {},
                onClickZoomOut = {},
                onClickFit = {},
            )
        }
    }
}
