package io.github.kei_1111.template.destination.screen

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.kei_1111.app.core.designsystem.layout.WindowLayout
import io.github.kei_1111.app.core.designsystem.layout.windowLayoutFor
import io.github.kei_1111.template.destination.screen.content.GoldenDesktopContent
import io.github.kei_1111.template.destination.screen.content.GoldenMobileContent

@Composable
internal fun GoldenScreen(
    state: GoldenState,
    onIntent: (GoldenIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        val layout = windowLayoutFor(screenWidth)

        when (layout) {
            WindowLayout.Mobile -> GoldenMobileContent(state = state, onIntent = onIntent)
            WindowLayout.Desktop -> GoldenDesktopContent(state = state, onIntent = onIntent)
        }
    }
}
