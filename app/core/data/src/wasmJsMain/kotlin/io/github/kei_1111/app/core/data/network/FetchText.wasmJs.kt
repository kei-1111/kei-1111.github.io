package io.github.kei_1111.app.core.data.network

import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.resume

private const val TIMEOUT_MS = 8_000
private const val HTTP_OK = 200

internal actual suspend fun fetchText(url: String): String? = suspendCancellableCoroutine { continuation ->
    val xhr = XMLHttpRequest()
    var resumed = false
    fun finish(result: String?) {
        if (!resumed) {
            resumed = true
            continuation.resume(result)
        }
    }
    continuation.invokeOnCancellation { xhr.abort() }
    // open()/send() は不正な URL などで同期例外を投げうるため、フォールバックを確実に効かせるよう握りつぶす。
    try {
        xhr.open("GET", url)
        xhr.timeout = TIMEOUT_MS
        xhr.onload = { finish(if (xhr.status.toInt() == HTTP_OK) xhr.responseText else null) }
        xhr.onerror = { finish(null) }
        xhr.ontimeout = { finish(null) }
        xhr.send()
    } catch (_: Throwable) {
        finish(null)
    }
}
