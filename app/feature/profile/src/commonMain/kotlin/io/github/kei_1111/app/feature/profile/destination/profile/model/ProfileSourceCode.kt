package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.toImmutableList

private val stringFieldRegex = Regex("""(\w+) = "([^"]*)",""")
private val intFieldRegex = Regex("""(\w+) = (-?\d+),""")
private val pinnedRepoHeaderRegex = Regex("""name = "([^"]*)", description = "([^"]*)",""")
private val pinnedRepoLanguageRegex = Regex("""url = "([^"]*)", language = RepoLanguage\.(\w+),""")
private val pinnedRepoStarsRegex = Regex("""url = "([^"]*)", stars = (-?\d+),""")
private val languageShareRegex = Regex(
    """LanguageShare\(language = RepoLanguage\.(\w+), share = (\d+(?:\.\d+)?)f\),""",
)
private val linkServiceRegex = Regex("""LinkService\(name = "([^"]*)", url = "([^"]*)"\),""")

private val profileHead = listOf(
    "package io.github.kei_1111.ui.profile",
    "import ...",
    "@Composable",
    "internal fun ProfileScreen(",
    "data: GitHubProfileData,",
    "modifier: Modifier = Modifier,",
    ") { ... }",
    "@Preview",
    "@Composable",
    "private fun ProfileScreenPreview() {",
    "ProfileScreen(",
    "data = GitHubProfileData(",
)

private val profileTail = listOf(
    "),",
    ")",
    "}",
)

private data class ProfileScalars(
    val name: String,
    val handle: String,
    val location: String,
    val role: String,
    val followers: Int,
    val following: Int,
    val repos: Int,
    val totalStars: Int,
)

private class LineCursor(private val lines: List<String>) {
    private var index = 0

    fun peek(): String? = lines.getOrNull(index)

    fun take(): String? = lines.getOrNull(index++)

    fun expect(expected: String): Boolean = take() == expected

    fun isAtEnd(): Boolean = index == lines.size
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

internal fun profileCode(profileData: GitHubProfile): String = """
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

/**
 * 生成テンプレート(profileCode)の形からの逸脱はコンパイルエラー扱いで null。
 * ただし各行の前後空白と空行は無視する（行内の空白・トークン列の逸脱はエラー）。
 */
@Suppress("ReturnCount")
internal fun parseProfileCode(code: String): GitHubProfile? {
    val cursor = LineCursor(code.split('\n').map(String::trim).filter(String::isNotEmpty))
    if (profileHead.any { !cursor.expect(it) }) return null
    val scalars = parseScalars(cursor) ?: return null
    if (!cursor.expect("pinnedRepos = listOf(")) return null
    val pinnedRepos = parsePinnedRepos(cursor) ?: return null
    if (!cursor.expect("languages = listOf(")) return null
    val languages = parseLanguages(cursor) ?: return null
    if (!cursor.expect("links = listOf(")) return null
    val links = parseLinks(cursor) ?: return null
    if (profileTail.any { !cursor.expect(it) } || !cursor.isAtEnd()) return null
    return GitHubProfile(
        name = scalars.name,
        handle = scalars.handle,
        location = scalars.location,
        role = scalars.role,
        followers = scalars.followers,
        following = scalars.following,
        repos = scalars.repos,
        totalStars = scalars.totalStars,
        pinnedRepos = pinnedRepos.toImmutableList(),
        languages = languages.toImmutableList(),
        links = links.toImmutableList(),
    )
}

@Suppress("ReturnCount")
private fun parseScalars(cursor: LineCursor): ProfileScalars? {
    val name = cursor.stringField("name") ?: return null
    val handle = cursor.stringField("handle") ?: return null
    val location = cursor.stringField("location") ?: return null
    val role = cursor.stringField("role") ?: return null
    val followers = cursor.intField("followers") ?: return null
    val following = cursor.intField("following") ?: return null
    val repos = cursor.intField("repos") ?: return null
    val totalStars = cursor.intField("totalStars") ?: return null
    return ProfileScalars(name, handle, location, role, followers, following, repos, totalStars)
}

private fun LineCursor.stringField(key: String): String? {
    val match = take()?.let(stringFieldRegex::matchEntire) ?: return null
    return match.groupValues[2].takeIf { match.groupValues[1] == key }
}

@Suppress("ReturnCount")
private fun LineCursor.intField(key: String): Int? {
    val match = take()?.let(intFieldRegex::matchEntire) ?: return null
    if (match.groupValues[1] != key) return null
    return match.groupValues[2].toIntOrNull()
}

@Suppress("ReturnCount")
private fun parsePinnedRepos(cursor: LineCursor): List<PinnedRepo>? {
    val repos = mutableListOf<PinnedRepo>()
    while (cursor.peek() == "PinnedRepo(") {
        cursor.take()
        val header = cursor.take()?.let(pinnedRepoHeaderRegex::matchEntire) ?: return null
        val metadata = cursor.take() ?: return null
        val language = pinnedRepoLanguageRegex.matchEntire(metadata)
        val stars = pinnedRepoStarsRegex.matchEntire(metadata)
        if ((language == null) == (stars == null) || !cursor.expect("),")) return null
        val repo = if (language != null) {
            val repoLanguage = RepoLanguage.entries.find { it.name == language.groupValues[2] } ?: return null
            PinnedRepo(
                name = header.groupValues[1],
                description = header.groupValues[2],
                url = language.groupValues[1],
                language = repoLanguage,
            )
        } else {
            val starsMatch = stars ?: return null
            val starCount = starsMatch.groupValues[2].toIntOrNull() ?: return null
            PinnedRepo(
                name = header.groupValues[1],
                description = header.groupValues[2],
                url = starsMatch.groupValues[1],
                stars = starCount,
            )
        }
        repos += repo
    }
    if (!cursor.expect("),")) return null
    return repos
}

@Suppress("ReturnCount")
private fun parseLanguages(cursor: LineCursor): List<LanguageShare>? {
    val languages = mutableListOf<LanguageShare>()
    while (cursor.peek()?.startsWith("LanguageShare(") == true) {
        val match = cursor.take()?.let(languageShareRegex::matchEntire) ?: return null
        val language = RepoLanguage.entries.find { it.name == match.groupValues[1] } ?: return null
        val share = match.groupValues[2].toFloatOrNull() ?: return null
        languages += LanguageShare(language = language, share = share)
    }
    if (!cursor.expect("),")) return null
    return languages
}

@Suppress("ReturnCount")
private fun parseLinks(cursor: LineCursor): List<LinkService>? {
    val links = mutableListOf<LinkService>()
    while (cursor.peek()?.startsWith("LinkService(") == true) {
        val match = cursor.take()?.let(linkServiceRegex::matchEntire) ?: return null
        val url = match.groupValues[2]
        links += LinkService(type = linkTypeFor(url), name = match.groupValues[1], url = url)
    }
    if (!cursor.expect("),")) return null
    return links
}

private fun linkTypeFor(url: String): LinkServiceType {
    val host = url.substringAfter("://", url).substringBefore('/').lowercase()
    return when {
        host.isHostOf("github.com") -> LinkServiceType.GitHub
        host.isHostOf("x.com") || host.isHostOf("twitter.com") -> LinkServiceType.X
        host.isHostOf("qiita.com") -> LinkServiceType.Qiita
        host.isHostOf("note.com") -> LinkServiceType.Note
        else -> LinkServiceType.GitHub
    }
}

/** 完全一致またはサブドメイン一致（部分文字列一致だと notgithub.com 等を誤判定する）。 */
private fun String.isHostOf(domain: String): Boolean = this == domain || endsWith(".$domain")
