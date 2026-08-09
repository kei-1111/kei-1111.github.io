package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileSourceCodeTest {

    @Test
    fun formatsCollectionConstructorsWithOneArgumentPerLine() {
        val code = profileCode(profileFixture, KeiLanguage.En)

        assertTrue(
            code.contains(
                """
                    |                PinnedRepo(
                    |                    name = "kotlin-repo",
                    |                    description = "Kotlin repository",
                    |                    url = "https://github.com/kei-1111/kotlin-repo",
                    |                    language = RepoLanguage("Kotlin"),
                    |                ),
                """.trimMargin(),
            ),
        )
        assertTrue(
            code.contains(
                """
                    |                PinnedRepo(
                    |                    name = "starred-repo",
                    |                    description = "Starred repository",
                    |                    url = "https://github.com/kei-1111/starred-repo",
                    |                    stars = 7,
                    |                ),
                """.trimMargin(),
            ),
        )
        assertTrue(
            code.contains(
                """
                    |                LanguageShare(
                    |                    language = RepoLanguage("Kotlin"),
                    |                    share = 0.75f,
                    |                ),
                """.trimMargin(),
            ),
        )
        assertTrue(
            code.contains(
                """
                    |                LinkService(
                    |                    name = "GitHub",
                    |                    url = "https://github.com/kei-1111",
                    |                ),
                """.trimMargin(),
            ),
        )
        val fieldAssignment = Regex("""\b[A-Za-z_]\w*\s*=""")
        assertTrue(code.lineSequence().all { fieldAssignment.findAll(it).count() <= 1 })
    }

    @Test
    fun roundTripsGeneratedProfileCode() {
        val code = profileCode(profileFixture, KeiLanguage.En)

        val parsed = parseProfileCode(code)

        assertEquals(profileFixture, parsed)
    }

    @Test
    fun formatsBothPinnedRepoMetadataFieldsInFixedOrder() {
        val profile = profileFixture.copy(
            pinnedRepos = persistentListOf(
                PinnedRepo(
                    name = "both-repo",
                    description = LocalizedText(ja = "Both repository", en = "Both repository"),
                    url = "https://github.com/kei-1111/both-repo",
                    language = RepoLanguage("Kotlin"),
                    stars = 7,
                ),
            ),
        )

        val code = profileCode(profile, KeiLanguage.En)

        assertTrue(
            code.contains(
                """
                    |                PinnedRepo(
                    |                    name = "both-repo",
                    |                    description = "Both repository",
                    |                    url = "https://github.com/kei-1111/both-repo",
                    |                    language = RepoLanguage("Kotlin"),
                    |                    stars = 7,
                    |                ),
                """.trimMargin(),
            ),
        )
    }

    @Test
    fun roundTripsPinnedRepoWithBothMetadataFields() {
        val profile = profileFixture.copy(
            pinnedRepos = persistentListOf(
                PinnedRepo(
                    name = "both-repo",
                    description = LocalizedText(ja = "Both repository", en = "Both repository"),
                    url = "https://github.com/kei-1111/both-repo",
                    language = RepoLanguage("Kotlin"),
                    stars = 7,
                ),
            ),
        )

        val code = profileCode(profile, KeiLanguage.En)

        assertEquals(profile, parseProfileCode(code))
    }

    @Test
    fun roundTripsPinnedRepoWithoutMetadataFields() {
        val profile = profileFixture.copy(
            pinnedRepos = persistentListOf(
                PinnedRepo(
                    name = "plain-repo",
                    description = LocalizedText(ja = "Plain repository", en = "Plain repository"),
                    url = "https://github.com/kei-1111/plain-repo",
                ),
            ),
        )

        val code = profileCode(profile, KeiLanguage.En)

        assertEquals(profile, parseProfileCode(code))
    }

    @Test
    fun rejectsPinnedRepoStarsBeforeLanguage() {
        val profile = profileFixture.copy(
            pinnedRepos = persistentListOf(
                PinnedRepo(
                    name = "both-repo",
                    description = LocalizedText(ja = "Both repository", en = "Both repository"),
                    url = "https://github.com/kei-1111/both-repo",
                    language = RepoLanguage("Kotlin"),
                    stars = 7,
                ),
            ),
        )
        val code = profileCode(profile, KeiLanguage.En).replace(
            """language = RepoLanguage("Kotlin"),
                    stars = 7,""",
            """stars = 7,
                    language = RepoLanguage("Kotlin"),""",
        )

        assertNull(parseProfileCode(code))
    }

    @Test
    fun roundTripsANonLegacyLanguageName() {
        val typeScript = RepoLanguage("TypeScript")
        val profile = profileFixture.copy(
            pinnedRepos = persistentListOf(profileFixture.pinnedRepos.first().copy(language = typeScript)),
            languages = persistentListOf(LanguageShare(language = typeScript, share = 1f)),
        )

        val code = profileCode(profile, KeiLanguage.En)

        assertEquals(profile, parseProfileCode(code))
    }

    @Test
    fun omitsLanguageColorsFromTheSourceProjection() {
        val profile = profileFixture.copy(
            languages = persistentListOf(
                LanguageShare(
                    language = RepoLanguage("TypeScript"),
                    share = 1f,
                    color = "#3178C6",
                ),
            ),
        )

        val code = profileCode(profile, KeiLanguage.En)
        val parsed = assertNotNull(parseProfileCode(code))

        assertTrue("color =" !in code)
        assertNull(parsed.languages.single().color)
    }

    @Test
    fun rejectsTheOldEnumEntryLanguageShape() {
        val code = profileCode(profileFixture, KeiLanguage.En)
            .replace("RepoLanguage(\"Kotlin\")", "RepoLanguage.Kotlin")

        assertNull(parseProfileCode(code))
    }
}

private val profileFixture = GitHubProfile(
    name = LocalizedText(ja = "Kei", en = "Kei"),
    handle = "kei-1111",
    location = "Japan",
    role = "Kotlin developer",
    followers = 12,
    following = 34,
    repos = 56,
    totalStars = 78,
    pinnedRepos = persistentListOf(
        PinnedRepo(
            name = "kotlin-repo",
            description = LocalizedText(ja = "Kotlin repository", en = "Kotlin repository"),
            url = "https://github.com/kei-1111/kotlin-repo",
            language = RepoLanguage("Kotlin"),
        ),
        PinnedRepo(
            name = "starred-repo",
            description = LocalizedText(ja = "Starred repository", en = "Starred repository"),
            url = "https://github.com/kei-1111/starred-repo",
            stars = 7,
        ),
    ),
    languages = persistentListOf(
        LanguageShare(language = RepoLanguage("Kotlin"), share = 0.75f),
    ),
    links = persistentListOf(
        LinkService(
            type = LinkServiceType.GitHub,
            name = "GitHub",
            url = "https://github.com/kei-1111",
        ),
    ),
)
