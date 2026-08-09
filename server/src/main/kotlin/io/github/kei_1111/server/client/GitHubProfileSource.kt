package io.github.kei_1111.server.client

import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

// 公開 endpoint で任意ユーザーを受けるとレートリミット枯渇の攻撃面になるため login は固定する。
internal const val PROFILE_LOGIN = "kei-1111"

internal val PROFILE_STATS_QUERY = """
    query(${'$'}login: String!) {
      user(login: ${'$'}login) {
        followers { totalCount }
        following { totalCount }
        repositories(first: 100, ownerAffiliations: [OWNER], privacy: PUBLIC) {
          totalCount
          nodes {
            languages(first: 20) {
              edges {
                size
                node { name color }
              }
            }
          }
        }
        starredRepositories { totalCount }
      }
    }
""".trimIndent()

@Serializable
internal data class ProfileStatsData(val user: GitHubUser? = null)

@Serializable
internal data class GitHubUser(
    val followers: TotalCount,
    val following: TotalCount,
    val repositories: RepositoryConnection,
    val starredRepositories: TotalCount,
)

@Serializable
internal data class TotalCount(val totalCount: Int = 0)

@Serializable
internal data class RepositoryConnection(
    val totalCount: Int = 0,
    val nodes: List<RepositoryNode> = emptyList(),
)

@Serializable
internal data class RepositoryNode(val languages: LanguageConnection = LanguageConnection())

@Serializable
internal data class LanguageConnection(val edges: List<LanguageEdge> = emptyList())

@Serializable
internal data class LanguageEdge(
    val size: Long,
    val node: LanguageNode,
)

@Serializable
internal data class LanguageNode(
    val name: String,
    val color: String? = null,
)

internal data class LanguageBytes(
    val name: String,
    val color: String?,
    val size: Long,
)

internal data class ProfileStats(
    val followers: Int,
    val following: Int,
    val repos: Int,
    val totalStars: Int,
    val languageSizes: List<LanguageBytes>,
)

private val logger = LoggerFactory.getLogger("io.github.kei_1111.server.client.GitHubProfileSource")

// HTTP 200 かつ errors なしでも user は null になり得る(アカウント改名やトークンのスコープ不足)。
private fun ProfileStatsData.userOrWarn(): GitHubUser? {
    if (user == null) logger.warn("GitHub GraphQL API returned a null user for login '{}'", PROFILE_LOGIN)
    return user
}

private fun RepositoryConnection.aggregateLanguageSizes(): List<LanguageBytes> {
    val sizesByLanguage = linkedMapOf<String, LanguageBytes>()
    nodes.flatMap { it.languages.edges }.forEach { edge ->
        val existing = sizesByLanguage[edge.node.name]
        sizesByLanguage[edge.node.name] = LanguageBytes(
            name = edge.node.name,
            color = existing?.color ?: edge.node.color,
            size = (existing?.size ?: 0L) + edge.size,
        )
    }
    return sizesByLanguage.values.toList()
}

internal suspend fun GitHubClient.fetchProfileStats(): ProfileStats? {
    val user = execute<ProfileStatsData>(PROFILE_STATS_QUERY, mapOf("login" to PROFILE_LOGIN))
        ?.userOrWarn()
        ?: return null
    return ProfileStats(
        followers = user.followers.totalCount,
        following = user.following.totalCount,
        repos = user.repositories.totalCount,
        // totalStars は「kei-1111 がスターを付けたリポジトリ数」(プロフィールカードの「★ N」表示に対応)。
        totalStars = user.starredRepositories.totalCount,
        languageSizes = user.repositories.aggregateLanguageSizes(),
    )
}
