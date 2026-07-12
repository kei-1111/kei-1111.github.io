@file:Suppress("MagicNumber", "ModifierMissing", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.TintedIcon
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions

/** 押せないアイコンの透過率。ProjectTree の押せない行と同じ値で統一する。 */
private const val NON_CLICKABLE_ICON_ALPHA = 0.45f

/** 左端のツールウィンドウレール（幅30px）。Project のみタップ可能で、ツリーの開閉をトグルする。 */
@Composable
internal fun ToolRail(
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
        RailIcon(
            icon = KeiTheme.icons.toolWindowProject,
            active = treeOpen,
            onClick = onClickToggleTree,
        )
        RailIcon(icon = KeiTheme.icons.toolWindowCommit)
        RailIcon(icon = KeiTheme.icons.merge)
        RailIcon(icon = KeiTheme.icons.toolWindowBookmarks)
        Spacer(modifier = Modifier.weight(1f))
        RailIcon(icon = KeiTheme.icons.toolWindowDebug)
        RailIcon(icon = KeiTheme.icons.toolWindowLogcat)
        RailIcon(icon = KeiTheme.icons.toolWindowTerminal)
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
        RailIcon(icon = KeiTheme.icons.toolWindowNotifications)
        RailIcon(icon = KeiTheme.icons.gradle)
        RailIcon(icon = KeiTheme.icons.toolWindowDeviceManager)
    }
}

@Composable
private fun RailIcon(
    icon: TintedIcon,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val clickable = onClick != null
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(KeiTheme.shapes.pill)
            .background(if (active || (hovered && clickable)) KeiTheme.colors.deskChip else Color.Transparent)
            .hoverable(interaction)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(if (clickable) 1f else NON_CLICKABLE_ICON_ALPHA),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = icon,
            contentDescription = null,
            tint = if (active) KeiTheme.colors.textPrimary else KeiTheme.colors.mutedHigh,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun ToolRailPreview() {
    KeiTheme {
        ToolRail(treeOpen = true, onClickToggleTree = {})
    }
}
