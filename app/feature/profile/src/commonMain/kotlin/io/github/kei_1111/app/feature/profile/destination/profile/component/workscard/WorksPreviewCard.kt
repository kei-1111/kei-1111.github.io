@file:Suppress("TooManyFunctions", "MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component.workscard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageController
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ThemedIcon
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.core.utils.prefersReducedMotion
import io.github.kei_1111.app.feature.profile.destination.profile.component.githubcard.SectionLabel
import io.github.kei_1111.app.feature.profile.destination.profile.model.forLanguage
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewWorks
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileAnimations
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.works_next
import kei_1111.app.feature.profile.generated.resources.works_prev
import kei_1111.app.feature.profile.generated.resources.works_screenshot_next
import kei_1111.app.feature.profile.generated.resources.works_screenshot_prev
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/**
 * 作品プレビューカード（280x600）。デザイン語彙は GitHubPreviewCard / LicensePreviewCard と共通。
 * [works] が空の場合はカードの枠のみを描く。
 */
@Composable
internal fun WorksPreviewCard(
    works: ImmutableList<Work>,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(ProfileDimensions.WorksCardWidth)
            .height(ProfileDimensions.WorksCardHeight)
            .background(KeiTheme.colors.cardBackground)
            .border(1.dp, KeiTheme.colors.outline),
    ) {
        if (works.isNotEmpty()) {
            // リスト差し替え時は選択をリセットし、旧 index による範囲外参照を防ぐ
            var workIndex by remember(works) { mutableIntStateOf(0) }
            // 作品を切り替えるたびに先頭スクショへ戻す
            var screenshotIndex by remember(works, workIndex) { mutableIntStateOf(0) }
            val work = works[workIndex]
            WorksCardHeader(
                work = work,
                onClickPrev = { workIndex = (workIndex - 1 + works.size) % works.size },
                onClickNext = { workIndex = (workIndex + 1) % works.size },
                modifier = Modifier.padding(
                    start = ProfileDimensions.WorksCardPadding,
                    top = 16.dp,
                    end = ProfileDimensions.WorksCardPadding,
                    bottom = 10.dp,
                ),
            )
            // 位置表示はクロスフェード対象外に置き、遷移中も testTag の id が文書内で一意に保たれるようにする
            ScreenshotLabelRow(
                workIndex = workIndex,
                totalWorks = works.size,
                modifier = Modifier.padding(
                    start = ProfileDimensions.WorksCardPadding,
                    end = ProfileDimensions.WorksCardPadding,
                    bottom = 6.dp,
                ),
            )
            WorksCardBody(
                works = works,
                workIndex = workIndex,
                screenshotIndex = screenshotIndex,
                onClickPrevScreenshot = { screenshotIndex = (screenshotIndex - 1).coerceAtLeast(0) },
                onClickNextScreenshot = {
                    screenshotIndex =
                        (screenshotIndex + 1).coerceAtMost((work.screenshots.size - 1).coerceAtLeast(0))
                },
                onClickUrl = onClickUrl,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WorksCardHeader(
    work: Work,
    onClickPrev: () -> Unit,
    onClickNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        WorksCardIcon(iconUrl = work.iconUrl)
        // ellipsis を効かせるため、ナビゲーションボタンを押し出す役割も兼ねて weight を持たせる
        WorksCardTitleBlock(work = work, modifier = Modifier.weight(1f))
        WorksNavButtons(onClickPrev = onClickPrev, onClickNext = onClickNext)
    }
}

/** 作品アイコン。読み込み前・失敗時・URL なしは既定の Kotlin アイコンがそのまま見える。 */
@Composable
private fun WorksCardIcon(
    iconUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(KeiTheme.shapes.card)
            .background(KeiTheme.colors.gitHubItem),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.kotlin,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        if (iconUrl != null) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
private fun WorksCardTitleBlock(
    work: Work,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = work.name,
            modifier = Modifier.semantics { heading() },
            style = KeiTheme.typography.chrome.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = KeiTheme.colors.textPrimary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = work.stack,
            style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.androidGreen),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WorksNavButtons(
    onClickPrev: () -> Unit,
    onClickNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        WorksNavButton(
            icon = KeiTheme.icons.chevronRight,
            // 左送りは公式 chevron_right の 180 度回転で表す（アイコン自作は不可）
            iconRotation = 180f,
            contentDescription = stringResource(Res.string.works_prev),
            testTag = TestTags.Profile.WORKS_PREV,
            onClick = onClickPrev,
        )
        WorksNavButton(
            icon = KeiTheme.icons.chevronRight,
            iconRotation = 0f,
            contentDescription = stringResource(Res.string.works_next),
            testTag = TestTags.Profile.WORKS_NEXT,
            onClick = onClickNext,
        )
    }
}

@Composable
private fun WorksNavButton(
    icon: ThemedIcon,
    iconRotation: Float,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    val background = if (hoverState.hovered) KeiTheme.colors.gitHubItemHover else KeiTheme.colors.gitHubItem
    Box(
        modifier = modifier
            .testTag(testTag)
            // アイコンだけではアクセシブルネームとして意味が伝わらないため、ラベルを上書きする
            .semantics { this.contentDescription = contentDescription }
            .size(26.dp)
            .clip(KeiTheme.shapes.githubItem)
            .background(background)
            .border(1.dp, KeiTheme.colors.outline, KeiTheme.shapes.githubItem)
            .hoverable(hoverState.interactionSource)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = icon,
            contentDescription = null,
            modifier = Modifier
                .size(ProfileDimensions.ChromeIconSize)
                .rotate(iconRotation),
        )
    }
}

/**
 * スクショ・タグ・ボタン等は作品切り替えでクロスフェードする。
 * [workIndex] をキーにするため、遷移中は退場側の作品データもそのまま解決できる。
 */
@Composable
private fun WorksCardBody(
    works: ImmutableList<Work>,
    workIndex: Int,
    screenshotIndex: Int,
    onClickPrevScreenshot: () -> Unit,
    onClickNextScreenshot: () -> Unit,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReducedMotion = remember { prefersReducedMotion() }
    Crossfade(
        targetState = workIndex,
        animationSpec = tween(if (isReducedMotion) 0 else ProfileAnimations.ContentCrossfadeMillis),
        modifier = modifier,
    ) { currentIndex ->
        // 退場側のフレームはリスト差し替え直後に旧 index のまま描かれうるため、範囲外は描かない
        works.getOrNull(currentIndex)?.let { work ->
            WorksCardBodyContent(
                work = work,
                screenshotIndex = screenshotIndex,
                onClickPrevScreenshot = onClickPrevScreenshot,
                onClickNextScreenshot = onClickNextScreenshot,
                onClickUrl = onClickUrl,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun WorksCardBodyContent(
    work: Work,
    screenshotIndex: Int,
    onClickPrevScreenshot: () -> Unit,
    onClickNextScreenshot: () -> Unit,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 退場側はホイストされた index と枚数が食い違いうるため、表示前に丸める
    val safeScreenshotIndex = screenshotIndex.coerceAtMost((work.screenshots.size - 1).coerceAtLeast(0))
    Column(modifier = modifier) {
        ScreenshotSection(
            screenshotUrl = work.screenshots.getOrNull(safeScreenshotIndex),
            onClickPrevScreenshot = onClickPrevScreenshot,
            onClickNextScreenshot = onClickNextScreenshot,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        if (work.screenshots.size >= 2) {
            WorksPageDots(
                count = work.screenshots.size,
                selectedIndex = safeScreenshotIndex,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        WorksInfoBlock(
            work = work,
            onClickUrl = onClickUrl,
            modifier = Modifier.padding(
                start = ProfileDimensions.WorksCardPadding,
                end = ProfileDimensions.WorksCardPadding,
                top = 6.dp,
                bottom = 16.dp,
            ),
        )
    }
}

@Composable
private fun ScreenshotLabelRow(
    workIndex: Int,
    totalWorks: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionLabel(text = "SCREENSHOT")
        Text(
            text = "${workIndex + 1} / $totalWorks",
            modifier = Modifier.testTag(TestTags.Profile.WORKS_POSITION),
            style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.textSecondary),
        )
    }
}

@Composable
private fun ScreenshotSection(
    screenshotUrl: String?,
    onClickPrevScreenshot: () -> Unit,
    onClickNextScreenshot: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(KeiTheme.colors.screenshotWell)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        ScreenshotFrame(
            screenshotUrl = screenshotUrl,
            onClickPrev = onClickPrevScreenshot,
            onClickNext = onClickNextScreenshot,
        )
    }
}

/** 9:19.5 の実比率フレーム。画像未着・読み込み失敗時はプレースホルダ面がそのまま見える。 */
@Composable
private fun ScreenshotFrame(
    screenshotUrl: String?,
    onClickPrev: () -> Unit,
    onClickNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(9f / 19.5f)
            // 浮いたスマホ画面に見せる控えめな影。クリップ前に適用する
            .shadow(6.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, KeiTheme.colors.outline, RoundedCornerShape(12.dp))
            .background(KeiTheme.colors.gitHubItem),
    ) {
        Text(
            text = "TODO",
            modifier = Modifier.align(Alignment.Center),
            style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.textSecondary),
        )
        if (screenshotUrl != null) {
            AsyncImage(
                model = screenshotUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
        Row(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        onClickLabel = stringResource(Res.string.works_screenshot_prev),
                        onClick = onClickPrev,
                    ),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        onClickLabel = stringResource(Res.string.works_screenshot_next),
                        onClick = onClickNext,
                    ),
            )
        }
    }
}

@Composable
private fun WorksPageDots(
    count: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(count) { index ->
                WorksPageDot(active = index == selectedIndex)
            }
        }
    }
}

@Composable
private fun WorksPageDot(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    if (active) {
        Box(
            modifier = modifier
                .size(width = 14.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(KeiTheme.colors.androidGreen),
        )
    } else {
        Box(
            modifier = modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(KeiTheme.colors.muted),
        )
    }
}

@Composable
private fun WorksInfoBlock(
    work: Work,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WorksDescription(description = work.description)
        WorksTagRow(tags = work.tags)
        if (work.storeUrl != null || work.sourceUrl != null) {
            WorksButtonRow(storeUrl = work.storeUrl, sourceUrl = work.sourceUrl, onClickUrl = onClickUrl)
        }
    }
}

@Composable
private fun WorksDescription(
    description: LocalizedText,
    modifier: Modifier = Modifier,
) {
    val language = KeiLanguageController.language
    Text(
        text = description.forLanguage(language),
        modifier = modifier.fillMaxWidth(),
        style = KeiTheme.typography.githubJp.copy(
            fontSize = 9.5.sp,
            lineHeight = 16.15.sp,
            color = KeiTheme.colors.textSecondary,
        ),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun WorksTagRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            WorksTagChip(tag = tag)
        }
    }
}

@Composable
private fun WorksTagChip(
    tag: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.pill)
            .background(KeiTheme.colors.gitHubItem)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = tag,
            style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = KeiTheme.colors.textSecondary),
        )
    }
}

@Composable
private fun WorksButtonRow(
    storeUrl: String?,
    sourceUrl: String?,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (storeUrl != null) {
            WorksPrimaryButton(
                label = "Google Play",
                url = storeUrl,
                onClickUrl = onClickUrl,
                modifier = Modifier.weight(1f),
            )
            if (sourceUrl != null) {
                WorksSecondaryButton(
                    label = "Source ↗",
                    url = sourceUrl,
                    onClickUrl = onClickUrl,
                    modifier = Modifier.weight(1f),
                )
            }
        } else if (sourceUrl != null) {
            WorksPrimaryButton(
                label = "Source ↗",
                url = sourceUrl,
                onClickUrl = onClickUrl,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WorksPrimaryButton(
    label: String,
    url: String,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.githubItem)
            .background(KeiTheme.colors.androidGreen)
            .clickable { onClickUrl(url) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = KeiTheme.typography.chrome.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = KeiTheme.colors.cardBackground,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WorksSecondaryButton(
    label: String,
    url: String,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    Box(
        modifier = modifier
            .clip(KeiTheme.shapes.githubItem)
            .background(if (hoverState.hovered) KeiTheme.colors.gitHubItem else KeiTheme.colors.cardBackground)
            .border(1.dp, KeiTheme.colors.outline, KeiTheme.shapes.githubItem)
            .hoverable(hoverState.interactionSource)
            .clickable { onClickUrl(url) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = KeiTheme.typography.chrome.copy(fontSize = 10.sp, color = KeiTheme.colors.textPrimary),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun WorksPreviewCardPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(8.dp)) {
            WorksPreviewCard(
                works = PreviewWorks,
                onClickUrl = {},
            )
        }
    }
}
