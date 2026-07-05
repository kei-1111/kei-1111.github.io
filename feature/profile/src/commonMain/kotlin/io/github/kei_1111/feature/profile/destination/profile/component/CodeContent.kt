@file:Suppress("UnusedPrivateMember")

package io.github.kei_1111.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.theme.KeiTheme
import io.github.kei_1111.core.model.GitHubProfile
import io.github.kei_1111.core.model.LanguageShare
import io.github.kei_1111.core.model.LinkService
import io.github.kei_1111.core.model.PinnedRepo
import io.github.kei_1111.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.feature.profile.highlightKotlin

/** 各ページに対応するコード（行ごとの AnnotatedString）を返す。 */
internal fun codeLinesFor(
    page: EditorPage,
    profile: GitHubProfile,
    japaneseFontFamily: FontFamily,
): List<AnnotatedString> = when (page) {
    EditorPage.Profile -> highlightKotlin(profileCode(profile), japaneseFontFamily)
}

private fun pinnedRepoCode(repo: PinnedRepo): String {
    val meta = repo.language?.let { "language = RepoLanguage.${it.name}" } ?: "stars = ${repo.stars}"
    return listOf(
        "|                PinnedRepo(",
        "|                    name = \"${repo.name}\", description = \"${repo.description}\",",
        "|                    url = \"${repo.url}\", $meta,",
        "|                ),",
    ).joinToString("\n")
}

private fun languageShareCode(entry: LanguageShare): String =
    "|                LanguageShare(language = RepoLanguage.${entry.language.name}, share = ${entry.share}f),"

private fun linkServiceCode(link: LinkService): String =
    "|                LinkService(name = \"${link.name}\", url = \"${link.url}\"),"

private fun profileCode(profileData: GitHubProfile): String = """
    |package io.github.kei_1111.ui.profile
    |
    |import ...
    |
    |@Composable
    |internal fun ProfileScreen(
    |    data: GitHubProfileData,
    |    modifier: Modifier = Modifier,
    |) { ... }
    |
    |@Preview
    |@Composable
    |private fun ProfileScreenPreview() {
    |    ProfileScreen(
    |        data = GitHubProfileData(
    |            name = "${profileData.name}",
    |            handle = "${profileData.handle}",
    |            location = "${profileData.location}",
    |            role = "${profileData.role}",
    |            followers = ${profileData.followers},
    |            following = ${profileData.following},
    |            repos = ${profileData.repos},
    |            totalStars = ${profileData.totalStars},
    |            pinnedRepos = listOf(
    ${profileData.pinnedRepos.joinToString("\n") { pinnedRepoCode(it) }}
    |            ),
    |            languages = listOf(
    ${profileData.languages.joinToString("\n") { languageShareCode(it) }}
    |            ),
    |            links = listOf(
    ${profileData.links.joinToString("\n") { linkServiceCode(it) }}
    |            ),
    |        ),
    |    )
    |}
""".trimMargin()

@Preview
@Composable
private fun ProfileCodeContentPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .width(560.dp)
                .background(KeiTheme.colors.island),
        ) {
            CodeLines(page = EditorPage.Profile, profile = PreviewGitHubProfile)
        }
    }
}
