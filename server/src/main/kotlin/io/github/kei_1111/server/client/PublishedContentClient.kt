package io.github.kei_1111.server.client

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import io.github.kei_1111.shared.model.Works
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * 管理コンソールが公開したコンテンツの取得。失敗・未公開は null に畳む
 * (フォールバック方針は service 層が持つ)。
 */
interface PublishedContentClient {
    suspend fun fetchWorks(): Works?
    suspend fun fetchProfile(): PublishedProfile?
}

/** 公開コンテンツ未接続時(env 未設定・テスト既定)は常にフォールバック側へ倒す。 */
object NoPublishedContent : PublishedContentClient {
    override suspend fun fetchWorks(): Works? = null
    override suspend fun fetchProfile(): PublishedProfile? = null
}

/**
 * GCS の `content/published/` を Cloud Run のサービスアカウント(ADC)で読む実装。
 * クライアントはブロッキングのため IO へ逃がし、あらゆる失敗を null に畳む。
 */
class GcsPublishedContentClient(
    private val bucket: String,
    private val assetBaseUrl: String,
    private val storage: Storage = StorageOptions.getDefaultInstance().service,
) : PublishedContentClient {

    private val json = Json { ignoreUnknownKeys = true }
    private val logger = LoggerFactory.getLogger(GcsPublishedContentClient::class.java)

    override suspend fun fetchWorks(): Works? =
        readJson(WORKS_PATH)?.let { body ->
            decodeOrNull<PublishedWorks>(body)?.toWorks(assetBaseUrl)
        }

    override suspend fun fetchProfile(): PublishedProfile? =
        readJson(PROFILE_PATH)?.let { body -> decodeOrNull<PublishedProfile>(body) }

    private suspend fun readJson(path: String): String? = withContext(Dispatchers.IO) {
        try {
            storage.get(BlobId.of(bucket, path))?.getContent()?.decodeToString()
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
        private const val WORKS_PATH = "content/published/works.json"
        private const val PROFILE_PATH = "content/published/profile.json"
    }
}
