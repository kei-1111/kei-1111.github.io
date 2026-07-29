package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kei_1111.app.core.mvi.MviEffect
import io.github.kei_1111.app.core.navigation.LocalResultEventBus
import io.github.kei_1111.app.core.utils.openUrl
import io.github.kei_1111.app.feature.profile.navigation.SearchEverywhereResult

@Composable
internal fun SearchEverywhereDialogRoot(
    viewModel: SearchEverywhereViewModel,
    navigateBack: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resultEventBus = LocalResultEventBus.current

    MviEffect(
        effect = state.effect,
        onConsume = { viewModel.onIntent(SearchEverywhereIntent.ConsumeEffect) },
    ) { effect ->
        when (effect) {
            is SearchEverywhereEffect.NavigateBack -> navigateBack()
            is SearchEverywhereEffect.ReturnPage -> {
                resultEventBus.sendResult(SearchEverywhereResult(page = effect.page))
                navigateBack()
            }

            is SearchEverywhereEffect.OpenUrl -> {
                openUrl(effect.url)
                navigateBack()
            }

            is SearchEverywhereEffect.ToggleTheme -> {
                onToggleTheme()
                navigateBack()
            }
        }
    }

    SearchEverywhereDialog(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
