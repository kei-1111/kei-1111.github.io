@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.component.EditorCodeArea
import io.github.kei_1111.app.feature.profile.destination.profile.component.EditorPreviewIsland
import io.github.kei_1111.app.feature.profile.destination.profile.component.LeftToolRail
import io.github.kei_1111.app.feature.profile.destination.profile.component.PreviewPane
import io.github.kei_1111.app.feature.profile.destination.profile.component.ProjectTree
import io.github.kei_1111.app.feature.profile.destination.profile.component.ReadmeSource
import io.github.kei_1111.app.feature.profile.destination.profile.component.RightToolRail
import io.github.kei_1111.app.feature.profile.destination.profile.component.StatusBar
import io.github.kei_1111.app.feature.profile.destination.profile.component.TitleBar
import io.github.kei_1111.app.feature.profile.destination.profile.component.UsageCodeArea
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.deskBackground
import io.github.kei_1111.shared.model.LicenseEntry

/** エディタペインの初期幅比。 */
private const val DEFAULT_EDITOR_PANE_FRACTION = 1.25f / 2.25f

/** エディタペインの最小幅比。 */
private const val MIN_PANE_FRACTION = 0.2f

/** エディタペインの最大幅比。 */
private const val MAX_PANE_FRACTION = 0.8f

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
                onClickBuild = { onIntent(ProfileIntent.ResetEditorCode) },
            )
            DesktopWorkspace(
                state = state,
                onClickToggleTree = { onIntent(ProfileIntent.ToggleTree(WindowLayout.Desktop)) },
                onClickPageFromTree = { onIntent(ProfileIntent.UpdateSelectedPageFromTree(it, WindowLayout.Desktop)) },
                onClickPage = { onIntent(ProfileIntent.UpdateSelectedPage(it)) },
                onClosePage = { onIntent(ProfileIntent.ClosePage(it)) },
                onChangeViewMode = { onIntent(ProfileIntent.UpdateViewMode(it, WindowLayout.Desktop)) },
                onChangeCode = { page, code ->
                    onIntent(
                        if (page == EditorPage.Readme) {
                            ProfileIntent.UpdateReadmeCode(code)
                        } else {
                            ProfileIntent.UpdateProfileCode(code)
                        },
                    )
                },
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
}

/** TitleBar と StatusBar の間の本体。左ツールレール、プロジェクトツリー、エディタ + プレビューの島、右ツールレールを並べる。 */
@Composable
private fun DesktopWorkspace(
    state: ProfileState,
    onClickToggleTree: () -> Unit,
    onClickPageFromTree: (EditorPage) -> Unit,
    onClickPage: (EditorPage) -> Unit,
    onClosePage: (EditorPage) -> Unit,
    onChangeViewMode: (EditorViewMode) -> Unit,
    onChangeCode: (EditorPage, String) -> Unit,
    onClickUrl: (String) -> Unit,
    onClickLicense: (LicenseEntry) -> Unit,
    onDismissLicense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return
    var editorPaneFraction by remember { mutableFloatStateOf(DEFAULT_EDITOR_PANE_FRACTION) }
    var editorBodyWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
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
            onClosePage = onClosePage,
            viewMode = state.desktopViewMode,
            onChangeViewMode = onChangeViewMode,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            val selectedPage = state.selectedPage
            if (selectedPage == null) {
                UsageCodeArea(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                val isSplit = state.desktopViewMode == EditorViewMode.Split
                val editorWeight = if (isSplit) editorPaneFraction else 1f
                val previewWeight = if (isSplit) 1f - editorPaneFraction else 1f
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .onSizeChanged { editorBodyWidthPx = it.width },
                ) {
                    if (state.desktopViewMode != EditorViewMode.PreviewOnly) {
                        EditorCodeArea(
                            page = selectedPage,
                            profile = profile,
                            licenses = state.licenses,
                            editorCode = if (selectedPage == EditorPage.Readme) {
                                state.readmeEditorCode
                            } else {
                                state.profileEditorCode
                            },
                            editable = true,
                            onChangeCode = { onChangeCode(selectedPage, it) },
                            codeHasError = selectedPage == EditorPage.Profile && state.profileCodeError,
                            editorResetTick = state.editorResetTick,
                            locked = selectedPage == EditorPage.Licenses,
                            modifier = Modifier
                                .weight(editorWeight)
                                .fillMaxHeight(),
                        )
                    }
                    if (isSplit) {
                        SplitDragHandle(
                            onDrag = { delta ->
                                val paneAreaWidthPx = editorBodyWidthPx -
                                    with(density) { ProfileDimensions.SplitHandleHitWidth.roundToPx() }
                                if (paneAreaWidthPx > 0) {
                                    editorPaneFraction = (editorPaneFraction + delta / paneAreaWidthPx)
                                        .coerceIn(MIN_PANE_FRACTION, MAX_PANE_FRACTION)
                                }
                            },
                        )
                    }
                    if (state.desktopViewMode != EditorViewMode.CodeOnly) {
                        PreviewPane(
                            page = selectedPage,
                            profile = profile,
                            contributions = state.contributions,
                            licenses = state.licenses,
                            selectedLicense = state.selectedLicense,
                            onClickUrl = onClickUrl,
                            onClickLicense = onClickLicense,
                            onDismissLicense = onDismissLicense,
                            upToDate = selectedPage != EditorPage.Profile || !state.profileCodeError,
                            readmeBlocks = state.readmeBlocks,
                            modifier = Modifier
                                .weight(previewWeight)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(ProfileDimensions.IslandGap))
        RightToolRail()
    }
}

/** ツールレール右のプロジェクトツリー。実 AS と同様、開閉は即時（アニメーションなし）。 */
@Composable
private fun DesktopTreePanel(
    visible: Boolean,
    selectedPage: EditorPage?,
    onClickPage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (visible) {
        Row(modifier = modifier.fillMaxHeight()) {
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

/** ドラッグでエディタとプレビューの分割比を変えるディバイダ。 */
@Composable
private fun SplitDragHandle(
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    // つかみ領域ぶんの幅を確保して中央に 1dp の罫線を描く。両ペインと同じ島色の上なので
    // 罫線以外は見えない（親境界外の子はヒットテストされないため、はみ出しでは拡げられない）
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(ProfileDimensions.SplitHandleHitWidth)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState(onDrag),
            ),
        contentAlignment = Alignment.Center,
    ) {
        VerticalDivider(color = KeiTheme.colors.outline, thickness = 1.dp)
    }
}

@Preview
@Composable
private fun ProfileDesktopContentPreview() {
    KeiTheme {
        // 内部の verticalScroll は無限制約下で測定できないため、Preview では有限サイズを与える
        Box(modifier = Modifier.size(width = 1280.dp, height = 800.dp)) {
            ProfileDesktopContent(
                state = ProfileState(
                    profile = PreviewGitHubProfile,
                    profileEditorCode = profileCode(PreviewGitHubProfile),
                    readmeEditorCode = ReadmeSource,
                ),
                onIntent = {},
            )
        }
    }
}
