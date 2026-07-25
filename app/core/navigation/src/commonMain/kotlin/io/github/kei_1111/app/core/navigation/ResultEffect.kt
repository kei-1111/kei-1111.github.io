package io.github.kei_1111.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlin.reflect.typeOf

/**
 * Copied from nav3-recipes' results/event pattern.
 *
 * An Effect composable for receiving a result of the specified type from a ResultEventBus.
 *
 * @param resultEventBus the ResultEventBus to receive results from
 * @param onResult callback invoked when a result is received
 * @see <a href="https://github.com/android/nav3-recipes">nav3-recipes</a>
 */
@Composable
inline fun <reified T : Any> ResultEffect(
    resultEventBus: ResultEventBus,
    noinline onResult: (T) -> Unit,
) {
    val currentOnResult by rememberUpdatedState(onResult)

    LaunchedEffect(resultEventBus) {
        resultEventBus.getResultFlow<T>(typeOf<T>()).collect { result ->
            currentOnResult(result)
        }
    }
}
