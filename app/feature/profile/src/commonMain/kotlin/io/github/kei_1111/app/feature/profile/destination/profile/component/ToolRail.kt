@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.tool_rail_logcat
import kei_1111.app.feature.profile.generated.resources.tool_rail_project
import kei_1111.app.feature.profile.generated.resources.tool_rail_terminal
import kei_1111.app.feature.profile.generated.resources.tool_rail_todo
import org.jetbrains.compose.resources.stringResource

/** 左端のツールウィンドウレール（幅30px）。Project / Logcat / TODO / Terminal の開閉をトグルする。 */
@Composable
internal fun LeftToolRail(
    treeOpen: Boolean,
    onClickToggleTree: () -> Unit,
    logcatOpen: Boolean,
    onClickToggleLogcat: () -> Unit,
    todoOpen: Boolean,
    onClickToggleTodo: () -> Unit,
    terminalOpen: Boolean,
    onClickToggleTerminal: () -> Unit,
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
            contentDescription = stringResource(Res.string.tool_rail_project),
            active = treeOpen,
            onClick = onClickToggleTree,
            modifier = Modifier.testTag(TestTags.Profile.TOOL_RAIL_PROJECT),
        )
        ChromeIconButton(icon = KeiTheme.icons.toolWindowCommit, contentDescription = null)
        ChromeIconButton(icon = KeiTheme.icons.merge, contentDescription = null)
        ChromeIconButton(icon = KeiTheme.icons.toolWindowBookmarks, contentDescription = null)
        Spacer(modifier = Modifier.weight(1f))
        ChromeIconButton(icon = KeiTheme.icons.toolWindowDebug, contentDescription = null)
        ChromeIconButton(
            icon = KeiTheme.icons.toolWindowLogcat,
            contentDescription = stringResource(Res.string.tool_rail_logcat),
            active = logcatOpen,
            onClick = onClickToggleLogcat,
            modifier = Modifier.testTag(TestTags.Profile.TOOL_RAIL_LOGCAT),
        )
        ChromeIconButton(
            icon = KeiTheme.icons.toolWindowTodo,
            contentDescription = stringResource(Res.string.tool_rail_todo),
            active = todoOpen,
            onClick = onClickToggleTodo,
            modifier = Modifier.testTag(TestTags.Profile.TOOL_RAIL_TODO_TOGGLE),
        )
        ChromeIconButton(
            icon = KeiTheme.icons.toolWindowTerminal,
            contentDescription = stringResource(Res.string.tool_rail_terminal),
            active = terminalOpen,
            onClick = onClickToggleTerminal,
            modifier = Modifier.testTag(TestTags.Profile.TOOL_RAIL_TERMINAL),
        )
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
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(8.dp)) {
            LeftToolRail(
                treeOpen = true,
                onClickToggleTree = {},
                logcatOpen = false,
                onClickToggleLogcat = {},
                todoOpen = false,
                onClickToggleTodo = {},
                terminalOpen = false,
                onClickToggleTerminal = {},
            )
        }
    }
}

@Preview
@Composable
private fun RightToolRailPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.desk).padding(8.dp)) {
            RightToolRail()
        }
    }
}
