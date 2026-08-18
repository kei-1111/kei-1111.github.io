package io.github.kei_1111.template.destination.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun GoldenDialogRoot(
    viewModel: GoldenViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // PLACEHOLDER: when the dialog has Effect variants, add MviEffect and result/back dependencies per SearchEverywhereDialogRoot

    GoldenDialog(
        state = state,
        onIntent = viewModel::onIntent,
    )
}
