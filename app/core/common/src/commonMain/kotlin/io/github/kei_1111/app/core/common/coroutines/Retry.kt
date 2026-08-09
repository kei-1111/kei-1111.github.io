package io.github.kei_1111.app.core.common.coroutines

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private sealed interface RetryResult<out T> {
    data class Success<T>(val value: T) : RetryResult<T>
    data object Failure : RetryResult<Nothing>
}

/** [block] を成功するまで指数バックオフで再試行する（null の返却も成功）。cancellation は回復せず必ず伝播する。 */
suspend fun <T> retryWithBackoff(
    initialDelay: Duration = 1.seconds,
    maxDelay: Duration = 16.seconds,
    factor: Double = 2.0,
    block: suspend () -> T,
): T {
    var currentDelay = initialDelay
    while (true) {
        when (
            val result = recoverOrElse<RetryResult<T>>(
                block = { RetryResult.Success(block()) },
                onFailure = { RetryResult.Failure },
            )
        ) {
            is RetryResult.Success -> return result.value
            RetryResult.Failure -> {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).coerceAtMost(maxDelay)
            }
        }
    }
}
