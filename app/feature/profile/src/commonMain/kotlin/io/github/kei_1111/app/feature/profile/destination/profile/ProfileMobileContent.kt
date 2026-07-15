@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.component.EditorCodeArea
import io.github.kei_1111.app.feature.profile.destination.profile.component.EditorPreviewIsland
import io.github.kei_1111.app.feature.profile.destination.profile.component.LeftToolRail
import io.github.kei_1111.app.feature.profile.destination.profile.component.PreviewPane
import io.github.kei_1111.app.feature.profile.destination.profile.component.ProjectTree
import io.github.kei_1111.app.feature.profile.destination.profile.component.StatusBar
import io.github.kei_1111.app.feature.profile.destination.profile.component.TitleBar
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.deskBackground

/** 900px 未満：ツリーはツールレールからオーバーレイで開閉、エディタ島はデフォルトで Preview 全体表示。 */
@Composable
internal fun ProfileMobileContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.profile == null) return

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
        MobileWorkspace(
            state = state,
            onClickToggleTree = { onIntent(ProfileIntent.ToggleTree(WindowLayout.Mobile)) },
            onClickPageFromTree = { onIntent(ProfileIntent.UpdateSelectedPageFromTree(it, WindowLayout.Mobile)) },
            onClickPage = { onIntent(ProfileIntent.UpdateSelectedPage(it)) },
            onChangeViewMode = { onIntent(ProfileIntent.UpdateViewMode(it, WindowLayout.Mobile)) },
            onClickUrl = { onIntent(ProfileIntent.OpenUrl(it)) },
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

/** TitleBar と StatusBar の間の本体。左ツールレールとエディタ領域を並べる。 */
@Composable
private fun MobileWorkspace(
    state: ProfileState,
    onClickToggleTree: () -> Unit,
    onClickPageFromTree: (EditorPage) -> Unit,
    onClickPage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onClickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // 右レールが無いため、右余白はタイトルバーのテーマ切替ボタン右端（DeskPadding）に揃える
            .padding(start = ProfileDimensions.RailMargin, end = ProfileDimensions.DeskPadding),
    ) {
        LeftToolRail(
            treeOpen = state.mobileTreeOpen,
            onClickToggleTree = onClickToggleTree,
        )
        Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
        MobileEditorArea(
            state = state,
            onClickPage = onClickPage,
            onChangeViewMode = onChangeViewMode,
            onClickUrl = onClickUrl,
            onClickPageFromTree = onClickPageFromTree,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

/** エディタ + プレビューの島とツリーオーバーレイを重ねる領域。 */
@Composable
private fun MobileEditorArea(
    state: ProfileState,
    onClickPage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickPageFromTree: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return
    // clipToBounds: ツリーのスライドイン/アウトを島の左端でマスクする
    Box(
        modifier = modifier.clipToBounds(),
    ) {
        EditorPreviewIsland(
            selectedPage = state.selectedPage,
            onClickPage = onClickPage,
            viewMode = state.mobileViewMode,
            onChangeViewMode = onChangeViewMode,
            showSplitButton = false,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (state.mobileViewMode == EditorViewMode.CodeOnly) {
                EditorCodeArea(
                    page = state.selectedPage,
                    profile = profile,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            } else {
                PreviewPane(
                    page = state.selectedPage,
                    profile = profile,
                    contributions = state.contributions,
                    onClickUrl = onClickUrl,
                    fitToWidth = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
        MobileTreeOverlay(
            visible = state.mobileTreeOpen,
            selectedPage = state.selectedPage,
            onClickPage = onClickPageFromTree,
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
