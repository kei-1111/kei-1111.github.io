@file:Suppress("TooManyFunctions", "MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component.workscard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ThemedIcon
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.core.utils.prefersReducedMotion
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewWorks
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileAnimations
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.works_detail
import kei_1111.app.feature.profile.generated.resources.works_next
import kei_1111.app.feature.profile.generated.resources.works_prev
import kei_1111.app.feature.profile.generated.resources.works_screenshot
import kei_1111.app.feature.profile.generated.resources.works_screenshot_next
import kei_1111.app.feature.profile.generated.resources.works_screenshot_prev
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/** チップ行に表示する上限枚数。超過分は "+n" チップに畳む（全量はシートで見せる）。 */
private const val MAX_VISIBLE_TAGS = 4

/**
 * 作品プレビューカード（280x600）。デザイン語彙は GitHubPreviewCard / LicensePreviewCard と共通。
 * 説明文や全タグ・担当領域は [WorksDetailSheetOverlay] へ逃がし、カード面はヘッダー・チップ・
 * スクショ・導線ボタンのみで完結させる（旧デザインは説明文とタグでカード面が溢れていた）。
 */
@Composable
internal fun WorksPreviewCard(
    works: ImmutableList<Work>,
    sheetOpen: Boolean,
    onChangeSheetVisible: (Boolean) -> Unit,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // リスト差し替え時は選択をリセットし、旧 index による範囲外参照を防ぐ
    var workIndex by remember(works) { mutableIntStateOf(0) }
    val selectedWork = works.getOrNull(workIndex)
    Box(
        modifier = modifier
            .width(ProfileDimensions.WorksCardWidth)
            .height(ProfileDimensions.WorksCardHeight)
            // LicensePreviewCard と同じ角の立った矩形。clipToBounds はシートのスライドをカード境界でマスクする
            .clipToBounds()
            .background(KeiTheme.colors.cardBackground)
            .border(1.dp, KeiTheme.colors.outline),
    ) {
        if (selectedWork != null) {
            WorksCardContent(
                works = works,
                workIndex = workIndex,
                onClickPrev = { workIndex = (workIndex - 1 + works.size) % works.size },
                onClickNext = { workIndex = (workIndex + 1) % works.size },
                onClickUrl = onClickUrl,
                onClickDetail = { onChangeSheetVisible(true) },
                modifier = Modifier.fillMaxSize(),
            )
            WorksDetailSheetOverlay(
                work = selectedWork,
                visible = sheetOpen,
                onDismiss = { onChangeSheetVisible(false) },
                onClickUrl = onClickUrl,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
private fun WorksCardContent(
    works: ImmutableList<Work>,
    workIndex: Int,
    onClickPrev: () -> Unit,
    onClickNext: () -> Unit,
    onClickUrl: (String) -> Unit,
    onClickDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 作品を切り替えるたびに先頭スクショへ戻す
    var screenshotIndex by remember(works, workIndex) { mutableIntStateOf(0) }
    val work = works[workIndex]
    Column(modifier = modifier) {
        WorksCardHeader(
            work = work,
            workIndex = workIndex,
            totalWorks = works.size,
            onClickPrev = onClickPrev,
            onClickNext = onClickNext,
            modifier = Modifier.padding(
                start = ProfileDimensions.WorksCardPadding,
                top = 16.dp,
                end = ProfileDimensions.WorksCardPadding,
                bottom = 10.dp,
            ),
        )
        WorksChipRow(
            tags = work.tags,
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
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        // ボタン行はクロスフェード対象外（WORKS_DETAIL の id が遷移中も文書内で一意に保たれる）
        WorksButtonRow(
            storeUrl = work.storeUrl,
            sourceUrl = work.sourceUrl,
            onClickUrl = onClickUrl,
            onClickDetail = onClickDetail,
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
private fun WorksCardHeader(
    work: Work,
    workIndex: Int,
    totalWorks: Int,
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
        // ellipsis を効かせるため、右側グループを押し出す役割も兼ねて weight を持たせる
        WorksCardTitle(name = work.name, modifier = Modifier.weight(1f))
        WorksHeaderTrailingGroup(
            workIndex = workIndex,
            totalWorks = totalWorks,
            onClickPrev = onClickPrev,
            onClickNext = onClickNext,
        )
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
            WorksAsyncImage(
                url = iconUrl,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
private fun WorksCardTitle(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = name,
        modifier = modifier.semantics { heading() },
        style = KeiTheme.typography.chrome.copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = KeiTheme.colors.textPrimary,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** 位置表示 + 前後ナビゲーション。位置表示はクロスフェード対象外に置き、遷移中も testTag の id が文書内で一意に保たれるようにする。 */
@Composable
private fun WorksHeaderTrailingGroup(
    workIndex: Int,
    totalWorks: Int,
    onClickPrev: () -> Unit,
    onClickNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        WorksPositionLabel(workIndex = workIndex, totalWorks = totalWorks)
        if (totalWorks >= 2) {
            WorksNavButtons(onClickPrev = onClickPrev, onClickNext = onClickNext)
        }
    }
}

@Composable
private fun WorksPositionLabel(
    workIndex: Int,
    totalWorks: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "${workIndex + 1} / $totalWorks",
        modifier = modifier.testTag(TestTags.Profile.WORKS_POSITION),
        style = KeiTheme.typography.chrome.copy(fontSize = 8.sp, color = KeiTheme.colors.textSecondary),
    )
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
            // 左送りは公式 chevron_right の 180 度回転で表す(アイコン自作は不可)
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

/** タグチップ行。2行に収まる想定で先頭 [MAX_VISIBLE_TAGS] 件のみ描き、超過分は "+n" チップに畳む（全量はシートで見せる）。 */
@Composable
private fun WorksChipRow(
    tags: ImmutableList<WorkTag>,
    modifier: Modifier = Modifier,
) {
    val visibleTags = tags.take(MAX_VISIBLE_TAGS)
    val overflowCount = tags.size - visibleTags.size
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        visibleTags.forEach { tag -> WorksTagChip(tag = tag) }
        if (overflowCount > 0) {
            WorksTagOverflowChip(count = overflowCount)
        }
    }
}

/**
 * スクショとドットは作品切り替えでクロスフェードする。
 * [workIndex] をキーにするため、遷移中は退場側の作品データもそのまま解決できる。
 */
@Composable
private fun WorksCardBody(
    works: ImmutableList<Work>,
    workIndex: Int,
    screenshotIndex: Int,
    onClickPrevScreenshot: () -> Unit,
    onClickNextScreenshot: () -> Unit,
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
    modifier: Modifier = Modifier,
) {
    // 退場側はホイストされた index と枚数が食い違いうるため、表示前に丸める
    val safeScreenshotIndex = screenshotIndex.coerceAtMost((work.screenshots.size - 1).coerceAtLeast(0))
    Column(modifier = modifier) {
        ScreenshotSection(
            screenshotUrl = work.screenshots.getOrNull(safeScreenshotIndex),
            workName = work.name,
            showNavigation = work.screenshots.size >= 2,
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
    }
}

@Composable
private fun ScreenshotSection(
    screenshotUrl: String?,
    workName: String,
    showNavigation: Boolean,
    onClickPrevScreenshot: () -> Unit,
    onClickNextScreenshot: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(KeiTheme.colors.screenshotWell)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        ScreenshotFrame(
            screenshotUrl = screenshotUrl,
            workName = workName,
        )
        // 送りゾーンはフレームでなく well 全域に重ねる（横長スクショでもクリック領域が痩せない）
        if (showNavigation) {
            Row(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            onClickLabel = stringResource(Res.string.works_screenshot_prev),
                            onClick = onClickPrevScreenshot,
                        ),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            onClickLabel = stringResource(Res.string.works_screenshot_next),
                            onClick = onClickNextScreenshot,
                        ),
                )
            }
        }
    }
}

/** 9:19.5 の実比率フレーム。画像未着・読み込み失敗時はプレースホルダ面がそのまま見える。 */
@Composable
private fun ScreenshotFrame(
    screenshotUrl: String?,
    workName: String,
    modifier: Modifier = Modifier,
) {
    val painter = screenshotUrl?.let { rememberWorksAsyncPainter(it) }
    val state = painter?.state?.collectAsState()?.value
    // 読み込んだ画像の実比率にフレームを合わせる（縦スクショはスマホ枠、横長はブラウザ枠）。
    // 切替の読み込み中は直前の比率を保ち、既定比率へ戻るジャンプを防ぐ（初期値は 9:19.5）
    val intrinsic = (state as? AsyncImagePainter.State.Success)?.painter?.intrinsicSize
    var ratio by remember { mutableFloatStateOf(9f / 19.5f) }
    val intrinsicRatio = if (intrinsic != null && intrinsic.height > 0f) intrinsic.width / intrinsic.height else null
    if (intrinsicRatio != null && intrinsicRatio.isFinite() && intrinsicRatio > 0f) {
        ratio = intrinsicRatio
    }
    val frameSizeModifier = if (ratio < 1f) {
        Modifier.fillMaxHeight().aspectRatio(ratio)
    } else {
        Modifier.fillMaxWidth().aspectRatio(ratio)
    }
    Box(
        modifier = modifier
            .then(frameSizeModifier)
            // 浮いた画面に見せる控えめな影。クリップ前に適用する
            .shadow(6.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, KeiTheme.colors.outline, RoundedCornerShape(12.dp))
            .background(KeiTheme.colors.gitHubItem),
    ) {
        if (painter != null) {
            // filterQuality は rememberWorksAsyncPainter 側で High を指定済み
            Image(
                painter = painter,
                contentDescription = stringResource(Res.string.works_screenshot, workName),
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
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

/**
 * Primary: ストア掲載があれば Google Play、なければソース公開があれば Source。
 * Secondary: 常に表示する詳細ボタン（クリックでシートを開く）。
 */
@Composable
private fun WorksButtonRow(
    storeUrl: String?,
    sourceUrl: String?,
    onClickUrl: (String) -> Unit,
    onClickDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            storeUrl != null -> WorksStoreButton(url = storeUrl, onClickUrl = onClickUrl, modifier = Modifier.weight(1f))
            sourceUrl != null -> WorksSourceButton(url = sourceUrl, onClickUrl = onClickUrl, modifier = Modifier.weight(1f))
        }
        WorksDetailButton(onClick = onClickDetail, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun WorksDetailButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    Box(
        modifier = modifier
            .testTag(TestTags.Profile.WORKS_DETAIL)
            .height(32.dp)
            .clip(KeiTheme.shapes.linkTile)
            .background(if (hoverState.hovered) KeiTheme.colors.gitHubItemHover else KeiTheme.colors.gitHubItem)
            .hoverable(hoverState.interactionSource)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.works_detail),
                style = KeiTheme.typography.githubJp.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.size(6.dp))
            // シートが上に開くことを示す ⌃。左送りボタンと同様、公式 chevron の回転で表す
            KeiIcon(
                icon = KeiTheme.icons.chevronDown,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .rotate(180f),
            )
        }
    }
}

@Preview
@Composable
private fun WorksPreviewCardPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(8.dp)) {
            WorksPreviewCard(
                works = PreviewWorks,
                sheetOpen = false,
                onChangeSheetVisible = {},
                onClickUrl = {},
            )
        }
    }
}
