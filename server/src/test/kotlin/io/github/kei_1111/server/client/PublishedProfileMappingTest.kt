package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublishedProfileMappingTest {

    @Test
    fun keepsOnlyConvertibleSocialLinks() {
        val published = PublishedProfile(
            socialLinks = listOf(
                PublishedSocialLink(service = "GitHub", url = "https://github.com/kei-1111"),
                PublishedSocialLink(service = "X", url = "https://x.com/kei"),
                PublishedSocialLink(service = "unknown-service", url = "https://example.com"),
            ),
        )

        assertEquals(listOf(LinkServiceType.GitHub, LinkServiceType.X), published.links().map { it.type })
    }

    @Test
    fun clearingEverySocialLinkLeavesNoLinks() {
        assertEquals(emptyList(), PublishedProfile(displayName = "けい").links())
    }

    @Test
    fun legacyXUrlBecomesAnXLinkWhenSocialLinksLackOne() {
        val published = PublishedProfile(
            xUrl = "https://x.com/legacy",
            socialLinks = listOf(PublishedSocialLink(service = "GitHub", url = "https://github.com/kei-1111")),
        )

        val links = published.links()

        assertEquals(listOf(LinkServiceType.GitHub, LinkServiceType.X), links.map { it.type })
        assertEquals("https://x.com/legacy", links.last().url)
    }

    @Test
    fun emitsCanonicalLinkNamesFromTheServiceType() {
        val published = PublishedProfile(
            socialLinks = listOf(
                PublishedSocialLink(service = "twitter", url = "https://x.com/kei"),
                PublishedSocialLink(service = " note ", url = "https://note.com/kei"),
            ),
        )

        assertEquals(listOf("X", "note"), published.links().map { it.name })
    }

    @Test
    fun dropsNonHttpSocialLinkAndLegacyXUrls() {
        val published = PublishedProfile(
            xUrl = "javascript:alert(2)",
            socialLinks = listOf(
                PublishedSocialLink(service = "GitHub", url = "javascript:alert(1)"),
                PublishedSocialLink(service = "Qiita", url = "https://qiita.com/kei-1111"),
            ),
        )

        assertEquals(listOf(LinkServiceType.Qiita), published.links().map { it.type })
    }

    @Test
    fun keepsOnlyTheFirstSocialLinkPerServiceType() {
        val published = PublishedProfile(
            socialLinks = listOf(
                PublishedSocialLink(service = "X", url = "https://x.com/first"),
                PublishedSocialLink(service = "twitter", url = "https://twitter.com/second"),
            ),
        )

        assertEquals("https://x.com/first", published.links().single().url)
    }

    @Test
    fun exposesOnlyThePinnedDescriptionsThatWereFilledIn() {
        val published = PublishedProfile(
            pinnedRepos = listOf(
                PublishedPinnedRepo(name = "withmo", descriptionJa = "説明", descriptionEn = "Description"),
                PublishedPinnedRepo(name = "portfolio"),
            ),
        )

        assertEquals(
            mapOf("withmo" to LocalizedText(ja = "説明", en = "Description")),
            published.descriptionOverrides(),
        )
    }

    @Test
    fun fillsAMissingDescriptionLanguageFromTheOther() {
        val published = PublishedProfile(
            pinnedRepos = listOf(PublishedPinnedRepo(name = "withmo", descriptionEn = "Description")),
        )

        assertEquals(
            mapOf("withmo" to LocalizedText(ja = "Description", en = "Description")),
            published.descriptionOverrides(),
        )
    }

    @Test
    fun reportsWhichPinnedRepositoriesAreHidden() {
        val published = PublishedProfile(
            pinnedRepos = listOf(
                PublishedPinnedRepo(name = "withmo", visible = false),
                PublishedPinnedRepo(name = "portfolio", visible = true),
            ),
        )

        assertTrue(published.hidesPinnedRepo("withmo"))
        assertFalse(published.hidesPinnedRepo("portfolio"))
        assertFalse(published.hidesPinnedRepo("never-configured"))
    }
}
