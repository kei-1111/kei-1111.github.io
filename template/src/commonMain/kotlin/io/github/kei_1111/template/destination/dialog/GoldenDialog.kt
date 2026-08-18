package io.github.kei_1111.template.destination.dialog

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun GoldenDialog(
    state: GoldenState,
    onIntent: (GoldenIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        // PLACEHOLDER: panel content; InlineDialogSceneStrategy owns overlay and positioning
    }
}
