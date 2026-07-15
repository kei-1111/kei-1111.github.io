package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme
import io.github.kei_1111.app.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.app.feature.profile.theme.highlightKotlin
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.PinnedRepo

/** 各ページに対応するコード（行ごとの AnnotatedString）を返す。 */
internal fun codeLinesFor(
    page: EditorPage,
    profile: GitHubProfile,
    japaneseFontFamily: FontFamily,
    colors: KeiColorScheme,
): List<AnnotatedString> = when (page) {
    EditorPage.Profile -> highlightKotlin(profileCode(profile), japaneseFontFamily, colors)
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
