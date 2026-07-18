@file:Suppress("MagicNumber", "ModifierMissing", "TooManyFunctions", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.CodeJapaneseFallbackFamily
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ThemedIcon
import io.github.kei_1111.app.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.app.feature.profile.destination.profile.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.theme.ProfileAnimations
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.rememberHoverState
import io.github.kei_1111.shared.model.GitHubProfile

/**
 * エディタのタブバー。[viewMode] と [onChangeViewMode] を渡すと、
 * 右端に実 AS の Code / Split / Design 相当の表示モード切替を表示する。
 * タブ列は weight で残り幅に収め、幅が足りないときは横スクロールする
 * （右端のボタン群が画面外に押し出されないようにするため）。
 * [showSplitButton] を false にすると Split ボタンを表示しない（Mobile 用）。
 */
@Composable
internal fun EditorTabBar(
    selectedPage: EditorPage,
    onClickPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
    viewMode: EditorViewMode? = null,
    onChangeViewMode: ((EditorViewMode) -> Unit)? = null,
    showSplitButton: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabList(
            selectedPage = selectedPage,
            onClickPage = onClickPage,
            modifier = Modifier.weight(1f),
        )
        if (viewMode != null && onChangeViewMode != null) {
            TabListIndicator()
            Spacer(modifier = Modifier.width(4.dp))
            ViewModeButton(
                icon = KeiTheme.icons.editorOnly,
                selected = viewMode == EditorViewMode.CodeOnly,
                onClick = { onChangeViewMode(EditorViewMode.CodeOnly) },
            )
            if (showSplitButton) {
                ViewModeButton(
                    icon = KeiTheme.icons.editorPreview,
                    selected = viewMode == EditorViewMode.Split,
                    onClick = { onChangeViewMode(EditorViewMode.Split) },
                )
            }
            ViewModeButton(
                icon = KeiTheme.icons.previewOnly,
                selected = viewMode == EditorViewMode.PreviewOnly,
                onClick = { onChangeViewMode(EditorViewMode.PreviewOnly) },
            )
            Spacer(modifier = Modifier.width(4.dp))
            EditorMenuIndicator()
        }
    }
}

/** 開いているタブの横スクロール列。 */
@Composable
private fun TabList(
    selectedPage: EditorPage,
    onClickPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorPage.entries.forEach { page ->
            EditorTab(
                page = page,
                selected = page == selectedPage,
                onClick = { onClickPage(page) },
            )
        }
    }
}

/** タブ列右の装飾シェブロン。実 AS の隠れタブ一覧アイコンを模しただけで、クリックしても何も起きない。 */
@Composable
private fun TabListIndicator(modifier: Modifier = Modifier) {
    KeiIcon(
        icon = KeiTheme.icons.chevronDown,
        contentDescription = null,
        modifier = modifier.size(12.dp),
    )
}

/** タブバー右端のエディタオプションメニューアイコン。 */
@Composable
private fun EditorMenuIndicator(modifier: Modifier = Modifier) {
    KeiIcon(
        icon = KeiTheme.icons.moreVertical,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.ChromeIconSize),
    )
}

/** 表示モード切替の1ボタン。選択中はグレーの選択ピルで示す。 */
@Composable
private fun ViewModeButton(
    icon: ThemedIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(KeiTheme.shapes.chip)
            .background(if (selected) KeiTheme.colors.selectionPill else Color.Transparent)
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

@Composable
private fun EditorTab(
    page: EditorPage,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    val background = when {
        selected -> KeiTheme.colors.tabSelected
        hoverState.hovered -> KeiTheme.colors.chip
        else -> Color.Transparent
    }
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .background(background)
            .then(
                if (selected) {
                    Modifier.border(1.dp, KeiTheme.colors.tabSelectedBorder, KeiTheme.shapes.row)
                } else {
                    Modifier
                },
            )
            .hoverable(hoverState.interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabFileIcon(kotlin = page.fileName.endsWith(".kt"))
        TabLabel(fileName = page.fileName, selected = selected)
        if (selected) {
            TabCloseIcon()
        }
    }
}

@Composable
private fun TabFileIcon(
    kotlin: Boolean,
    modifier: Modifier = Modifier,
) {
    KeiIcon(
        icon = if (kotlin) KeiTheme.icons.kotlin else KeiTheme.icons.markdown,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.ChromeIconSize),
    )
}

@Composable
private fun TabLabel(
    fileName: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = fileName,
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(
            fontSize = ProfileDimensions.ChromeLabelFontSize,
            color = if (selected) KeiTheme.colors.textPrimary else KeiTheme.colors.textSecondary,
        ),
    )
}

/** 選択中タブに表示する閉じるアイコン。 */
@Composable
private fun TabCloseIcon(modifier: Modifier = Modifier) {
    KeiIcon(
        icon = KeiTheme.icons.closeSmall,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.ChromeIconSize),
    )
}

/** エディタのコード領域（縦スクロール付き）。Desktop の島レイアウトから直接使う。 */
@Composable
internal fun EditorCodeArea(
    page: EditorPage,
    profile: GitHubProfile,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CodeLines(page = page, profile = profile)
    }
}

/** 行番号 + ハイライト済みコード + キャレットを自然な高さで描画する（縦スクロールは持たない）。 */
@Composable
private fun CodeLines(
    page: EditorPage,
    profile: GitHubProfile,
    modifier: Modifier = Modifier,
) {
    val japaneseFontFamily = CodeJapaneseFallbackFamily()
    val colors = KeiTheme.colors
    val lines = remember(page, profile, japaneseFontFamily, colors) {
        codeLinesFor(page, profile, japaneseFontFamily, colors)
    }

    Box(modifier = modifier.padding(vertical = 8.dp)) {
        CodeBody(lines = lines, modifier = Modifier.fillMaxWidth())
        InspectionsIndicator(modifier = Modifier.align(Alignment.TopEnd))
    }
}

/** 行番号ガター + ハイライト済みコード列。 */
@Composable
private fun CodeBody(
    lines: List<AnnotatedString>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        LineNumberColumn(lines = lines)
        CodeColumn(
            lines = lines,
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
        )
    }
}

@Composable
private fun LineNumberColumn(
    lines: List<AnnotatedString>,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier.padding(start = 12.dp, end = 12.dp),
    ) {
        lines.indices.forEach { i ->
            Text(
                text = (i + 1).toString(),
                modifier = Modifier.height(ProfileDimensions.EditorLineHeight),
                style = KeiTheme.typography.code.copy(color = KeiTheme.colors.muted),
                textAlign = TextAlign.End,
            )
        }
    }
}

/** ハイライト済みコード列（横スクロール）。 */
@Composable
private fun CodeColumn(
    lines: List<AnnotatedString>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        lines.forEachIndexed { i, line ->
            Row(
                modifier = Modifier.height(ProfileDimensions.EditorLineHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CodeLineText(line = line)
                if (i == lines.lastIndex) {
                    BlinkingCaret()
                }
            }
        }
    }
}

@Composable
private fun CodeLineText(
    line: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(text = line, modifier = modifier, style = KeiTheme.typography.code, softWrap = false)
}

/** 右上に表示する成功インスペクションのチェックマーク。 */
@Composable
private fun InspectionsIndicator(modifier: Modifier = Modifier) {
    KeiIcon(
        icon = KeiTheme.icons.inspectionsOk,
        contentDescription = null,
        modifier = modifier
            .padding(top = 8.dp, end = 14.dp)
            .size(ProfileDimensions.ChromeIconSize),
    )
}

/** 点滅キャレット（8×15px, 1.1s step-end 相当）。 */
@Composable
private fun BlinkingCaret(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ProfileAnimations.CaretBlinkMillis,
                easing = { fraction -> if (fraction < 0.5f) 0f else 1f },
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )
    Box(
        modifier = modifier
            .padding(start = 1.dp)
            .alpha(alpha)
            .size(width = 8.dp, height = 15.dp)
            .background(KeiTheme.colors.textPrimary),
    )
}

@Preview
@Composable
private fun EditorTabBarPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.island)) {
            EditorTabBar(
                selectedPage = EditorPage.Profile,
                onClickPage = {},
                viewMode = EditorViewMode.Split,
                onChangeViewMode = {},
            )
        }
    }
}

@Preview
@Composable
private fun EditorCodeAreaPreview() {
    KeiTheme {
        // verticalScroll は無限制約下で測定できないため、Preview では有限サイズを与える
        Box(
            modifier = Modifier
                .size(width = 560.dp, height = 480.dp)
                .background(KeiTheme.colors.island),
        ) {
            EditorCodeArea(page = EditorPage.Profile, profile = PreviewGitHubProfile)
        }
    }
}

@Preview
@Composable
private fun CodeLinesPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .width(560.dp)
                .background(KeiTheme.colors.island),
        ) {
            CodeLines(page = EditorPage.Profile, profile = PreviewGitHubProfile)
        }
    }
}
