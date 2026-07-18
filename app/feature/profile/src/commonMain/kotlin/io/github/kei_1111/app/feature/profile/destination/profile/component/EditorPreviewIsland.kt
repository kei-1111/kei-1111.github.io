@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.app.feature.profile.destination.profile.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile

/**
 * エディタ + プレビューの島の共通枠。実 AS と同様、タブバーが島の全幅に渡り、
 * その右端の表示モード切替で Code / Split / Design を切り替える。
 * タブバー下の本体は Desktop / Mobile で異なるため [body] スロットで受け取る。
 * [showSplitButton] を false にすると Split ボタンを表示しない（Mobile 用）。
 */
@Composable
internal fun EditorPreviewIsland(
    selectedPage: EditorPage,
    onClickPage: (EditorPage) -> Unit,
    viewMode: EditorViewMode,
    onChangeViewMode: (EditorViewMode) -> Unit,
    modifier: Modifier = Modifier,
    showSplitButton: Boolean = true,
    body: @Composable ColumnScope.() -> Unit,
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
            showSplitButton = showSplitButton,
            modifier = Modifier
                .fillMaxWidth()
                .background(KeiTheme.colors.islandDark),
        )
        HorizontalDivider(color = KeiTheme.colors.outline, thickness = 1.dp)
        body()
    }
}

@Preview
@Composable
private fun EditorPreviewIslandPreview() {
    KeiTheme {
        // ColumnScope.weight は無限制約下で測定できないため、Preview では有限サイズを与える
        Box(modifier = Modifier.size(width = 640.dp, height = 480.dp)) {
            EditorPreviewIsland(
                selectedPage = EditorPage.Profile,
                onClickPage = {},
                viewMode = EditorViewMode.CodeOnly,
                onChangeViewMode = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                EditorCodeArea(
                    page = EditorPage.Profile,
                    profile = PreviewGitHubProfile,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }
    }
}
