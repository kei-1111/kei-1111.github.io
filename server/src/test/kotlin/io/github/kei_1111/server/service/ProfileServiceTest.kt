package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.LanguageBytes
import io.github.kei_1111.server.client.PinnedRepoSource
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileServiceTest {

    @Test
    fun dropsLanguagesBelowOnePercent() {
        val sizes = listOf(
            LanguageBytes(name = "Kotlin", color = "#A97BFF", size = 981),
            LanguageBytes(name = "TypeScript", color = "#3178C6", size = 10),
            LanguageBytes(name = "Rust", color = "#DEA584", size = 9),
        )

        val shares = languageSharesFrom(sizes)

        assertEquals(listOf("Kotlin", "TypeScript"), shares.map { it.language.name })
    }

    @Test
    fun sortsBySizeDescendingAndKeepsAtMostFiveLanguages() {
        val sizes = listOf(
            LanguageBytes(name = "Sixth", color = null, size = 10),
            LanguageBytes(name = "Second", color = null, size = 50),
            LanguageBytes(name = "Fourth", color = null, size = 30),
            LanguageBytes(name = "First", color = null, size = 60),
            LanguageBytes(name = "Fifth", color = null, size = 20),
            LanguageBytes(name = "Third", color = null, size = 40),
        )

        val shares = languageSharesFrom(sizes)

        assertEquals(listOf("First", "Second", "Third", "Fourth", "Fifth"), shares.map { it.language.name })
    }

    @Test
    fun roundsSharesToTwoDecimalPlaces() {
        val sizes = listOf(
            LanguageBytes(name = "Kotlin", color = null, size = 2),
            LanguageBytes(name = "TypeScript", color = null, size = 1),
        )

        val shares = languageSharesFrom(sizes)

        assertEquals(listOf(0.67f, 0.33f), shares.map { it.share })
    }

    @Test
    fun passesLanguageColorThrough() {
        val sizes = listOf(LanguageBytes(name = "Rust", color = "#DEA584", size = 100))

        val shares = languageSharesFrom(sizes)

        assertEquals("#DEA584", shares.single().color)
    }

    @Test
    fun returnsEmptyForEmptyInput() {
        assertEquals(persistentListOf(), languageSharesFrom(emptyList()))
    }

    @Test
    fun returnsEmptyWhenAllSizesAreZero() {
        val sizes = listOf(
            LanguageBytes(name = "Kotlin", color = "#A97BFF", size = 0),
            LanguageBytes(name = "Swift", color = "#F05138", size = 0),
        )

        assertEquals(persistentListOf(), languageSharesFrom(sizes))
    }

    @Test
    fun computesSharesAgainstAllLanguagesIncludingDroppedOnes() {
        val sizes = listOf(
            LanguageBytes(name = "Kotlin", color = null, size = 990),
            LanguageBytes(name = "Rust", color = null, size = 9),
            LanguageBytes(name = "Shell", color = null, size = 1),
        )

        val shares = languageSharesFrom(sizes)

        assertEquals(
            persistentListOf(LanguageShare(language = RepoLanguage("Kotlin"), share = 0.99f)),
            shares,
        )
    }

    @Test
    fun usesRegisteredPinnedRepoDescriptionOverride() {
        val override = LocalizedText(ja = "上書き", en = "Override")
        val repos = listOf(
            PinnedRepoSource(
                name = "registered-repo",
                description = "GitHub description",
                url = "https://github.com/kei-1111/registered-repo",
                stars = 0,
                languageName = null,
            ),
        )

        val pinnedRepos = pinnedReposFrom(repos, mapOf("registered-repo" to override))

        assertEquals(override, pinnedRepos.single().description)
    }

    @Test
    fun usesGitHubDescriptionForUnregisteredPinnedRepo() {
        val repos = listOf(
            PinnedRepoSource(
                name = "unregistered-repo",
                description = "GitHub description",
                url = "https://github.com/kei-1111/unregistered-repo",
                stars = 0,
                languageName = null,
            ),
        )

        val pinnedRepos = pinnedReposFrom(repos, emptyMap())

        assertEquals(
            LocalizedText(ja = "GitHub description", en = "GitHub description"),
            pinnedRepos.single().description,
        )
    }

    @Test
    fun usesEmptyDescriptionForUnregisteredPinnedRepoWithoutGitHubDescription() {
        val repos = listOf(
            PinnedRepoSource(
                name = "unregistered-repo",
                description = null,
                url = "https://github.com/kei-1111/unregistered-repo",
                stars = 0,
                languageName = null,
            ),
        )

        val pinnedRepos = pinnedReposFrom(repos, emptyMap())

        assertEquals(LocalizedText(ja = "", en = ""), pinnedRepos.single().description)
    }

    @Test
    fun hidesZeroPinnedRepoStars() {
        val repos = listOf(
            PinnedRepoSource(
                name = "zero-stars",
                description = null,
                url = "https://github.com/kei-1111/zero-stars",
                stars = 0,
                languageName = null,
            ),
        )

        val pinnedRepos = pinnedReposFrom(repos, emptyMap())

        assertNull(pinnedRepos.single().stars)
    }

    @Test
    fun keepsPositivePinnedRepoStars() {
        val repos = listOf(
            PinnedRepoSource(
                name = "starred-repo",
                description = null,
                url = "https://github.com/kei-1111/starred-repo",
                stars = 2,
                languageName = null,
            ),
        )

        val pinnedRepos = pinnedReposFrom(repos, emptyMap())

        assertEquals(2, pinnedRepos.single().stars)
    }

    @Test
    fun omitsMissingPinnedRepoLanguage() {
        val repos = listOf(
            PinnedRepoSource(
                name = "no-language",
                description = null,
                url = "https://github.com/kei-1111/no-language",
                stars = 0,
                languageName = null,
            ),
        )

        val pinnedRepos = pinnedReposFrom(repos, emptyMap())

        assertNull(pinnedRepos.single().language)
    }

    @Test
    fun returnsEmptyPinnedReposForEmptyInput() {
        assertEquals(persistentListOf(), pinnedReposFrom(emptyList(), emptyMap()))
    }
}
