package io.github.kei_1111.server.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Serializable
data class GraphQlRequest(val query: String, val variables: Map<String, String>)

@Serializable
data class GraphQlResponse<T>(val data: T? = null, val errors: List<GraphQlError> = emptyList())

@Serializable
data class GraphQlError(val message: String = "")

/**
 * GitHub GraphQL API の薄い汎用クライアント。失敗(HTTP 非 200・errors・例外)はすべて null に畳み、
 * 呼び出し側の静的フォールバックに委ねる。token が null の場合は API を呼ばず常に null を返す。
 */
class GitHubClient private constructor(
    token: String?,
    private val engine: HttpClientEngine,
    private val ownsEngine: Boolean,
) : AutoCloseable {

    /** production 用。CIO エンジンを自前で生成し、close() で解放する。 */
    constructor(token: String?) : this(token, CIO.create(), ownsEngine = true)

    /** テスト用。呼び出し側が所有するエンジン(MockEngine 等)を注入する。close() では解放しない。 */
    constructor(token: String?, engine: HttpClientEngine) : this(token, engine, ownsEngine = false)

    @PublishedApi
    internal val token: String? = token

    @PublishedApi
    internal val logger: Logger = LoggerFactory.getLogger(GitHubClient::class.java)

    @PublishedApi
    internal val httpClient: HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        }
    }

    suspend inline fun <reified T> execute(query: String, variables: Map<String, String>): T? {
        val bearerToken = token ?: return null
        return try {
            val response = httpClient.post(GRAPHQL_ENDPOINT) {
                header(HttpHeaders.Authorization, "Bearer $bearerToken")
                contentType(ContentType.Application.Json)
                setBody(GraphQlRequest(query, variables))
            }
            if (response.status != HttpStatusCode.OK) {
                logger.warn("GitHub GraphQL API returned {}", response.status)
                null
            } else {
                val body = response.body<GraphQlResponse<T>>()
                if (body.errors.isNotEmpty()) {
                    logger.warn("GitHub GraphQL API errors: {}", body.errors.map { it.message })
                    null
                } else {
                    body.data
                }
            }
        } catch (e: Exception) {
            // コルーチンがキャンセル済みなら再スローして構造化並行性を保つ(API 障害のみ null に畳む)。
            currentCoroutineContext().ensureActive()
            logger.warn("GitHub GraphQL API call failed", e)
            null
        }
    }

    override fun close() {
        // HttpClient(engine) はエンジンを所有しないため、自前生成したエンジンだけ明示的に閉じる
        // (注入されたエンジンは呼び出し側が所有)。いずれも close は冪等で二重呼び出しは無害。
        httpClient.close()
        if (ownsEngine) engine.close()
    }

    companion object {
        @PublishedApi
        internal const val GRAPHQL_ENDPOINT = "https://api.github.com/graphql"

        private const val REQUEST_TIMEOUT_MILLIS = 10_000L
    }
}
