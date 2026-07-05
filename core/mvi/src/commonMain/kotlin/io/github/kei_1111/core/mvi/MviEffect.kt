package io.github.kei_1111.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * A Composable function that prevents forgetting to consume an Effect.
 *
 * If the Effect is non-null, it runs the handler and then automatically calls onConsume.
 *
 * @param effect the Effect to process (does nothing if null)
 * @param onConsume callback invoked after the Effect is processed (typically sends the ConsumeEffect Intent)
 * @param onHandle lambda that processes the Effect
 *
 * Usage:
 * ```
 * MviEffect(
 *     effect = state.effect,
 *     onConsume = { viewModel.onIntent(XxxIntent.ConsumeEffect) }
 * ) { effect ->
 *     when (effect) {
 *         is XxxEffect.NavigateBack -> currentNavigateBack()
 *         is XxxEffect.ShowToast -> Toast.makeText(...).show()
 *     }
 * }
 * ```
 */
@Composable
fun <E> MviEffect(
    effect: E?,
    onConsume: () -> Unit,
    onHandle: (E) -> Unit,
) {
    val currentOnConsume by rememberUpdatedState(onConsume)
    val currentOnHandle by rememberUpdatedState(onHandle)

    effect?.let { currentEffect ->
        LaunchedEffect(currentEffect) {
            currentOnHandle(currentEffect)
            currentOnConsume()
        }
    }
}
