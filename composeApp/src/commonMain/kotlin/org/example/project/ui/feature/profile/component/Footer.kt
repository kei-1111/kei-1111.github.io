package org.example.project.ui.feature.profile.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.ui.component.LabelMediumText
import org.example.project.ui.theme.dimensions.Paddings

@Composable
fun Footer(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
    ) {
        LabelMediumText(
            text = "© 2025 kei-1111 | Build with CMP (WasmJs)",
            modifier = Modifier.padding(
                horizontal = Paddings.Content,
                vertical = Paddings.Small,
            ),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
