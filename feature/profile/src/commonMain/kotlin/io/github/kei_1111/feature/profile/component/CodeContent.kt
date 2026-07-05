@file:Suppress("UnusedPrivateMember")

package io.github.kei_1111.feature.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.theme.AppTheme
import io.github.kei_1111.core.designsystem.theme.IdeColors
import io.github.kei_1111.feature.profile.EditorPage
import io.github.kei_1111.feature.profile.GitHubProfileContent
import io.github.kei_1111.feature.profile.LanguageShare
import io.github.kei_1111.feature.profile.LinkService
import io.github.kei_1111.feature.profile.PinnedRepo
import io.github.kei_1111.feature.profile.highlightKotlin

/** 各ページに対応するコード（行ごとの AnnotatedString）を返す。 */
internal fun codeLinesFor(page: EditorPage, japaneseFontFamily: FontFamily): List<AnnotatedString> = when (page) {
    EditorPage.Profile -> highlightKotlin(profileCode, japaneseFontFamily)
}

private val profileData = GitHubProfileContent.default

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

private val profileCode = """
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
    AppTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .width(560.dp)
                .background(IdeColors.Island),
        ) {
            CodeLines(page = EditorPage.Profile)
        }
    }
}
