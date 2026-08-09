package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val ISSUES_RESPONSE = """
{"data":{"repository":{"issues":{
  "totalCount":2,
  "nodes":[
    {"number":106,"title":"[Feature]: Add a TODO tool window","url":"https://github.com/kei-1111/kei-1111.github.io/issues/106"},
    {"number":24,"title":"作品ページの追加","url":"https://github.com/kei-1111/kei-1111.github.io/issues/24"}
  ]
}}}}
"""

class IssuesServiceTest {

    @Test
    fun mapsOpenIssuesFromTheGitHubResponse() = runTest {
        val service = IssuesService(
            GitHubClient(
                token = "t",
                engine = MockEngine {
                    respond(
                        content = ISSUES_RESPONSE,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ),
        )

        val issues = assertNotNull(service.getIssues())

        assertEquals(2, issues.totalCount)
        assertEquals(listOf(106, 24), issues.issues.map { it.number })
    }

    @Test
    fun returnsNullWhenTheGitHubFetchFails() = runTest {
        val service = IssuesService(
            GitHubClient(token = "t", engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }),
        )

        assertNull(service.getIssues())
    }

    @Test
    fun servesTheSecondCallFromTheCacheWithoutRefetching() = runTest {
        var requestCount = 0
        val service = IssuesService(
            GitHubClient(
                token = "t",
                engine = MockEngine {
                    requestCount++
                    respond(
                        content = ISSUES_RESPONSE,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ),
        )

        val first = service.getIssues()
        val second = service.getIssues()

        assertEquals(first, second)
        assertEquals(1, requestCount)
    }
}
