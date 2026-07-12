@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile.destination.profile

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.layout.WindowLayout
import io.github.kei_1111.core.designsystem.theme.KeiTheme
import io.github.kei_1111.feature.profile.destination.profile.component.EditorCodeArea
import io.github.kei_1111.feature.profile.destination.profile.component.EditorTabBar
import io.github.kei_1111.feature.profile.destination.profile.component.PreviewPane
import io.github.kei_1111.feature.profile.destination.profile.component.ProjectTree
import io.github.kei_1111.feature.profile.destination.profile.component.StatusBar
import io.github.kei_1111.feature.profile.destination.profile.component.TitleBar
import io.github.kei_1111.feature.profile.destination.profile.component.ToolRail
import io.github.kei_1111.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.feature.profile.theme.deskBackground
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile

/** 900px 未満：ツリーはツールレールからオーバーレイで開閉、エディタ島はデフォルトで Preview 全体表示。 */
@Composable
internal fun ProfileMobileContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .deskBackground(),
    ) {
        TitleBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProfileDimensions.DeskPadding, vertical = 8.dp),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // 右レールが無いため、右余白はタイトルバーのテーマ切替ボタン右端（DeskPadding）に揃える
                .padding(start = ProfileDimensions.RailMargin, end = ProfileDimensions.DeskPadding),
        ) {
            ToolRail(
                treeOpen = state.mobileTreeOpen,
                onClickToggleTree = { onIntent(ProfileIntent.ToggleTree(WindowLayout.Mobile)) },
            )
            Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
            // clipToBounds: ツリーのスライドイン/アウトを島の左端でマスクする
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds(),
            ) {
                MobileEditorPreviewIsland(
                    selectedPage = state.selectedPage,
                    onClickPage = { onIntent(ProfileIntent.UpdateSelectedPage(it)) },
                    viewMode = state.mobileViewMode,
                    onChangeViewMode = { onIntent(ProfileIntent.UpdateViewMode(it, WindowLayout.Mobile)) },
                    profile = profile,
                    contributions = state.contributions,
                    onClickUrl = { onIntent(ProfileIntent.OpenUrl(it)) },
                    modifier = Modifier.fillMaxSize(),
                )
                MobileTreeOverlay(
                    visible = state.mobileTreeOpen,
                    selectedPage = state.selectedPage,
                    onClickPage = { onIntent(ProfileIntent.UpdateSelectedPageFromTree(it, WindowLayout.Mobile)) },
                )
            }
        }
        StatusBar(
            page = state.selectedPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProfileDimensions.DeskPadding + 4.dp, vertical = 6.dp),
        )
    }
}

/** ツールレールから開くプロジェクトツリー。エディタ島の上に左からスライドインで重ねて表示する。 */
@Composable
private fun MobileTreeOverlay(
    visible: Boolean,
    selectedPage: EditorPage,
    onClickPage: (EditorPage) -> Unit,
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
            onClickPage = onClickPage,
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
    onClickPage: (EditorPage) -> Unit,
    viewMode: EditorViewMode,
    onChangeViewMode: (EditorViewMode) -> Unit,
    profile: GitHubProfile,
    contributions: ContributionCalendar?,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(KeiTheme.shapes.island)
            .background(KeiTheme.colors.island),
    ) {
        EditorTabBar(
            selectedPage = selectedPage,
            onClickPage = onClickPage,
            viewMode = viewMode,
            onChangeViewMode = onChangeViewMode,
            showSplitButton = false,
            modifier = Modifier
                .fillMaxWidth()
                .background(KeiTheme.colors.islandDark),
        )
        HorizontalDivider(color = KeiTheme.colors.islandBorder, thickness = 1.dp)
        if (viewMode == EditorViewMode.CodeOnly) {
            EditorCodeArea(
                page = selectedPage,
                profile = profile,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else {
            PreviewPane(
                page = selectedPage,
                profile = profile,
                contributions = contributions,
                onClickUrl = onClickUrl,
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
    KeiTheme {
        // weight ベースの固定レイアウトは無限制約下で測定できないため、Preview では有限サイズを与える
        Box(modifier = Modifier.size(width = 390.dp, height = 820.dp)) {
            ProfileMobileContent(
                state = ProfileState(profile = PreviewGitHubProfile),
                onIntent = {},
            )
        }
    }
}
