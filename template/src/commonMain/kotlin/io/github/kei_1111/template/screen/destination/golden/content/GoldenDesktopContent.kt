package io.github.kei_1111.template.screen.destination.golden.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.template.screen.destination.golden.GoldenIntent
import io.github.kei_1111.template.screen.destination.golden.GoldenState

@Composable
internal fun GoldenDesktopContent(
    state: GoldenState,
    onIntent: (GoldenIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // PLACEHOLDER: same-abstraction-level section components, converting child callbacks to Intents
    }
}

@Preview
@Composable
private fun GoldenDesktopContentPreview() {
    KeiTheme {
        GoldenDesktopContent(
            state = GoldenState(),
            onIntent = {},
        )
    }
}
