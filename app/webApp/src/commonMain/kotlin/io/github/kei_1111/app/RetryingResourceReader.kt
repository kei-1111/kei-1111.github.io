package io.github.kei_1111.app

import io.github.kei_1111.app.core.common.coroutines.retryWithBackoff
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.ResourceReader
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsException

// CMP resources は fetch 失敗後に再試行せず State が固まるため、リーダー層で成功まで再試行する。
@OptIn(ExperimentalResourceApi::class)
internal class RetryingResourceReader(
    private val delegate: ResourceReader,
) : ResourceReader {
    override suspend fun read(path: String): ByteArray =
        retryWithBackoff { normalizeJsFailure { delegate.read(path) } }

    override suspend fun readPart(path: String, offset: Long, size: Long): ByteArray =
        retryWithBackoff { normalizeJsFailure { delegate.readPart(path, offset, size) } }

    override fun getUri(path: String): String = delegate.getUri(path)
}

// wasm の fetch 拒否は Exception を継承しない JsException(Throwable 直下)で届き、
// そのままでは retryWithBackoff の再試行対象外のため Exception に正規化する。
@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun <T> normalizeJsFailure(block: suspend () -> T): T =
    try {
        block()
    } catch (e: JsException) {
        throw ResourceFetchFailedException(e)
    }

private class ResourceFetchFailedException(cause: Throwable) : Exception(cause)
