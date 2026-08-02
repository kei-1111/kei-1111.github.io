@file:Suppress("MagicNumber", "LongMethod", "ModifierMissing", "TooManyFunctions", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageController
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ThemedIcon
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.core.utils.prefersReducedMotion
import io.github.kei_1111.app.feature.profile.destination.profile.component.githubcard.GitHubPreviewCard
import io.github.kei_1111.app.feature.profile.destination.profile.component.licensecard.LicensePreviewCard
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownBlock
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.MarkdownPreviewPane
import io.github.kei_1111.app.feature.profile.destination.profile.component.workscard.WorksPreviewCard
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewContributionCalendar
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewThirdPartyLicenses
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewWorks
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileAnimations
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.preview_actual_size
import kei_1111.app.feature.profile.generated.resources.preview_expand_to_fit
import kei_1111.app.feature.profile.generated.resources.preview_retry
import kei_1111.app.feature.profile.generated.resources.preview_zoom_in
import kei_1111.app.feature.profile.generated.resources.preview_zoom_out
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** Fit 表示時の最大拡大率。ペイン幅が許す範囲でここまで等倍拡大する。 */
private const val PREVIEW_MAX_SCALE = 1.5f

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
    profile: GitHubProfile?,
    contributions: ContributionCalendar?,
    licenses: ThirdPartyLicenses?,
    works: ImmutableList<Work>?,
    selectedLicense: LicenseEntry?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onClickRetry: () -> Unit,
    modifier: Modifier = Modifier,
    fitToWidth: Boolean = false,
    upToDate: Boolean = true,
    profileLoadFailed: Boolean = false,
    contributionsLoadFailed: Boolean = false,
    worksLoadFailed: Boolean = false,
    readmeBlocks: ImmutableList<MarkdownBlock> = readmeBlocks(KeiLanguageController.language),
) {
    // null = Fit（ペイン幅に合わせる）。値があれば手動ズーム倍率。
    // README への切り替えでズームが失われないよう、Readme 分岐より先に remember する。
    var fixedScale by remember { mutableStateOf<Float?>(null) }
    var effectiveScale by remember { mutableFloatStateOf(1f) }

    // Compose Preview を持たない README は IntelliJ 風 Markdown プレビューへ委譲する
    if (page == EditorPage.Readme) {
        MarkdownPreviewPane(
            blocks = readmeBlocks,
            onClickUrl = onClickUrl,
            modifier = modifier,
        )
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        PreviewHeader(upToDate = upToDate)
        PreviewBody(
            page = page,
            profile = profile,
            profileLoadFailed = profileLoadFailed,
            contributions = contributions,
            contributionsFailed = contributionsLoadFailed,
            licenses = licenses,
            selectedLicense = selectedLicense,
            works = works,
            worksLoadFailed = worksLoadFailed,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            onClickRetry = onClickRetry,
            fixedScale = fixedScale,
            fitToWidth = fitToWidth,
            onChangeEffectiveScale = { if (effectiveScale != it) effectiveScale = it },
            onClickZoomIn = { fixedScale = (effectiveScale * ZOOM_STEP).coerceAtMost(MAX_ZOOM) },
            onClickZoomOut = { fixedScale = (effectiveScale / ZOOM_STEP).coerceAtLeast(MIN_ZOOM) },
            onClickActualSize = { fixedScale = 1f },
            onClickFit = { fixedScale = null },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }
}

/** ライセンスページは常に Ready。Profile / Works は取得状態に応じて遷移する。 */
private enum class PreviewPhase { Loading, Failed, Ready }

/** 選択ページのデータが未到着かどうか。phase 計算と Ready フェーズの空描画ガードで共有する。 */
private fun awaitingPageData(page: EditorPage, profile: GitHubProfile?, works: ImmutableList<Work>?): Boolean =
    when (page) {
        EditorPage.Profile -> profile == null
        EditorPage.Works -> works == null
        EditorPage.Readme, EditorPage.Licenses -> false
    }

@Composable
private fun PreviewBody(
    page: EditorPage,
    profile: GitHubProfile?,
    profileLoadFailed: Boolean,
    contributions: ContributionCalendar?,
    contributionsFailed: Boolean,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    works: ImmutableList<Work>?,
    worksLoadFailed: Boolean,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onClickRetry: () -> Unit,
    fixedScale: Float?,
    fitToWidth: Boolean,
    onChangeEffectiveScale: (Float) -> Unit,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onClickActualSize: () -> Unit,
    onClickFit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loadFailed = if (page == EditorPage.Works) worksLoadFailed else profileLoadFailed
    val phase = when {
        !awaitingPageData(page, profile, works) -> PreviewPhase.Ready
        loadFailed -> PreviewPhase.Failed
        else -> PreviewPhase.Loading
    }
    val isReducedMotion = remember { prefersReducedMotion() }
    Crossfade(
        targetState = phase,
        animationSpec = tween(if (isReducedMotion) 0 else ProfileAnimations.ContentCrossfadeMillis),
        modifier = modifier,
    ) { currentPhase ->
        when (currentPhase) {
            PreviewPhase.Loading -> PreviewBuildingIndicator(modifier = Modifier.fillMaxSize())
            PreviewPhase.Failed -> PreviewBuildingFailed(
                onClickRetry = onClickRetry,
                modifier = Modifier.fillMaxSize(),
            )

            // クロスフェードの退場側は古い phase のまま最新の page / profile / works を読むため、
            // データ未到着ページの組み合わせが遷移中だけ生じる。そのまま進むと
            // PreviewCard が子を emit せず ZoomedPreview の3要素分配が崩れて落ちるので、空を描く。
            PreviewPhase.Ready -> if (awaitingPageData(page, profile, works)) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                PreviewViewport(
                    page = page,
                    profile = profile,
                    contributions = contributions,
                    contributionsFailed = contributionsFailed,
                    licenses = licenses,
                    selectedLicense = selectedLicense,
                    works = works,
                    onClickUrl = onClickUrl,
                    onClickLicense = onClickLicense,
                    onDismissLicense = onDismissLicense,
                    onClickRetryContributions = onClickRetry,
                    fixedScale = fixedScale,
                    fitToWidth = fitToWidth,
                    onChangeEffectiveScale = onChangeEffectiveScale,
                    onClickZoomIn = onClickZoomIn,
                    onClickZoomOut = onClickZoomOut,
                    onClickActualSize = onClickActualSize,
                    onClickFit = onClickFit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun PreviewBuildingFailed(
    onClickRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KeiIcon(
                icon = KeiTheme.icons.warning,
                contentDescription = null,
                modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "failed — ",
                style = KeiTheme.typography.chrome.copy(
                    fontSize = ProfileDimensions.ChromeLabelFontSize,
                    color = KeiTheme.colors.textSecondary,
                ),
            )
            Text(
                text = "retry",
                modifier = Modifier
                    .testTag(TestTags.Profile.PREVIEW_RETRY)
                    .clickable(
                        onClickLabel = stringResource(Res.string.preview_retry),
                        onClick = onClickRetry,
                    ),
                style = KeiTheme.typography.chrome.copy(
                    fontSize = ProfileDimensions.ChromeLabelFontSize,
                    color = KeiTheme.colors.syntaxLink,
                    textDecoration = TextDecoration.Underline,
                ),
            )
        }
    }
}

@Composable
private fun PreviewViewport(
    page: EditorPage,
    profile: GitHubProfile?,
    contributions: ContributionCalendar?,
    contributionsFailed: Boolean,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    works: ImmutableList<Work>?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onClickRetryContributions: () -> Unit,
    fixedScale: Float?,
    fitToWidth: Boolean,
    onChangeEffectiveScale: (Float) -> Unit,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onClickActualSize: () -> Unit,
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
            contributionsFailed = contributionsFailed,
            licenses = licenses,
            selectedLicense = selectedLicense,
            works = works,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            onClickRetryContributions = onClickRetryContributions,
            fixedScale = fixedScale,
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            fitToWidth = fitToWidth,
            onChangeEffectiveScale = onChangeEffectiveScale,
        )
        ZoomControls(
            onClickZoomIn = onClickZoomIn,
            onClickZoomOut = onClickZoomOut,
            onClickActualSize = onClickActualSize,
            onClickFit = onClickFit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
        )
    }
}

@Composable
private fun PreviewScrollArea(
    page: EditorPage,
    profile: GitHubProfile?,
    contributions: ContributionCalendar?,
    contributionsFailed: Boolean,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    works: ImmutableList<Work>?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onClickRetryContributions: () -> Unit,
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
            contributionsFailed = contributionsFailed,
            licenses = licenses,
            selectedLicense = selectedLicense,
            works = works,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            onClickRetryContributions = onClickRetryContributions,
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
 * 適用した倍率は [onChangeEffectiveScale] で通知する（ズームボタンの倍率計算用）。
 */
@Composable
private fun ZoomedPreview(
    page: EditorPage,
    profile: GitHubProfile?,
    contributions: ContributionCalendar?,
    contributionsFailed: Boolean,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    works: ImmutableList<Work>?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onClickRetryContributions: () -> Unit,
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
                contributionsFailed = contributionsFailed,
                licenses = licenses,
                selectedLicense = selectedLicense,
                works = works,
                onClickUrl = onClickUrl,
                onClickLicense = onClickLicense,
                onDismissLicense = onDismissLicense,
                onClickRetryContributions = onClickRetryContributions,
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

/** カードと一緒にスクロールする。 */
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
            modifier = Modifier
                .size(ProfileDimensions.ChromeIconSize)
                .alpha(KeiTheme.colors.nonClickableAlpha),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = page.previewName.orEmpty(),
            style = KeiTheme.typography.chrome.copy(
                fontSize = ProfileDimensions.ChromeLabelFontSize,
                fontWeight = FontWeight.Bold,
                color = KeiTheme.colors.textPrimary,
            ),
        )
    }
}

/** ズームの影響を受けない。 */
@Composable
private fun PreviewCardTitleRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (KeiTheme.colors.isDark) "Dark - parameter 0" else "Light - parameter 0",
            style = KeiTheme.typography.chrome.copy(fontSize = 11.sp, color = KeiTheme.colors.textSecondary),
        )
        Spacer(modifier = Modifier.weight(1f))
        KeiIcon(
            icon = KeiTheme.icons.moreVertical,
            contentDescription = null,
            modifier = Modifier
                .size(ProfileDimensions.ChromeIconSize)
                .alpha(KeiTheme.colors.nonClickableAlpha),
        )
    }
}

@Composable
private fun PreviewCard(
    page: EditorPage,
    profile: GitHubProfile?,
    contributions: ContributionCalendar?,
    contributionsFailed: Boolean,
    licenses: ThirdPartyLicenses?,
    selectedLicense: LicenseEntry?,
    works: ImmutableList<Work>?,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onClickRetryContributions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (page) {
        // README は PreviewPane 冒頭で MarkdownPreviewPane に分岐するため、ここには到達しない
        EditorPage.Readme -> Unit

        // 呼び出し元（PreviewPane の Ready フェーズ）が profile != null を保証している
        EditorPage.Profile -> profile?.let {
            GitHubPreviewCard(
                profile = it,
                contributions = contributions,
                contributionsFailed = contributionsFailed,
                onClickUrl = onClickUrl,
                onClickRetry = onClickRetryContributions,
                modifier = modifier,
            )
        }

        // 呼び出し元（PreviewBody の Ready フェーズ）が works != null を保証している
        EditorPage.Works -> works?.let {
            WorksPreviewCard(
                works = it,
                onClickUrl = onClickUrl,
                modifier = modifier,
            )
        }

        EditorPage.Licenses -> LicensePreviewCard(
            licenses = licenses,
            selectedLicense = selectedLicense,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            modifier = modifier,
        )
    }
}

/** 実 AS ではタブバーと同じ高さで下に境界線が走る。 */
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
        modifier = modifier
            .size(ProfileDimensions.ChromeIconSize)
            .alpha(KeiTheme.colors.nonClickableAlpha),
    )
}

/** 実 AS の Compose Preview と同じ縦積み構成。 */
@Composable
private fun ZoomControls(
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onClickActualSize: () -> Unit,
    onClickFit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ProfileDimensions.ZoomControlGroupGap),
    ) {
        ZoomControlGroup {
            PanIndicator()
        }
        ZoomControlGroup {
            ZoomButton(
                icon = KeiTheme.icons.zoomIn,
                contentDescription = stringResource(Res.string.preview_zoom_in),
                onClick = onClickZoomIn,
            )
            ZoomButton(
                icon = KeiTheme.icons.zoomOut,
                contentDescription = stringResource(Res.string.preview_zoom_out),
                onClick = onClickZoomOut,
            )
            ActualSizeButton(onClick = onClickActualSize)
            ZoomButton(
                icon = KeiTheme.icons.expandToFit,
                contentDescription = stringResource(Res.string.preview_expand_to_fit),
                onClick = onClickFit,
            )
        }
    }
}

@Composable
private fun ZoomControlGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(KeiTheme.colors.chip)
            .border(1.dp, KeiTheme.colors.outline, KeiTheme.shapes.pill),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

/** 実 AS のパンモード切替を模す。 */
@Composable
private fun PanIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(ProfileDimensions.ZoomControlButtonSize)
            .alpha(KeiTheme.colors.nonClickableAlpha),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.pan,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
    }
}

@Composable
private fun ZoomButton(
    icon: ThemedIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ZoomControlButton(onClick = onClick, modifier = modifier) {
        KeiIcon(
            icon = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
    }
}

@Composable
private fun ActualSizeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 表示テキスト「1:1」ではアクセシブルネームとして意味が伝わらないため、ラベルを上書きする
    val actualSizeDescription = stringResource(Res.string.preview_actual_size)
    ZoomControlButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = actualSizeDescription },
    ) {
        Text(
            text = "1:1",
            style = KeiTheme.typography.chrome.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = KeiTheme.colors.textSecondary,
            ),
        )
    }
}

@Composable
private fun ZoomControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val hoverState = rememberHoverState()
    Box(
        modifier = modifier
            .size(ProfileDimensions.ZoomControlButtonSize)
            .clip(KeiTheme.shapes.chip)
            .background(if (hoverState.hovered) KeiTheme.colors.chip else Color.Transparent)
            .hoverable(hoverState.interactionSource)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
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
                works = PreviewWorks,
                selectedLicense = null,
                onClickUrl = {},
                onClickLicense = {},
                onDismissLicense = {},
                onClickRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewPaneLoadingPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 420.dp, height = 640.dp)
                .background(KeiTheme.colors.island),
        ) {
            PreviewPane(
                page = EditorPage.Profile,
                profile = null,
                contributions = null,
                licenses = PreviewThirdPartyLicenses,
                works = PreviewWorks,
                selectedLicense = null,
                onClickUrl = {},
                onClickLicense = {},
                onDismissLicense = {},
                onClickRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewPaneFailedPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 420.dp, height = 640.dp)
                .background(KeiTheme.colors.island),
        ) {
            PreviewPane(
                page = EditorPage.Profile,
                profile = null,
                contributions = null,
                licenses = PreviewThirdPartyLicenses,
                works = PreviewWorks,
                selectedLicense = null,
                onClickUrl = {},
                onClickLicense = {},
                onDismissLicense = {},
                onClickRetry = {},
                profileLoadFailed = true,
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
                onClickZoomIn = {},
                onClickZoomOut = {},
                onClickActualSize = {},
                onClickFit = {},
            )
        }
    }
}
