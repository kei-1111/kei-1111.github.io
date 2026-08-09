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
                    |                    language = RepoLanguage.Kotlin,
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
                    |                    language = RepoLanguage.Kotlin,
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
    fun overlayRestoresIconUrlFromBase() {
        val base = profileFixture.copy(iconUrl = "images/profile-icon.webp")

        val restored = overlayProfileAssets(profileFixture, base)

        assertEquals("images/profile-icon.webp", restored.iconUrl)
    }

    @Test
    fun overlayWithoutBaseLeavesIconUrlNull() {
        val withoutBase = overlayProfileAssets(profileFixture, null)

        assertNull(withoutBase.iconUrl)
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
            language = RepoLanguage.Kotlin,
        ),
        PinnedRepo(
            name = "starred-repo",
            description = LocalizedText(ja = "Starred repository", en = "Starred repository"),
            url = "https://github.com/kei-1111/starred-repo",
            stars = 7,
        ),
    ),
    languages = persistentListOf(
        LanguageShare(language = RepoLanguage.Kotlin, share = 0.75f),
    ),
    links = persistentListOf(
        LinkService(
            type = LinkServiceType.GitHub,
            name = "GitHub",
            url = "https://github.com/kei-1111",
        ),
    ),
)
