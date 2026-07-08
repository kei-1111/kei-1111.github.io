package io.github.kei_1111.core.designsystem.theme

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource

@Composable
fun KeiIcon(
    icon: ThemedIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(icon.current),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = Color.Unspecified,
    )
}

@Composable
fun KeiIcon(
    icon: TintedIcon,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(icon.resource),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}
