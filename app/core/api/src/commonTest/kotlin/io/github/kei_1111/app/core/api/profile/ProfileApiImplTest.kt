package io.github.kei_1111.app.core.api.profile

import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Profile
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileApiImplTest {

    @Test
    fun returnsDecodedBodyOnSuccess() = runTest {
        val expected = profile()
        var requestedUrl: String? = null
        val api = ProfileApiImpl(
            client = testClient { request ->
                requestedUrl = request.url.toString()
                respond(
                    content = Json.encodeToString(expected),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchProfile()

        assertEquals(expected, actual)
        assertEquals("$API_BASE_URL/api/profile", requestedUrl)
    }

    @Test
    fun foldsHttpErrorToNull() = runTest {
        val api = ProfileApiImpl(
            client = testClient {
                respond(
                    content = "{}",
                    status = HttpStatusCode.ServiceUnavailable,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchProfile()

        assertNull(actual)
    }

    @Test
    fun foldsNetworkFailureToNull() = runTest {
        val api = ProfileApiImpl(
            client = testClient { error("network failure") },
        )

        val actual = api.fetchProfile()

        assertNull(actual)
    }

    @Test
    fun foldsMalformedBodyToNull() = runTest {
        val api = ProfileApiImpl(
            client = testClient {
                respond(
                    content = "not json",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )

        val actual = api.fetchProfile()

        assertNull(actual)
    }

    @Test
    fun propagatesCancellationInsteadOfFoldingToNull() = runTest {
        lateinit var job: Job
        var resultRecorded = false
        val api = ProfileApiImpl(
            client = testClient {
                job.cancel()
                error("network failure")
            },
        )

        job = launch(StandardTestDispatcher(testScheduler)) {
            api.fetchProfile()
            resultRecorded = true
        }
        runCurrent()
        job.join()

        assertTrue(job.isCancelled)
        assertFalse(resultRecorded)
    }
}

private fun testClient(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

private fun profile() = Profile(
    name = LocalizedText(ja = "テスト", en = "Test"),
    handle = "kei-1111",
    location = "Tokyo",
    role = "Student",
    followers = 0,
    following = 0,
    repos = 0,
    totalStars = 0,
    pinnedRepos = persistentListOf(),
    languages = persistentListOf(),
    links = persistentListOf(),
)
