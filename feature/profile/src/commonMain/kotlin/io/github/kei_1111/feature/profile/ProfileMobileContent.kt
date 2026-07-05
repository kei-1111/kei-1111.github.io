@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.theme.AppTheme
import io.github.kei_1111.core.designsystem.theme.IdeColors
import io.github.kei_1111.feature.profile.component.EditorCodeArea
import io.github.kei_1111.feature.profile.component.EditorTabBar
import io.github.kei_1111.feature.profile.component.PreviewPane
import io.github.kei_1111.feature.profile.component.ProjectTree
import io.github.kei_1111.feature.profile.component.StatusBar
import io.github.kei_1111.feature.profile.component.TitleBar
import io.github.kei_1111.feature.profile.component.ToolRail

/** 900px 未満：ツリーはツールレールからオーバーレイで開閉、エディタ島はデフォルトで Preview 全体表示。 */
@Composable
internal fun ProfileMobileContent(
    selectedPage: EditorPage,
    onSelectPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var treeOpen by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(EditorViewMode.PreviewOnly) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .deskBackground(),
    ) {
        TitleBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = IdeDimens.DeskPadding, vertical = 8.dp),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = IdeDimens.RailMargin),
        ) {
            ToolRail(
                treeOpen = treeOpen,
                onToggleTree = { treeOpen = !treeOpen },
            )
            Spacer(modifier = Modifier.width(IdeDimens.IslandGap))
            // clipToBounds: ツリーのスライドイン/アウトを島の左端でマスクする
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds(),
            ) {
                MobileEditorPreviewIsland(
                    selectedPage = selectedPage,
                    onSelectPage = onSelectPage,
                    viewMode = viewMode,
                    onSelectViewMode = { viewMode = it },
                    modifier = Modifier.fillMaxSize(),
                )
                MobileTreeOverlay(
                    visible = treeOpen,
                    selectedPage = selectedPage,
                    onSelectPage = {
                        onSelectPage(it)
                        treeOpen = false
                    },
                )
            }
        }
        StatusBar(
            page = selectedPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = IdeDimens.DeskPadding + 4.dp, vertical = 6.dp),
        )
    }
}

/** ツールレールから開くプロジェクトツリー。エディタ島の上に左からスライドインで重ねて表示する。 */
@Composable
private fun MobileTreeOverlay(
    visible: Boolean,
    selectedPage: EditorPage,
    onSelectPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInHorizontally { -it },
        exit = slideOutHorizontally { -it },
    ) {
        ProjectTree(
            selectedPage = selectedPage,
            onSelectPage = onSelectPage,
            modifier = Modifier.fillMaxHeight(),
            scrollable = true,
        )
    }
}

/**
 * エディタ + プレビューの島（Mobile 版）。Code / Design の2モードのみで、
 * Design ではプレビューを島の横幅いっぱいに表示する（縦はスクロール）。
 */
@Composable
private fun MobileEditorPreviewIsland(
    selectedPage: EditorPage,
    onSelectPage: (EditorPage) -> Unit,
    viewMode: EditorViewMode,
    onSelectViewMode: (EditorViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(IdeDimens.IslandShape)
            .background(IdeColors.Island),
    ) {
        EditorTabBar(
            selectedPage = selectedPage,
            onSelectPage = onSelectPage,
            viewMode = viewMode,
            onSelectViewMode = onSelectViewMode,
            showSplitButton = false,
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeColors.IslandDark),
        )
        HorizontalDivider(color = IdeColors.IslandBorder, thickness = 1.dp)
        if (viewMode == EditorViewMode.CodeOnly) {
            EditorCodeArea(
                page = selectedPage,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else {
            PreviewPane(
                page = selectedPage,
                fitToWidth = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun ProfileMobileContentPreview() {
    AppTheme(darkTheme = true) {
        // weight ベースの固定レイアウトは無限制約下で測定できないため、Preview では有限サイズを与える
        Box(modifier = Modifier.size(width = 390.dp, height = 820.dp)) {
            ProfileMobileContent(
                selectedPage = EditorPage.Profile,
                onSelectPage = {},
            )
        }
    }
}
