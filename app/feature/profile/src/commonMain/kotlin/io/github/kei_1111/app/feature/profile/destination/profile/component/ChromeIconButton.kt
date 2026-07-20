package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.TintedIcon
import io.github.kei_1111.app.core.ui.rememberHoverState
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions

/** 押せないアイコンの透過率。ProjectTree の押せない行と同じ値で統一する。 */
private const val NON_CLICKABLE_ICON_ALPHA = 0.45f

@Composable
internal fun ChromeIconButton(
    icon: TintedIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    iconSize: Dp = ProfileDimensions.RailIconSize,
    tint: Color = if (active) KeiTheme.colors.textPrimary else KeiTheme.colors.mutedHigh,
    onClick: (() -> Unit)? = null,
) {
    val hoverState = rememberHoverState()
    Box(
        modifier = modifier
            .size(ProfileDimensions.ChromePillSize)
            .clip(KeiTheme.shapes.pill)
            .background(if (active || (hoverState.hovered && onClick != null)) KeiTheme.colors.deskChip else Color.Transparent)
            .hoverable(hoverState.interactionSource)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(if (onClick != null) 1f else NON_CLICKABLE_ICON_ALPHA),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Preview
@Composable
private fun ChromeIconButtonPreview() {
    KeiTheme {
        ChromeIconButton(
            icon = KeiTheme.icons.toolWindowProject,
            contentDescription = null,
            active = true,
            onClick = {},
        )
    }
}
