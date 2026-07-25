@file:Suppress("MagicNumber", "LongMethod", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ThemedIcon
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.model.EditorPage

/** ナビゲーションとして機能しない（押せない）行の透過率。 */
private const val NON_CLICKABLE_ROW_ALPHA = 0.45f

/**
 * プロジェクトツリー（島1）。実ナビゲーションとして機能する行を含む。
 * Android Studio の Project ビュー（実ディレクトリ構成）を模している。
 * アイコンは IntelliJ Platform / Android Studio の実アイコン（New UI ダーク版）。
 * [scrollable] を true にするとツリー内で縦スクロールする（有限高さが前提）。
 * 横方向は常にスクロール可能で、島幅からはみ出したファイル名を確認できる（実 AS の Project ビュー同様）。
 */
@Composable
internal fun ProjectTree(
    selectedPage: EditorPage?,
    onClickPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
) {
    Column(
        modifier = modifier
            .width(ProfileDimensions.TreeWidth)
            .clip(KeiTheme.shapes.island)
            .background(KeiTheme.colors.islandDark),
    ) {
        ProjectPaneHeader()
        TreeRows(
            selectedPage = selectedPage,
            onClickPage = onClickPage,
            modifier = Modifier
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .horizontalScroll(rememberScrollState())
                .width(IntrinsicSize.Max)
                .padding(bottom = 8.dp, start = 6.dp, end = 6.dp),
        )
    }
}

/** Project ツールウィンドウのヘッダー行（ペイン名 + 右端のメニュー）。 */
@Composable
private fun ProjectPaneHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Project",
            style = KeiTheme.typography.chrome.copy(
                fontSize = ProfileDimensions.ChromeLabelFontSize,
                fontWeight = FontWeight.Medium,
                color = KeiTheme.colors.textPrimary,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        KeiIcon(
            icon = KeiTheme.icons.chevronDown,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
        Spacer(modifier = Modifier.weight(1f))
        KeiIcon(
            icon = KeiTheme.icons.moreVertical,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
    }
}

@Composable
private fun TreeRows(
    selectedPage: EditorPage?,
    onClickPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        FolderRow(depth = 0, expanded = true, label = "kei-1111.github.io")
        FolderRow(
            depth = 1,
            expanded = false,
            label = ".gradle",
            icon = KeiTheme.icons.excludeRoot,
        )
        FolderRow(
            depth = 1,
            expanded = false,
            label = ".idea",
            icon = KeiTheme.icons.excludeRoot,
        )
        FolderRow(
            depth = 1,
            expanded = false,
            label = ".kotlin",
            icon = KeiTheme.icons.excludeRoot,
        )
        FolderRow(depth = 1, expanded = true, label = "app")
        FolderRow(
            depth = 2,
            expanded = false,
            label = "build",
            icon = KeiTheme.icons.excludeRoot,
        )
        FolderRow(depth = 2, expanded = true, label = "src")
        FolderRow(depth = 3, expanded = false, label = "androidTest")
        FolderRow(depth = 3, expanded = true, label = "main")
        FolderRow(
            depth = 4,
            expanded = true,
            label = "kotlin",
            icon = KeiTheme.icons.sourceRoot,
        )
        FolderRow(
            depth = 5,
            expanded = true,
            label = "io.github.kei_1111",
            icon = KeiTheme.icons.packageNode,
        )
        FolderRow(depth = 6, expanded = true, label = "theme", icon = KeiTheme.icons.packageNode)
        FileRow(depth = 7, label = "Color.kt", icon = KeiTheme.icons.kotlin)
        FileRow(depth = 7, label = "Theme.kt", icon = KeiTheme.icons.kotlin)
        FileRow(depth = 7, label = "Type.kt", icon = KeiTheme.icons.kotlin)
        FolderRow(
            depth = 6,
            expanded = true,
            label = "ui",
            icon = KeiTheme.icons.packageNode,
        )
        FolderRow(
            depth = 7,
            expanded = true,
            label = "profile",
            icon = KeiTheme.icons.packageNode,
        )
        FileRow(
            depth = 8,
            label = "ProfileScreen.kt",
            icon = KeiTheme.icons.kotlin,
            selected = selectedPage == EditorPage.Profile,
            onClick = { onClickPage(EditorPage.Profile) },
        )
        FileRow(depth = 8, label = "GitHubProfileData", icon = KeiTheme.icons.classKotlin)
        FolderRow(
            depth = 7,
            expanded = true,
            label = "license",
            icon = KeiTheme.icons.packageNode,
        )
        FileRow(
            depth = 8,
            label = "LicenseScreen.kt",
            icon = KeiTheme.icons.kotlin,
            selected = selectedPage == EditorPage.Licenses,
            onClick = { onClickPage(EditorPage.Licenses) },
        )
        FileRow(depth = 6, label = "MainActivity", icon = KeiTheme.icons.classKotlin)
        FolderRow(
            depth = 4,
            expanded = false,
            label = "res",
            icon = KeiTheme.icons.resourcesRoot,
        )
        FileRow(
            depth = 4,
            label = "AndroidManifest.xml",
            icon = KeiTheme.icons.manifestFile,
        )
        FileRow(depth = 2, label = ".gitignore", icon = KeiTheme.icons.ignored)
        FileRow(
            depth = 2,
            label = "build.gradle.kts",
            icon = KeiTheme.icons.kotlinGradleScript,
        )
        FolderRow(
            depth = 1,
            expanded = false,
            label = "build",
            icon = KeiTheme.icons.excludeRoot,
        )
        FolderRow(depth = 1, expanded = false, label = "gradle")
        FileRow(
            depth = 1,
            label = "build.gradle.kts",
            icon = KeiTheme.icons.kotlinGradleScript,
        )
        FileRow(depth = 1, label = "gradle.properties", icon = KeiTheme.icons.properties)
        FileRow(
            depth = 1,
            label = "README.md",
            icon = KeiTheme.icons.markdown,
            selected = selectedPage == EditorPage.Readme,
            onClick = { onClickPage(EditorPage.Readme) },
        )
        FileRow(
            depth = 1,
            label = "settings.gradle.kts",
            icon = KeiTheme.icons.kotlinGradleScript,
        )
    }
}

@Composable
private fun FolderRow(
    depth: Int,
    expanded: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    icon: ThemedIcon = KeiTheme.icons.folder,
    onClick: (() -> Unit)? = null,
) {
    TreeRow(depth = depth, modifier = modifier, onClick = onClick) {
        FolderChevron(expanded = expanded)
        Spacer(modifier = Modifier.width(ProfileDimensions.TreeChevronGap))
        TreeIcon(icon)
        Spacer(modifier = Modifier.width(ProfileDimensions.TreeIconLabelGap))
        TreeLabel(label = label, color = KeiTheme.colors.textPrimary)
    }
}

@Composable
private fun FileRow(
    depth: Int,
    label: String,
    icon: ThemedIcon,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    TreeRow(depth = depth, modifier = modifier, selected = selected, onClick = onClick) {
        Spacer(modifier = Modifier.width(ProfileDimensions.TreeChevronSize + ProfileDimensions.TreeChevronGap))
        TreeIcon(icon)
        Spacer(modifier = Modifier.width(ProfileDimensions.TreeIconLabelGap))
        TreeLabel(
            label = label,
            color = if (selected) KeiTheme.colors.textPrimary else KeiTheme.colors.textCode,
        )
    }
}

@Composable
private fun TreeRow(
    depth: Int,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val hoverState = rememberHoverState()
    val clickable = onClick != null
    val background = when {
        selected -> KeiTheme.colors.selectionPill
        hoverState.hovered && clickable -> KeiTheme.colors.chip
        else -> Color.Transparent
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(KeiTheme.shapes.row)
            .background(background)
            .hoverable(hoverState.interactionSource)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(if (clickable) 1f else NON_CLICKABLE_ROW_ALPHA)
            .padding(
                start = ProfileDimensions.TreeLeftInset + ProfileDimensions.TreeIndentStep * depth,
                end = 6.dp,
                top = 4.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

/** ツリー内のファイル/フォルダアイコン。実アイコンの色をそのまま描画する。 */
@Composable
private fun TreeIcon(
    icon: ThemedIcon,
    modifier: Modifier = Modifier,
) {
    KeiIcon(
        icon = icon,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.TreeIconSize),
    )
}

@Composable
private fun TreeLabel(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(fontSize = ProfileDimensions.ChromeLabelFontSize, color = color),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** フォルダ行の展開/折りたたみシェブロン。 */
@Composable
private fun FolderChevron(
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    KeiIcon(
        icon = if (expanded) KeiTheme.icons.chevronDown else KeiTheme.icons.chevronRight,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.TreeChevronSize),
    )
}

@Preview
@Composable
private fun ProjectTreePreview() {
    KeiTheme {
        ProjectTree(
            selectedPage = EditorPage.Profile,
            onClickPage = {},
        )
    }
}
