package io.github.kei_1111.app.core.api.terminal

import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
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

class TerminalCommandsApiImplTest {

    @Test
    fun returnsDecodedBodyOnSuccess() = runTest {
        val expected = terminalCommands()
        var requestedUrl: String? = null
        val api = TerminalCommandsApiImpl(
            client = testClient { request ->
                requestedUrl = request.url.toString()
                respond(
                    content = Json.encodeToString(expected),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchTerminalCommands()

        assertEquals(expected, actual)
        assertEquals("$API_BASE_URL/api/terminal-commands", requestedUrl)
    }

    @Test
    fun foldsHttpErrorToNull() = runTest {
        val api = TerminalCommandsApiImpl(
            client = testClient {
                respond(
                    content = "{}",
                    status = HttpStatusCode.ServiceUnavailable,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchTerminalCommands()

        assertNull(actual)
    }

    @Test
    fun foldsNetworkFailureToNull() = runTest {
        val api = TerminalCommandsApiImpl(
            client = testClient { error("network failure") },
        )

        val actual = api.fetchTerminalCommands()

        assertNull(actual)
    }
}

private fun testClient(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

private fun terminalCommands() = TerminalTextCommands(
    items = persistentListOf(
        TerminalTextCommand(
            keyword = "neofetch",
            description = "show portfolio system info",
            lines = persistentListOf("test output"),
        ),
    ),
)
