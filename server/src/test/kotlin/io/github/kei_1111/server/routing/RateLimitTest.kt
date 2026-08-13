package io.github.kei_1111.server.routing

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.publishedContentClient
import io.github.kei_1111.server.configureApplication
import io.github.kei_1111.server.plugins.API_RATE_LIMIT_PER_MINUTE
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOKEN = "test-token"

// プロフィールは GitHub 取得失敗時も静的フォールバックで 200 を返すため、レートリミットの観測に使う。
private fun failingEngine() = MockEngine { respondError(HttpStatusCode.InternalServerError) }

class RateLimitTest {

    @Test
    fun apiReturnsTooManyRequestsOnceTheIpLimitIsExceeded() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

        repeat(API_RATE_LIMIT_PER_MINUTE) {
            assertEquals(HttpStatusCode.OK, client.get("/api/profile").status)
        }

        assertEquals(HttpStatusCode.TooManyRequests, client.get("/api/profile").status)
    }

    @Test
    fun distinctClientIpsHaveIndependentBudgets() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

        repeat(API_RATE_LIMIT_PER_MINUTE) {
            client.get("/api/profile") { header(HttpHeaders.XForwardedFor, "203.0.113.1") }
        }

        val exhausted = client.get("/api/profile") { header(HttpHeaders.XForwardedFor, "203.0.113.1") }
        val fresh = client.get("/api/profile") { header(HttpHeaders.XForwardedFor, "198.51.100.7") }

        assertEquals(HttpStatusCode.TooManyRequests, exhausted.status)
        assertEquals(HttpStatusCode.OK, fresh.status)
    }

    @Test
    fun spoofedLeadingForwardedEntriesShareTheRealClientIpBudget() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

        // 先頭エントリはクライアントが自由に名乗れる。末尾(Cloud Run のフロントエンドが付加する実 IP)が同じなら同一予算。
        repeat(API_RATE_LIMIT_PER_MINUTE) { index ->
            client.get("/api/profile") { header(HttpHeaders.XForwardedFor, "10.0.0.$index, 203.0.113.1") }
        }

        val response = client.get("/api/profile") {
            header(HttpHeaders.XForwardedFor, "10.9.9.9, 203.0.113.1")
        }

        assertEquals(HttpStatusCode.TooManyRequests, response.status)
    }

    @Test
    fun healthStaysReachableAfterTheApiBudgetIsExhausted() = testApplication {
        application { configureApplication(GitHubClient(TOKEN, failingEngine()), publishedContentClient()) }

        repeat(API_RATE_LIMIT_PER_MINUTE) {
            client.get("/api/profile")
        }
        assertEquals(HttpStatusCode.TooManyRequests, client.get("/api/profile").status)

        assertEquals(HttpStatusCode.OK, client.get("/health").status)
    }
}
