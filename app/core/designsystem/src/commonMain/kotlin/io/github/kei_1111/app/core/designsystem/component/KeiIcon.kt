package io.github.kei_1111.app.core.designsystem.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.kei_1111.app.core.designsystem.theme.TintedIcon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * 多色の焼き込みアートを再着色しない（tint=Color.Unspecified 固定）。
 * 明暗の出し分けは drawable / drawable-dark の修飾子でリソース側が解決する。
 */
@Composable
fun KeiIcon(
    icon: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(icon),
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
