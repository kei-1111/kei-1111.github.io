package io.github.kei_1111.server.routing

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.client.PublishedProfile
import io.github.kei_1111.server.client.PublishedResult
import io.github.kei_1111.server.configureApplication
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.Works
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

private val publishedWorks = Works(
    items = persistentListOf(
        Work(
            id = "published-work",
            name = "Published Work",
            kind = "Web App",
            period = "2026–",
            description = LocalizedText(ja = "公開", en = "published"),
        ),
    ),
)

private class FakePublishedContentClient : PublishedContentClient {
    override suspend fun fetchWorks(): PublishedResult<Works> = PublishedResult.Found(publishedWorks)
    override suspend fun fetchProfile(): PublishedResult<PublishedProfile> = PublishedResult.Missing
    override suspend fun fetchReadme(): PublishedResult<Readme> = PublishedResult.Missing
    override suspend fun fetchTerminalCommands(): PublishedResult<TerminalTextCommands> = PublishedResult.Missing
}

class PublishedContentRoutesTest {

    @Test
    fun servesPublishedWorksThroughTheApiRoute() = testApplication {
        application {
            configureApplication(
                gitHubClient = GitHubClient(
                    token = "t",
                    engine = MockEngine { respondError(HttpStatusCode.InternalServerError) },
                ),
                publishedContentClient = FakePublishedContentClient(),
            )
        }

        val response = client.get("/api/works")

        assertEquals(HttpStatusCode.OK, response.status)
        val works = Json.decodeFromString<Works>(response.bodyAsText())
        assertEquals(publishedWorks, works)
    }
}
