package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kei_1111.app.core.mvi.MviEffect
import io.github.kei_1111.app.core.utils.openUrl

@Composable
internal fun ProfileScreenRoot(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MviEffect(
        effect = state.effect,
        onConsume = { viewModel.onIntent(ProfileIntent.ConsumeEffect) },
    ) { effect ->
        when (effect) {
            is ProfileEffect.OpenUrl -> openUrl(effect.url)
        }
    }

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
