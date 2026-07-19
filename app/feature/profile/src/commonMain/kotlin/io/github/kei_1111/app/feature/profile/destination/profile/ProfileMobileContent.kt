@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
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
import io.github.kei_1111.app.feature.profile.destination.profile.component.UsageCodeArea
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.deskBackground
import io.github.kei_1111.shared.model.LicenseEntry

/**
 * 900px 未満：ツリー表示中はエディタ + プレビューの島の上を全幅のツリー島で覆う
 * （島はコンポーズし続け、ズームやスクロール状態を保持する）。
 * 実 AS 同様アニメーションなしで切り替える。
 */
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
            onClosePage = { onIntent(ProfileIntent.ClosePage(it)) },
            onChangeViewMode = { onIntent(ProfileIntent.UpdateViewMode(it, WindowLayout.Mobile)) },
            onClickUrl = { onIntent(ProfileIntent.OpenUrl(it)) },
            onClickLicense = { onIntent(ProfileIntent.UpdateSelectedLicense(it)) },
            onDismissLicense = { onIntent(ProfileIntent.UpdateSelectedLicense(null)) },
            modifier = Modifier.weight(1f),
        )
        StatusBar(
            page = state.selectedPage,
            readOnly = state.selectedPage == EditorPage.Licenses,
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
    onClosePage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
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
            onClosePage = onClosePage,
            onChangeViewMode = onChangeViewMode,
            onClickUrl = onClickUrl,
            onClickLicense = onClickLicense,
            onDismissLicense = onDismissLicense,
            onClickPageFromTree = onClickPageFromTree,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

/**
 * ツリー表示中はエディタ + プレビューの島の上を全幅のツリー島で覆う
 * （島はコンポーズし続け、ズームやスクロール状態を保持する）。
 * 実 AS 同様アニメーションなしで切り替える領域。
 */
@Composable
private fun MobileEditorArea(
    state: ProfileState,
    onClickPage: (EditorPage) -> Unit,
    onClosePage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    onClickPageFromTree: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return
    Box(modifier = modifier) {
        // ツリー表示中も島をコンポーズし続けて zoom/スクロール状態を保持する
        // （if で外すと remember が破棄される）。後描画のツリーが全面を覆うのでタップもツリーが受ける
        EditorPreviewIsland(
            openPages = state.openPages,
            selectedPage = state.selectedPage,
            onClickPage = onClickPage,
            onClosePage = onClosePage,
            viewMode = state.mobileViewMode,
            onChangeViewMode = onChangeViewMode,
            showSplitButton = false,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (state.mobileTreeOpen) 0f else 1f),
        ) {
            val selectedPage = state.selectedPage
            if (selectedPage == null) {
                UsageCodeArea(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                if (state.mobileViewMode == EditorViewMode.CodeOnly) {
                    EditorCodeArea(
                        page = selectedPage,
                        profile = profile,
                        licenses = state.licenses,
                        locked = selectedPage == EditorPage.Licenses,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                } else {
                    PreviewPane(
                        page = selectedPage,
                        profile = profile,
                        contributions = state.contributions,
                        licenses = state.licenses,
                        selectedLicense = state.selectedLicense,
                        onClickUrl = onClickUrl,
                        onClickLicense = onClickLicense,
                        onDismissLicense = onDismissLicense,
                        fitToWidth = true,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
            }
        }
        if (state.mobileTreeOpen) {
            ProjectTree(
                selectedPage = state.selectedPage,
                onClickPage = onClickPageFromTree,
                // ツリーの空き領域（行リストの外）はポインタ入力ノードを持たず、タップが
                // 下の非表示の島の interactive 要素へ素通りするため、全域で入力を受けて遮蔽する
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures {} },
                scrollable = true,
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
