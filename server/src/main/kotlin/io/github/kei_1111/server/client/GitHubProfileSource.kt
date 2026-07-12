package io.github.kei_1111.server.client

import kotlinx.serialization.Serializable

// 公開 endpoint で任意ユーザーを受けるとレートリミット枯渇の攻撃面になるため login は固定する。
internal const val PROFILE_LOGIN = "kei-1111"

internal val PROFILE_STATS_QUERY = """
    query(${'$'}login: String!) {
      user(login: ${'$'}login) {
        followers { totalCount }
        following { totalCount }
        repositories(ownerAffiliations: [OWNER], privacy: PUBLIC) { totalCount }
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
    val repositories: TotalCount,
    val starredRepositories: TotalCount,
)

@Serializable
internal data class TotalCount(val totalCount: Int = 0)

internal data class ProfileStats(
    val followers: Int,
    val following: Int,
    val repos: Int,
    val totalStars: Int,
)

internal suspend fun GitHubClient.fetchProfileStats(): ProfileStats? {
    val user = execute<ProfileStatsData>(PROFILE_STATS_QUERY, mapOf("login" to PROFILE_LOGIN))?.user ?: return null
    return ProfileStats(
        followers = user.followers.totalCount,
        following = user.following.totalCount,
        repos = user.repositories.totalCount,
        // totalStars は「kei-1111 がスターを付けたリポジトリ数」(プロフィールカードの「★ N」表示に対応)。
        totalStars = user.starredRepositories.totalCount,
    )
}
