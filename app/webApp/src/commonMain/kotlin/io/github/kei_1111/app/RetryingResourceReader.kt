package io.github.kei_1111.app

import io.github.kei_1111.app.core.common.coroutines.retryWithBackoff
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.ResourceReader

// CMP resources は fetch 失敗後に再試行せず State が固まるため、リーダー層で成功まで再試行する。
@OptIn(ExperimentalResourceApi::class)
internal class RetryingResourceReader(
    private val delegate: ResourceReader,
) : ResourceReader {
    override suspend fun read(path: String): ByteArray = retryWithBackoff { delegate.read(path) }

    override suspend fun readPart(path: String, offset: Long, size: Long): ByteArray =
        retryWithBackoff { delegate.readPart(path, offset, size) }

    override fun getUri(path: String): String = delegate.getUri(path)
}
