@file:Suppress("MagicNumber", "LongMethod", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.core.designsystem.theme.KeiTheme
import io.github.kei_1111.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.feature.profile.theme.themedIcon
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.ic_chevron_down_dark
import kei_1111.feature.profile.generated.resources.ic_chevron_right_dark
import kei_1111.feature.profile.generated.resources.ic_class_kotlin_dark
import kei_1111.feature.profile.generated.resources.ic_exclude_root_dark
import kei_1111.feature.profile.generated.resources.ic_folder_dark
import kei_1111.feature.profile.generated.resources.ic_ignored_dark
import kei_1111.feature.profile.generated.resources.ic_kotlin_dark
import kei_1111.feature.profile.generated.resources.ic_kotlin_gradle_script_dark
import kei_1111.feature.profile.generated.resources.ic_manifest_file_dark
import kei_1111.feature.profile.generated.resources.ic_markdown_dark
import kei_1111.feature.profile.generated.resources.ic_more_vertical_dark
import kei_1111.feature.profile.generated.resources.ic_package_dark
import kei_1111.feature.profile.generated.resources.ic_properties_dark
import kei_1111.feature.profile.generated.resources.ic_resources_root_dark
import kei_1111.feature.profile.generated.resources.ic_source_root_dark
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
    selectedPage: EditorPage,
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
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = KeiTheme.colors.textPrimary,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(themedIcon(Res.drawable.ic_chevron_down_dark)),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(themedIcon(Res.drawable.ic_more_vertical_dark)),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Unspecified,
        )
    }
}

@Composable
private fun TreeRows(
    selectedPage: EditorPage,
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
            icon = Res.drawable.ic_exclude_root_dark,
        )
        FolderRow(
            depth = 1,
            expanded = false,
            label = ".idea",
            icon = Res.drawable.ic_exclude_root_dark,
        )
        FolderRow(
            depth = 1,
            expanded = false,
            label = ".kotlin",
            icon = Res.drawable.ic_exclude_root_dark,
        )
        FolderRow(depth = 1, expanded = true, label = "app")
        FolderRow(
            depth = 2,
            expanded = false,
            label = "build",
            icon = Res.drawable.ic_exclude_root_dark,
        )
        FolderRow(depth = 2, expanded = true, label = "src")
        FolderRow(depth = 3, expanded = false, label = "androidTest")
        FolderRow(depth = 3, expanded = true, label = "main")
        FolderRow(
            depth = 4,
            expanded = true,
            label = "kotlin",
            icon = Res.drawable.ic_source_root_dark,
        )
        FolderRow(
            depth = 5,
            expanded = true,
            label = "io.github.kei_1111",
            icon = Res.drawable.ic_package_dark,
        )
        FolderRow(depth = 6, expanded = true, label = "theme", icon = Res.drawable.ic_package_dark)
        FileRow(depth = 7, label = "Color.kt", icon = Res.drawable.ic_kotlin_dark)
        FileRow(depth = 7, label = "Theme.kt", icon = Res.drawable.ic_kotlin_dark)
        FileRow(depth = 7, label = "Type.kt", icon = Res.drawable.ic_kotlin_dark)
        FolderRow(
            depth = 6,
            expanded = true,
            label = "ui.profile",
            icon = Res.drawable.ic_package_dark,
        )
        FileRow(
            depth = 7,
            label = "ProfileScreen.kt",
            icon = Res.drawable.ic_kotlin_dark,
            selected = selectedPage == EditorPage.Profile,
            onClick = { onClickPage(EditorPage.Profile) },
        )
        FileRow(depth = 7, label = "GitHubProfileData", icon = Res.drawable.ic_class_kotlin_dark)
        FileRow(depth = 6, label = "MainActivity", icon = Res.drawable.ic_class_kotlin_dark)
        FolderRow(
            depth = 4,
            expanded = false,
            label = "res",
            icon = Res.drawable.ic_resources_root_dark,
        )
        FileRow(
            depth = 4,
            label = "AndroidManifest.xml",
            icon = Res.drawable.ic_manifest_file_dark,
        )
        FileRow(depth = 2, label = ".gitignore", icon = Res.drawable.ic_ignored_dark)
        FileRow(
            depth = 2,
            label = "build.gradle.kts",
            icon = Res.drawable.ic_kotlin_gradle_script_dark,
        )
        FolderRow(
            depth = 1,
            expanded = false,
            label = "build",
            icon = Res.drawable.ic_exclude_root_dark,
        )
        FolderRow(depth = 1, expanded = false, label = "gradle")
        FileRow(
            depth = 1,
            label = "build.gradle.kts",
            icon = Res.drawable.ic_kotlin_gradle_script_dark,
        )
        FileRow(depth = 1, label = "gradle.properties", icon = Res.drawable.ic_properties_dark)
        FileRow(depth = 1, label = "README.md", icon = Res.drawable.ic_markdown_dark)
        FileRow(
            depth = 1,
            label = "settings.gradle.kts",
            icon = Res.drawable.ic_kotlin_gradle_script_dark,
        )
    }
}

@Composable
private fun FolderRow(
    depth: Int,
    expanded: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    icon: DrawableResource = Res.drawable.ic_folder_dark,
    onClick: (() -> Unit)? = null,
) {
    TreeRow(depth = depth, modifier = modifier, onClick = onClick) {
        Icon(
            painter = painterResource(
                themedIcon(if (expanded) Res.drawable.ic_chevron_down_dark else Res.drawable.ic_chevron_right_dark),
            ),
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.TreeChevronSize),
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(ProfileDimensions.TreeChevronGap))
        TreeIcon(icon)
        Spacer(modifier = Modifier.width(ProfileDimensions.TreeIconLabelGap))
        Text(
            text = label,
            style = KeiTheme.typography.chrome.copy(fontSize = 12.sp, color = KeiTheme.colors.textPrimary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FileRow(
    depth: Int,
    label: String,
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    TreeRow(depth = depth, modifier = modifier, selected = selected, onClick = onClick) {
        Spacer(modifier = Modifier.width(ProfileDimensions.TreeChevronSize + ProfileDimensions.TreeChevronGap))
        TreeIcon(icon)
        Spacer(modifier = Modifier.width(ProfileDimensions.TreeIconLabelGap))
        Text(
            text = label,
            style = KeiTheme.typography.chrome.copy(
                fontSize = 12.sp,
                color = if (selected) KeiTheme.colors.textPrimary else KeiTheme.colors.textCode,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val clickable = onClick != null
    val background = when {
        selected -> KeiTheme.colors.selectionPill
        hovered && clickable -> KeiTheme.colors.chip
        else -> Color.Transparent
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(KeiTheme.shapes.row)
            .background(background)
            .hoverable(interaction)
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
    icon: DrawableResource,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(themedIcon(icon)),
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.TreeIconSize),
        tint = Color.Unspecified,
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
