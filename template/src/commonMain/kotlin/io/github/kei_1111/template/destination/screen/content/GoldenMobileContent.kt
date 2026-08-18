package io.github.kei_1111.template.destination.screen.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.template.destination.screen.GoldenIntent
import io.github.kei_1111.template.destination.screen.GoldenState

@Composable
internal fun GoldenMobileContent(
    state: GoldenState,
    onIntent: (GoldenIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // PLACEHOLDER: mobile layout of the same sections as DesktopContent (overlay tree, stacked panes, etc.)
    }
}

@Preview
@Composable
private fun GoldenMobileContentPreview() {
    KeiTheme {
        GoldenMobileContent(
            state = GoldenState(),
            onIntent = {},
        )
    }
}
