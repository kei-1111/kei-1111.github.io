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

private const val CONTRIBUTIONS_RESPONSE = """
{"data":{"user":{"contributionsCollection":{"contributionCalendar":{
  "totalContributions":5,
  "weeks":[
    {"contributionDays":[
      {"date":"2025-07-14","contributionCount":1,"contributionLevel":"FIRST_QUARTILE"}
    ]},
    {"contributionDays":[
      {"date":"2025-07-15","contributionCount":4,"contributionLevel":"FOURTH_QUARTILE"}
    ]}
  ]
}}}}}
"""

class ContributionsServiceTest {

    @Test
    fun mapsTheCalendarFromTheGitHubResponse() = runTest {
        val service = ContributionsService(
            GitHubClient(
                token = "t",
                engine = MockEngine {
                    respond(
                        content = CONTRIBUTIONS_RESPONSE,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ),
        )

        val calendar = assertNotNull(service.getContributions())

        assertEquals(5, calendar.totalLastYear)
        assertEquals(2, calendar.days.size)
    }

    @Test
    fun returnsNullWhenTheGitHubFetchFails() = runTest {
        val service = ContributionsService(
            GitHubClient(token = "t", engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }),
        )

        assertNull(service.getContributions())
    }

    @Test
    fun servesTheSecondCallFromTheCacheWithoutRefetching() = runTest {
        var requestCount = 0
        val service = ContributionsService(
            GitHubClient(
                token = "t",
                engine = MockEngine {
                    requestCount++
                    respond(
                        content = CONTRIBUTIONS_RESPONSE,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ),
        )

        val first = service.getContributions()
        val second = service.getContributions()

        assertEquals(first, second)
        assertEquals(1, requestCount)
    }
}
