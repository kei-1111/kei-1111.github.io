package io.github.kei_1111.app.core.api.readme

import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.MarkdownInline
import io.github.kei_1111.shared.model.Readme
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

class ReadmeApiImplTest {

    @Test
    fun returnsDecodedBodyOnSuccess() = runTest {
        val expected = readme()
        var requestedUrl: String? = null
        val api = ReadmeApiImpl(
            client = testClient { request ->
                requestedUrl = request.url.toString()
                respond(
                    content = Json.encodeToString(expected),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchReadme()

        assertEquals(expected, actual)
        assertEquals("$API_BASE_URL/api/readme", requestedUrl)
    }

    @Test
    fun foldsHttpErrorToNull() = runTest {
        val api = ReadmeApiImpl(
            client = testClient {
                respond(
                    content = "{}",
                    status = HttpStatusCode.ServiceUnavailable,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchReadme()

        assertNull(actual)
    }

    @Test
    fun foldsNetworkFailureToNull() = runTest {
        val api = ReadmeApiImpl(
            client = testClient { error("network failure") },
        )

        val actual = api.fetchReadme()

        assertNull(actual)
    }
}

private fun testClient(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

private fun readme() = Readme(
    ja = persistentListOf(
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(MarkdownInline.PlainText("ja")),
        ),
    ),
    en = persistentListOf(
        MarkdownBlock.Paragraph(
            inlines = persistentListOf(MarkdownInline.PlainText("en")),
        ),
    ),
)
