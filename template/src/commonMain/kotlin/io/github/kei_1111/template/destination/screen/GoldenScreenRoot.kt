package io.github.kei_1111.template.destination.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun GoldenScreenRoot(
    viewModel: GoldenViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // PLACEHOLDER: when the destination has Effect variants, add MviEffect from the current ProfileScreenRoot or SplashScreenRoot pattern

    // PLACEHOLDER: environment bridges dispatching Intents (font loading, page visibility — see SplashScreenRoot), or delete

    GoldenScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
