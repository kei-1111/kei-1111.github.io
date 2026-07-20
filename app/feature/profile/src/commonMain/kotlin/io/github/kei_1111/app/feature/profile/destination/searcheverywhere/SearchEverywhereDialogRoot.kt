package io.github.kei_1111.app.feature.profile.destination.searcheverywhere

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kei_1111.app.core.designsystem.theme.KeiThemeController
import io.github.kei_1111.app.core.mvi.MviEffect
import io.github.kei_1111.app.core.navigation.LocalResultEventBus
import io.github.kei_1111.app.core.utils.openUrl
import io.github.kei_1111.app.feature.profile.navigation.SearchEverywhereResult

@Composable
internal fun SearchEverywhereDialogRoot(
    viewModel: SearchEverywhereViewModel,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentNavigateBack by rememberUpdatedState(navigateBack)
    val resultEventBus = LocalResultEventBus.current

    MviEffect(
        effect = state.effect,
        onConsume = { viewModel.onIntent(SearchEverywhereIntent.ConsumeEffect) },
    ) { effect ->
        when (effect) {
            SearchEverywhereEffect.NavigateBack -> currentNavigateBack()
            is SearchEverywhereEffect.ReturnPage -> {
                resultEventBus.sendResult(SearchEverywhereResult(page = effect.page))
                currentNavigateBack()
            }

            is SearchEverywhereEffect.OpenUrl -> {
                openUrl(effect.url)
                currentNavigateBack()
            }

            SearchEverywhereEffect.ToggleTheme -> {
                KeiThemeController.toggle()
                currentNavigateBack()
            }
        }
    }

    SearchEverywhereDialog(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
