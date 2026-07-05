@file:Suppress("MagicNumber", "LongMethod", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile.destination.profile.component

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.core.designsystem.theme.CodeJapaneseFallbackFamily
import io.github.kei_1111.core.designsystem.theme.KeiTheme
import io.github.kei_1111.core.model.GitHubProfile
import io.github.kei_1111.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.feature.profile.destination.profile.EditorViewMode
import io.github.kei_1111.feature.profile.destination.profile.preview.PreviewGitHubProfile
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.ic_chevron_down_dark
import kei_1111.feature.profile.generated.resources.ic_close_small_dark
import kei_1111.feature.profile.generated.resources.ic_editor_only_dark
import kei_1111.feature.profile.generated.resources.ic_editor_preview_dark
import kei_1111.feature.profile.generated.resources.ic_inspections_ok_dark
import kei_1111.feature.profile.generated.resources.ic_kotlin_dark
import kei_1111.feature.profile.generated.resources.ic_markdown_dark
import kei_1111.feature.profile.generated.resources.ic_more_vertical_dark
import kei_1111.feature.profile.generated.resources.ic_preview_only_dark
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * エディタのタブバー。[viewMode] と [onSelectViewMode] を渡すと、
 * 右端に実 AS の Code / Split / Design 相当の表示モード切替を表示する。
 * タブ列は weight で残り幅に収め、幅が足りないときは横スクロールする
 * （右端のボタン群が画面外に押し出されないようにするため）。
 * [showSplitButton] を false にすると Split ボタンを表示しない（Mobile 用）。
 */
@Composable
internal fun EditorTabBar(
    selectedPage: EditorPage,
    onSelectPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
    viewMode: EditorViewMode? = null,
    onSelectViewMode: ((EditorViewMode) -> Unit)? = null,
    showSplitButton: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditorPage.entries.forEach { page ->
                EditorTab(
                    page = page,
                    selected = page == selectedPage,
                    onClick = { onSelectPage(page) },
                )
            }
        }
        if (viewMode != null && onSelectViewMode != null) {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_down_dark),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(4.dp))
            ViewModeButton(
                icon = Res.drawable.ic_editor_only_dark,
                selected = viewMode == EditorViewMode.CodeOnly,
                onClick = { onSelectViewMode(EditorViewMode.CodeOnly) },
            )
            if (showSplitButton) {
                ViewModeButton(
                    icon = Res.drawable.ic_editor_preview_dark,
                    selected = viewMode == EditorViewMode.Split,
                    onClick = { onSelectViewMode(EditorViewMode.Split) },
                )
            }
            ViewModeButton(
                icon = Res.drawable.ic_preview_only_dark,
                selected = viewMode == EditorViewMode.PreviewOnly,
                onClick = { onSelectViewMode(EditorViewMode.PreviewOnly) },
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(Res.drawable.ic_more_vertical_dark),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

/** 表示モード切替の1ボタン。選択中はグレーの選択ピルで示す。 */
@Composable
private fun ViewModeButton(
    icon: DrawableResource,
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
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Unspecified,
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
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val background = when {
        selected -> KeiTheme.colors.tabSelected
        hovered -> KeiTheme.colors.chip
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
            .hoverable(interaction)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabBadge(kotlin = page.fileName.endsWith(".kt"))
        Text(
            text = page.fileName,
            style = KeiTheme.typography.chrome.copy(
                fontSize = 12.sp,
                color = if (selected) KeiTheme.colors.textPrimary else KeiTheme.colors.textSecondary,
            ),
        )
        if (selected) {
            Icon(
                painter = painterResource(Res.drawable.ic_close_small_dark),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun TabBadge(
    kotlin: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(
            if (kotlin) Res.drawable.ic_kotlin_dark else Res.drawable.ic_markdown_dark,
        ),
        contentDescription = null,
        modifier = modifier.size(16.dp),
        tint = Color.Unspecified,
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
internal fun CodeLines(
    page: EditorPage,
    profile: GitHubProfile,
    modifier: Modifier = Modifier,
) {
    val japaneseFontFamily = CodeJapaneseFallbackFamily()
    val lines = remember(page, profile, japaneseFontFamily) { codeLinesFor(page, profile, japaneseFontFamily) }
    val codeStyle = KeiTheme.typography.code
    val lineHeight = 22.dp

    Box(modifier = modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 行番号列
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp),
            ) {
                lines.indices.forEach { i ->
                    Text(
                        text = (i + 1).toString(),
                        modifier = Modifier.height(lineHeight),
                        style = codeStyle.copy(color = KeiTheme.colors.muted),
                        textAlign = TextAlign.End,
                    )
                }
            }
            // コード列
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
            ) {
                lines.forEachIndexed { i, line ->
                    Row(
                        modifier = Modifier.height(lineHeight),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = line, style = codeStyle, softWrap = false)
                        if (i == lines.lastIndex) {
                            BlinkingCaret()
                        }
                    }
                }
            }
        }
        // 1行目の右端に成功インスペクションのチェック
        Icon(
            painter = painterResource(Res.drawable.ic_inspections_ok_dark),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 14.dp)
                .size(16.dp),
            tint = Color.Unspecified,
        )
    }
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
                durationMillis = 1100,
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
private fun EditorPanePreview() {
    KeiTheme {
        // verticalScroll は無限制約下で測定できないため、Preview では有限サイズを与える
        Box(
            modifier = Modifier
                .size(width = 560.dp, height = 480.dp)
                .background(KeiTheme.colors.island),
        ) {
            Column {
                EditorTabBar(
                    selectedPage = EditorPage.Profile,
                    onSelectPage = {},
                    viewMode = EditorViewMode.Split,
                    onSelectViewMode = {},
                )
                HorizontalDivider(color = KeiTheme.colors.islandBorder, thickness = 1.dp)
                EditorCodeArea(page = EditorPage.Profile, profile = PreviewGitHubProfile)
            }
        }
    }
}
