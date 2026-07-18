@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions

/** 左端のツールウィンドウレール（幅30px）。Project のみタップ可能で、ツリーの開閉をトグルする。 */
@Composable
internal fun LeftToolRail(
    treeOpen: Boolean,
    onClickToggleTree: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(ProfileDimensions.RailWidth)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ChromeIconButton(
            icon = KeiTheme.icons.toolWindowProject,
            contentDescription = null,
            active = treeOpen,
            onClick = onClickToggleTree,
        )
        ChromeIconButton(icon = KeiTheme.icons.toolWindowCommit, contentDescription = null)
        ChromeIconButton(icon = KeiTheme.icons.merge, contentDescription = null)
        ChromeIconButton(icon = KeiTheme.icons.toolWindowBookmarks, contentDescription = null)
        Spacer(modifier = Modifier.weight(1f))
        ChromeIconButton(icon = KeiTheme.icons.toolWindowDebug, contentDescription = null)
        ChromeIconButton(icon = KeiTheme.icons.toolWindowLogcat, contentDescription = null)
        ChromeIconButton(icon = KeiTheme.icons.toolWindowTerminal, contentDescription = null)
    }
}

/** 右端のツールウィンドウレール（通知 / Gradle / Device Manager、装飾）。 */
@Composable
internal fun RightToolRail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(ProfileDimensions.RailWidth)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ChromeIconButton(icon = KeiTheme.icons.toolWindowNotifications, contentDescription = null)
        ChromeIconButton(icon = KeiTheme.icons.gradle, contentDescription = null)
        ChromeIconButton(icon = KeiTheme.icons.toolWindowDeviceManager, contentDescription = null)
    }
}

@Preview
@Composable
private fun LeftToolRailPreview() {
    KeiTheme {
        LeftToolRail(treeOpen = true, onClickToggleTree = {})
    }
}

@Preview
@Composable
private fun RightToolRailPreview() {
    KeiTheme {
        RightToolRail()
    }
}
