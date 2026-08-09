package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.LanguageBytes
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
