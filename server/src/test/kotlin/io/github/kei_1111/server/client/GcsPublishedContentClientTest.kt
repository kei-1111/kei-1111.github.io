package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.Works
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GcsPublishedContentClientTest {

    @Test
    fun fetchWorksDecodesAndMapsPublishedContent() = runTest {
        val client = clientWithBody(
            """
            {
              "works": [
                {
                  "id": "portfolio",
                  "name": "Portfolio",
                  "type": "Web App"
                }
              ]
            }
            """.trimIndent(),
        )

        val result = assertIs<PublishedResult.Found<*>>(client.fetchWorks())
        val works = assertIs<Works>(result.value)

        assertEquals("Web App", works.items.single().kind)
    }

    @Test
    fun fetchWorksReturnsMissingWhenTheObjectDoesNotExist() = runTest {
        val client = GcsPublishedContentClient(
            bucket = "published-content",
            assetBaseUrl = "https://admin.example",
            readBlob = PublishedBlobReader { null },
        )

        assertEquals(PublishedResult.Missing, client.fetchWorks())
    }

    @Test
    fun fetchWorksReturnsNullWhenReadingFails() = runTest {
        val client = GcsPublishedContentClient(
            bucket = "published-content",
            assetBaseUrl = "https://admin.example",
            readBlob = PublishedBlobReader { throw IllegalStateException("boom") },
        )

        assertNull(client.fetchWorks())
    }

    @Test
    fun fetchWorksReturnsNullWhenPublishedJsonIsMalformed() = runTest {
        val client = clientWithBody("not-json")

        assertNull(client.fetchWorks())
    }

    @Test
    fun fetchProfileResolvesUploadedAvatarPathsAndKeepsAbsoluteUrls() = runTest {
        val uploaded = clientWithBody("""{"avatarUrl":"images/profile/x.png"}""").fetchProfile()
        val absolute = clientWithBody("""{"avatarUrl":"https://cdn.example/x.png"}""").fetchProfile()

        assertEquals(
            "https://admin.example/images/profile/x.png",
            assertIs<PublishedProfile>(assertIs<PublishedResult.Found<*>>(uploaded).value).avatarUrl,
        )
        assertEquals(
            "https://cdn.example/x.png",
            assertIs<PublishedProfile>(assertIs<PublishedResult.Found<*>>(absolute).value).avatarUrl,
        )
    }

    private fun clientWithBody(body: String) = GcsPublishedContentClient(
        bucket = "published-content",
        assetBaseUrl = "https://admin.example",
        readBlob = PublishedBlobReader { body.encodeToByteArray() },
    )
}
