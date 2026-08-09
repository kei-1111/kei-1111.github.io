package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LanguageShare
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.PinnedRepo
import io.github.kei_1111.shared.model.RepoLanguage
import kotlinx.collections.immutable.toImmutableList

private val stringFieldRegex = Regex("""(\w+) = "($KOTLIN_STRING_BODY_PATTERN)",""")
private val intFieldRegex = Regex("""(\w+) = (-?\d+),""")
private val languageFieldRegex = Regex("""language = RepoLanguage\("($KOTLIN_STRING_BODY_PATTERN)"\),""")
private val shareFieldRegex = Regex("""share = (\d+(?:\.\d+)?)f,""")

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

internal class LineCursor(private val lines: List<String>) {
    private var index = 0

    fun peek(): String? = lines.getOrNull(index)

    fun take(): String? = lines.getOrNull(index++)

    fun expect(expected: String): Boolean = take() == expected

    fun isAtEnd(): Boolean = index == lines.size
}

private fun pinnedRepoCode(repo: PinnedRepo, language: KeiLanguage): String {
    val meta = repo.language
        ?.let { "language = RepoLanguage(\"${escapeKotlinString(it.name)}\")" }
        ?: "stars = ${repo.stars}"
    val name = escapeKotlinString(repo.name)
    val description = escapeKotlinString(repo.description.forLanguage(language))
    val url = escapeKotlinString(repo.url)
    return listOf(
        "|                PinnedRepo(",
        "|                    name = \"$name\",",
        "|                    description = \"$description\",",
        "|                    url = \"$url\",",
        "|                    $meta,",
        "|                ),",
    ).joinToString("\n")
}

private fun languageShareCode(entry: LanguageShare): String = listOf(
    "|                LanguageShare(",
    "|                    language = RepoLanguage(\"${escapeKotlinString(entry.language.name)}\"),",
    "|                    share = ${entry.share}f,",
    "|                ),",
).joinToString("\n")

private fun linkServiceCode(link: LinkService): String {
    val name = escapeKotlinString(link.name)
    val url = escapeKotlinString(link.url)
    return listOf(
        "|                LinkService(",
        "|                    name = \"$name\",",
        "|                    url = \"$url\",",
        "|                ),",
    ).joinToString("\n")
}

internal fun profileCode(profileData: GitHubProfile, language: KeiLanguage): String = """
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
    |            name = "${escapeKotlinString(profileData.name.forLanguage(language))}",
    |            handle = "${escapeKotlinString(profileData.handle)}",
    |            location = "${escapeKotlinString(profileData.location)}",
    |            role = "${escapeKotlinString(profileData.role)}",
    |            followers = ${profileData.followers},
    |            following = ${profileData.following},
    |            repos = ${profileData.repos},
    |            totalStars = ${profileData.totalStars},
    |            pinnedRepos = listOf(
    ${profileData.pinnedRepos.joinToString("\n") { pinnedRepoCode(it, language) }}
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
 * コードは単一言語の投影なので、編集された文字列は ja / en 両方へ同値で持ち上げる
 * （以後の言語切替では編集値がそのまま両言語に表示される）。
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
        name = LocalizedText(ja = scalars.name, en = scalars.name),
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
    return match.groupValues[2].takeIf { match.groupValues[1] == key }?.let(::unescapeKotlinString)
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
        val name = cursor.stringField("name") ?: return null
        val description = cursor.stringField("description") ?: return null
        val url = cursor.stringField("url") ?: return null
        val metadata = cursor.take() ?: return null
        val language = languageFieldRegex.matchEntire(metadata)
        val stars = intFieldRegex.matchEntire(metadata)?.takeIf { it.groupValues[1] == "stars" }
        val hasExactlyOneMetadataField = (language == null) != (stars == null)
        if (!hasExactlyOneMetadataField || !cursor.expect("),")) return null
        repos += parsePinnedRepo(name, description, url, language, stars) ?: return null
    }
    if (!cursor.expect("),")) return null
    return repos
}

@Suppress("ReturnCount")
private fun parsePinnedRepo(
    name: String,
    description: String,
    url: String,
    language: MatchResult?,
    stars: MatchResult?,
): PinnedRepo? {
    return if (language != null) {
        val languageName = unescapeKotlinString(language.groupValues[1]) ?: return null
        PinnedRepo(
            name = name,
            description = LocalizedText(ja = description, en = description),
            url = url,
            language = RepoLanguage(languageName),
        )
    } else {
        val starsMatch = stars ?: return null
        val starCount = starsMatch.groupValues[2].toIntOrNull() ?: return null
        PinnedRepo(
            name = name,
            description = LocalizedText(ja = description, en = description),
            url = url,
            stars = starCount,
        )
    }
}

@Suppress("ReturnCount")
private fun parseLanguages(cursor: LineCursor): List<LanguageShare>? {
    val languages = mutableListOf<LanguageShare>()
    while (cursor.peek() == "LanguageShare(") {
        cursor.take()
        val languageMatch = cursor.take()?.let(languageFieldRegex::matchEntire) ?: return null
        val languageName = unescapeKotlinString(languageMatch.groupValues[1]) ?: return null
        val shareMatch = cursor.take()?.let(shareFieldRegex::matchEntire) ?: return null
        val share = shareMatch.groupValues[1].toFloatOrNull() ?: return null
        if (!cursor.expect("),")) return null
        languages += LanguageShare(language = RepoLanguage(languageName), share = share)
    }
    if (!cursor.expect("),")) return null
    return languages
}

@Suppress("ReturnCount")
private fun parseLinks(cursor: LineCursor): List<LinkService>? {
    val links = mutableListOf<LinkService>()
    while (cursor.peek() == "LinkService(") {
        cursor.take()
        val name = cursor.stringField("name") ?: return null
        val url = cursor.stringField("url") ?: return null
        if (!cursor.expect("),")) return null
        links += LinkService(type = linkTypeFor(url), name = name, url = url)
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

/** 部分文字列一致だと notgithub.com 等を誤判定する。 */
private fun String.isHostOf(domain: String): Boolean = this == domain || endsWith(".$domain")
