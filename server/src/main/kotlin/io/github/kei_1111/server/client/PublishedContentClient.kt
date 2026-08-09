package io.github.kei_1111.server.client

import com.google.cloud.http.HttpTransportOptions
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.StorageOptions
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.Works
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

internal sealed interface PublishedResult<out T : Any> {
    data class Found<T : Any>(val value: T) : PublishedResult<T>
    data object Missing : PublishedResult<Nothing>
}

internal fun <T : Any> PublishedResult<T>?.valueOrNull(): T? = (this as? PublishedResult.Found)?.value

internal interface PublishedContentClient {
    suspend fun fetchWorks(): PublishedResult<Works>?
    suspend fun fetchProfile(): PublishedResult<PublishedProfile>?
    suspend fun fetchReadme(): PublishedResult<Readme>?
    suspend fun fetchTerminalCommands(): PublishedResult<TerminalTextCommands>?
}

/** 公開コンテンツ未接続時(env 未設定・テスト既定)は常にフォールバック側へ倒す。 */
internal object NoPublishedContent : PublishedContentClient {
    override suspend fun fetchWorks(): PublishedResult<Works> = PublishedResult.Missing
    override suspend fun fetchProfile(): PublishedResult<PublishedProfile> = PublishedResult.Missing
    override suspend fun fetchReadme(): PublishedResult<Readme> = PublishedResult.Missing
    override suspend fun fetchTerminalCommands(): PublishedResult<TerminalTextCommands> = PublishedResult.Missing
}

internal fun interface PublishedBlobReader {
    /** Returns the object bytes, or null when the object does not exist. Throws on failure. */
    fun read(path: String): ByteArray?
}

internal class GcsPublishedContentClient(
    private val bucket: String,
    private val assetBaseUrl: String,
    private val readBlob: PublishedBlobReader = gcsBlobReader(bucket),
    private val readTimeoutMillis: Long = DEFAULT_READ_TIMEOUT_MILLIS,
) : PublishedContentClient {

    private val json = Json { ignoreUnknownKeys = true }
    private val logger = LoggerFactory.getLogger(GcsPublishedContentClient::class.java)

    override suspend fun fetchWorks(): PublishedResult<Works>? =
        readJson(WORKS_PATH).mapFound { body ->
            decodeOrNull<PublishedWorks>(body)?.toWorks(assetBaseUrl)
        }

    override suspend fun fetchProfile(): PublishedResult<PublishedProfile>? =
        readJson(PROFILE_PATH).mapFound { body ->
            decodeOrNull<PublishedProfile>(body)?.let { profile ->
                profile.copy(
                    avatarUrl = resolveAssetUrl(profile.avatarUrl, assetBaseUrl),
                )
            }
        }

    override suspend fun fetchReadme(): PublishedResult<Readme>? =
        readJson(README_PATH).mapFound { body -> decodeOrNull<PublishedReadme>(body)?.toReadme() }

    override suspend fun fetchTerminalCommands(): PublishedResult<TerminalTextCommands>? =
        readJson(TERMINAL_PATH).mapFound { body ->
            decodeOrNull<PublishedTerminalCommands>(body)?.toTerminalTextCommands()
        }

    private suspend fun readJson(path: String): PublishedResult<String>? = withContext(Dispatchers.IO) {
        try {
            val read = withTimeoutOrNull(readTimeoutMillis) {
                when (val bytes = runInterruptible { readBlob.read(path) }) {
                    null -> PublishedResult.Missing
                    else -> PublishedResult.Found(bytes)
                }
            }
            if (read == null) {
                logger.warn(
                    "timed out reading published content gs://{}/{} after {} ms",
                    bucket,
                    path,
                    readTimeoutMillis,
                )
            }
            read.mapFound { bytes ->
                if (bytes.size > MAX_PUBLISHED_OBJECT_BYTES) {
                    logger.warn(
                        "published content gs://{}/{} is {} bytes, over the {} byte limit",
                        bucket,
                        path,
                        bytes.size,
                        MAX_PUBLISHED_OBJECT_BYTES,
                    )
                    null
                } else {
                    bytes.decodeToString()
                }
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.warn("failed to read published content gs://{}/{}", bucket, path, e)
            null
        }
    }

    private inline fun <reified T> decodeOrNull(body: String): T? =
        try {
            json.decodeFromString<T>(body)
        } catch (e: Exception) {
            logger.warn("failed to decode published content as {}", T::class.simpleName, e)
            null
        }

    companion object {
        internal const val MAX_PUBLISHED_OBJECT_BYTES = 1 * 1024 * 1024
        private const val DEFAULT_READ_TIMEOUT_MILLIS = 10_000L
        private const val WORKS_PATH = "content/published/works.json"
        private const val PROFILE_PATH = "content/published/profile.json"
        private const val README_PATH = "content/published/readme.json"
        private const val TERMINAL_PATH = "content/published/terminal-commands.json"
    }
}

private inline fun <T : Any, R : Any> PublishedResult<T>?.mapFound(
    transform: (T) -> R?,
): PublishedResult<R>? = when (this) {
    is PublishedResult.Found -> transform(value)?.let { PublishedResult.Found(it) }
    PublishedResult.Missing -> PublishedResult.Missing
    null -> null
}

private const val GCS_TRANSPORT_TIMEOUT_MILLIS = 10_000

private fun gcsBlobReader(bucket: String): PublishedBlobReader {
    val storage = StorageOptions.getDefaultInstance().toBuilder()
        .setTransportOptions(
            HttpTransportOptions.newBuilder()
                .setConnectTimeout(GCS_TRANSPORT_TIMEOUT_MILLIS)
                .setReadTimeout(GCS_TRANSPORT_TIMEOUT_MILLIS)
                .build(),
        )
        .build()
        .service
    return PublishedBlobReader { path ->
        storage.get(BlobId.of(bucket, path))?.let { blob ->
            // ダウンロード前にメタデータで弾く。ダウンロード後の上限検査は readJson 側が持つ
            check(blob.size <= GcsPublishedContentClient.MAX_PUBLISHED_OBJECT_BYTES) {
                "published object gs://$bucket/$path is ${blob.size} bytes"
            }
            blob.getContent()
        }
    }
}
