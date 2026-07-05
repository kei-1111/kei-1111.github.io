@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.theme.AppTheme
import io.github.kei_1111.core.designsystem.theme.IdeColors
import io.github.kei_1111.feature.profile.component.EditorCodeArea
import io.github.kei_1111.feature.profile.component.EditorTabBar
import io.github.kei_1111.feature.profile.component.PreviewPane
import io.github.kei_1111.feature.profile.component.ProjectTree
import io.github.kei_1111.feature.profile.component.RightToolRail
import io.github.kei_1111.feature.profile.component.StatusBar
import io.github.kei_1111.feature.profile.component.TitleBar
import io.github.kei_1111.feature.profile.component.ToolRail

/** デスクトップ（横1180px基準）の Islands レイアウト。 */
@Composable
internal fun ProfileDesktopContent(
    selectedPage: EditorPage,
    onSelectPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var treeOpen by remember { mutableStateOf(true) }
    var viewMode by remember { mutableStateOf(EditorViewMode.Split) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .deskBackground(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                AnimatedVisibility(visible = treeOpen) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        ProjectTree(
                            selectedPage = selectedPage,
                            onSelectPage = onSelectPage,
                            modifier = Modifier.fillMaxHeight(),
                            scrollable = true,
                        )
                        Spacer(modifier = Modifier.width(IdeDimens.IslandGap))
                    }
                }
                EditorPreviewIsland(
                    selectedPage = selectedPage,
                    onSelectPage = onSelectPage,
                    viewMode = viewMode,
                    onSelectViewMode = { viewMode = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Spacer(modifier = Modifier.width(IdeDimens.IslandGap))
                RightToolRail()
            }
            StatusBar(
                page = selectedPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = IdeDimens.DeskPadding + 4.dp, vertical = 6.dp),
            )
        }
    }
}

/**
 * エディタ + プレビューの島。実 AS と同様、タブバーが島の全幅に渡り、
 * その右端の表示モード切替で Code / Split / Design を切り替える。
 */
@Composable
private fun EditorPreviewIsland(
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
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeColors.IslandDark),
        )
        HorizontalDivider(color = IdeColors.IslandBorder, thickness = 1.dp)
        Row(modifier = Modifier.weight(1f)) {
            if (viewMode != EditorViewMode.PreviewOnly) {
                EditorCodeArea(
                    page = selectedPage,
                    modifier = Modifier
                        .weight(1.25f)
                        .fillMaxHeight(),
                )
            }
            if (viewMode == EditorViewMode.Split) {
                VerticalDivider(color = IdeColors.IslandBorder, thickness = 1.dp)
            }
            if (viewMode != EditorViewMode.CodeOnly) {
                PreviewPane(
                    page = selectedPage,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProfileDesktopContentPreview() {
    AppTheme(darkTheme = true) {
        // 内部の verticalScroll は無限制約下で測定できないため、Preview では有限サイズを与える
        Box(modifier = Modifier.size(width = 1280.dp, height = 800.dp)) {
            ProfileDesktopContent(
                selectedPage = EditorPage.Profile,
                onSelectPage = {},
            )
        }
    }
}
