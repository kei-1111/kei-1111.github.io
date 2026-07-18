@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.component.EditorCodeArea
import io.github.kei_1111.app.feature.profile.destination.profile.component.EditorPreviewIsland
import io.github.kei_1111.app.feature.profile.destination.profile.component.LeftToolRail
import io.github.kei_1111.app.feature.profile.destination.profile.component.PreviewPane
import io.github.kei_1111.app.feature.profile.destination.profile.component.ProjectTree
import io.github.kei_1111.app.feature.profile.destination.profile.component.RightToolRail
import io.github.kei_1111.app.feature.profile.destination.profile.component.StatusBar
import io.github.kei_1111.app.feature.profile.destination.profile.component.TitleBar
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.deskBackground
import io.github.kei_1111.shared.model.LicenseEntry

/** デスクトップ（横1180px基準）の Islands レイアウト。 */
@Composable
internal fun ProfileDesktopContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.profile == null) return
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
            DesktopWorkspace(
                state = state,
                onClickToggleTree = { onIntent(ProfileIntent.ToggleTree(WindowLayout.Desktop)) },
                onClickPageFromTree = { onIntent(ProfileIntent.UpdateSelectedPageFromTree(it, WindowLayout.Desktop)) },
                onClickPage = { onIntent(ProfileIntent.UpdateSelectedPage(it)) },
                onChangeViewMode = { onIntent(ProfileIntent.UpdateViewMode(it, WindowLayout.Desktop)) },
                onClickUrl = { onIntent(ProfileIntent.OpenUrl(it)) },
                onClickLicense = { onIntent(ProfileIntent.UpdateSelectedLicense(it)) },
                onDismissLicense = { onIntent(ProfileIntent.UpdateSelectedLicense(null)) },
                modifier = Modifier.weight(1f),
            )
            StatusBar(
                page = state.selectedPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ProfileDimensions.DeskPadding + 4.dp, vertical = 6.dp),
            )
        }
    }
}

/** TitleBar と StatusBar の間の本体。左ツールレール、プロジェクトツリー、エディタ + プレビューの島、右ツールレールを並べる。 */
@Composable
private fun DesktopWorkspace(
    state: ProfileState,
    onClickToggleTree: () -> Unit,
    onClickPageFromTree: (EditorPage) -> Unit,
    onClickPage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ProfileDimensions.RailMargin),
    ) {
        LeftToolRail(
            treeOpen = state.desktopTreeOpen,
            onClickToggleTree = onClickToggleTree,
        )
        Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
        DesktopTreePanel(
            visible = state.desktopTreeOpen,
            selectedPage = state.selectedPage,
            onClickPage = onClickPageFromTree,
        )
        EditorPreviewIsland(
            openPages = state.openPages,
            selectedPage = state.selectedPage,
            onClickPage = onClickPage,
            viewMode = state.desktopViewMode,
            onChangeViewMode = onChangeViewMode,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Row(modifier = Modifier.weight(1f)) {
                if (state.desktopViewMode != EditorViewMode.PreviewOnly) {
                    EditorCodeArea(
                        page = state.selectedPage,
                        profile = profile,
                        licenses = state.licenses,
                        modifier = Modifier
                            .weight(1.25f)
                            .fillMaxHeight(),
                    )
                }
                if (state.desktopViewMode == EditorViewMode.Split) {
                    VerticalDivider(color = KeiTheme.colors.outline, thickness = 1.dp)
                }
                if (state.desktopViewMode != EditorViewMode.CodeOnly) {
                    PreviewPane(
                        page = state.selectedPage,
                        profile = profile,
                        contributions = state.contributions,
                        licenses = state.licenses,
                        selectedLicense = state.selectedLicense,
                        onClickUrl = onClickUrl,
                        onClickLicense = onClickLicense,
                        onDismissLicense = onDismissLicense,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
        RightToolRail()
    }
}

/** ツールレール右のプロジェクトツリー（開閉アニメーション付き）。 */
@Composable
private fun DesktopTreePanel(
    visible: Boolean,
    selectedPage: EditorPage,
    onClickPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        Row(modifier = Modifier.fillMaxHeight()) {
            ProjectTree(
                selectedPage = selectedPage,
                onClickPage = onClickPage,
                modifier = Modifier.fillMaxHeight(),
                scrollable = true,
            )
            Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
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
