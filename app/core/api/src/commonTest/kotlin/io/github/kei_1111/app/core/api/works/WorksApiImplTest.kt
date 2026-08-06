package io.github.kei_1111.app.core.api.works

import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
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

class WorksApiImplTest {

    @Test
    fun returnsDecodedBodyOnSuccess() = runTest {
        val expected = works()
        var requestedUrl: String? = null
        val api = WorksApiImpl(
            client = testClient { request ->
                requestedUrl = request.url.toString()
                respond(
                    content = Json.encodeToString(expected),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchWorks()

        assertEquals(expected, actual)
        assertEquals("$API_BASE_URL/api/works", requestedUrl)
    }

    @Test
    fun foldsHttpErrorToNull() = runTest {
        val api = WorksApiImpl(
            client = testClient {
                respond(
                    content = "{}",
                    status = HttpStatusCode.ServiceUnavailable,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchWorks()

        assertNull(actual)
    }

    @Test
    fun foldsNetworkFailureToNull() = runTest {
        val api = WorksApiImpl(
            client = testClient { error("network failure") },
        )

        val actual = api.fetchWorks()

        assertNull(actual)
    }
}

private fun testClient(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

private fun works() = listOf(
    Work(
        id = "work",
        name = "Work",
        kind = "Android App",
        period = "2024–",
        description = LocalizedText(ja = "説明", en = "Description"),
        tags = persistentListOf(WorkTag(name = "Kotlin", accent = true)),
        screenshots = persistentListOf("https://example.com/1.webp"),
    ),
)
