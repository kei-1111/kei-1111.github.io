@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.feature.profile.destination.profile

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.layout.WindowLayout
import io.github.kei_1111.core.designsystem.theme.KeiTheme
import io.github.kei_1111.feature.profile.destination.profile.component.EditorCodeArea
import io.github.kei_1111.feature.profile.destination.profile.component.EditorTabBar
import io.github.kei_1111.feature.profile.destination.profile.component.PreviewPane
import io.github.kei_1111.feature.profile.destination.profile.component.ProjectTree
import io.github.kei_1111.feature.profile.destination.profile.component.RightToolRail
import io.github.kei_1111.feature.profile.destination.profile.component.StatusBar
import io.github.kei_1111.feature.profile.destination.profile.component.TitleBar
import io.github.kei_1111.feature.profile.destination.profile.component.ToolRail
import io.github.kei_1111.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.feature.profile.theme.deskBackground
import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.GitHubProfile

/** デスクトップ（横1180px基準）の Islands レイアウト。 */
@Composable
internal fun ProfileDesktopContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return
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
                    .padding(
                        start = ProfileDimensions.DeskPadding,
                        top = 8.dp,
                        // 右端をテーマ切替ピル（30dp）と RightToolRail のピル列（幅 RailWidth=30dp）の
                        // 右端で揃えるため、DeskPadding ではなく RailMargin を使う
                        end = ProfileDimensions.RailMargin,
                        bottom = 8.dp,
                    ),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = ProfileDimensions.RailMargin),
            ) {
                ToolRail(
                    treeOpen = state.desktopTreeOpen,
                    onClickToggleTree = { onIntent(ProfileIntent.ToggleTree(WindowLayout.Desktop)) },
                )
                Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
                AnimatedVisibility(visible = state.desktopTreeOpen) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        ProjectTree(
                            selectedPage = state.selectedPage,
                            onClickPage = {
                                onIntent(ProfileIntent.UpdateSelectedPageFromTree(it, WindowLayout.Desktop))
                            },
                            modifier = Modifier.fillMaxHeight(),
                            scrollable = true,
                        )
                        Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
                    }
                }
                EditorPreviewIsland(
                    selectedPage = state.selectedPage,
                    onClickPage = { onIntent(ProfileIntent.UpdateSelectedPage(it)) },
                    viewMode = state.desktopViewMode,
                    onChangeViewMode = { onIntent(ProfileIntent.UpdateViewMode(it, WindowLayout.Desktop)) },
                    profile = profile,
                    contributions = state.contributions,
                    onClickUrl = { onIntent(ProfileIntent.OpenUrl(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
                RightToolRail()
            }
            StatusBar(
                page = state.selectedPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ProfileDimensions.DeskPadding + 4.dp, vertical = 6.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .background(KeiTheme.colors.islandDark),
        )
        HorizontalDivider(color = KeiTheme.colors.islandBorder, thickness = 1.dp)
        Row(modifier = Modifier.weight(1f)) {
            if (viewMode != EditorViewMode.PreviewOnly) {
                EditorCodeArea(
                    page = selectedPage,
                    profile = profile,
                    modifier = Modifier
                        .weight(1.25f)
                        .fillMaxHeight(),
                )
            }
            if (viewMode == EditorViewMode.Split) {
                VerticalDivider(color = KeiTheme.colors.islandBorder, thickness = 1.dp)
            }
            if (viewMode != EditorViewMode.CodeOnly) {
                PreviewPane(
                    page = selectedPage,
                    profile = profile,
                    contributions = contributions,
                    onClickUrl = onClickUrl,
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
    KeiTheme {
        // 内部の verticalScroll は無限制約下で測定できないため、Preview では有限サイズを与える
        Box(modifier = Modifier.size(width = 1280.dp, height = 800.dp)) {
            ProfileDesktopContent(
                state = ProfileState(profile = PreviewGitHubProfile),
                onIntent = {},
            )
        }
    }
}
