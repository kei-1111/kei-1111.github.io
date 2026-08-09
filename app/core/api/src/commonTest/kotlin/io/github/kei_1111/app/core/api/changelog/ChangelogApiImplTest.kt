package io.github.kei_1111.app.core.api.changelog

import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.GitHubChangelog
import io.github.kei_1111.shared.model.GitHubPullRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChangelogApiImplTest {

    @Test
    fun returnsDecodedBodyOnSuccess() = runTest {
        val expected = changelog()
        var requestedUrl: String? = null
        val api = ChangelogApiImpl(
            client = testClient { request ->
                requestedUrl = request.url.toString()
                respond(
                    content = Json.encodeToString(expected),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchChangelog()

        assertEquals(expected, actual)
        assertEquals("$API_BASE_URL/api/changelog", requestedUrl)
    }

    @Test
    fun foldsHttpErrorToNull() = runTest {
        val api = ChangelogApiImpl(
            client = testClient {
                respond(
                    content = "{}",
                    status = HttpStatusCode.ServiceUnavailable,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchChangelog()

        assertNull(actual)
    }

    @Test
    fun foldsNetworkFailureToNull() = runTest {
        val api = ChangelogApiImpl(
            client = testClient { error("network failure") },
        )

        val actual = api.fetchChangelog()

        assertNull(actual)
    }
}

private fun testClient(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

private fun changelog() = GitHubChangelog(
    totalCount = 1,
    pullRequests = persistentListOf(
        GitHubPullRequest(
            number = 205,
            title = "Add changelog data chain",
            url = "https://github.com/kei-1111/kei-1111.github.io/pull/205",
            headRefName = "feature/205",
            mergedAt = "2026-08-09T02:00:00Z",
            type = "Feature",
        ),
    ),
)
