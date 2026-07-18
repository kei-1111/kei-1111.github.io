package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.ProfileStats
import io.github.kei_1111.server.client.fetchProfileStats
import io.github.kei_1111.server.content.DefaultGitHubProfile
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.GitHubProfile

class ProfileService(private val gitHubClient: GitHubClient) {
    private val statsCache = TtlCache<ProfileStats>(STATS_TTL_MILLIS)

    suspend fun getProfile(): GitHubProfile {
        val stats = statsCache.get { gitHubClient.fetchProfileStats() }
        return if (stats != null) {
            DefaultGitHubProfile.copy(
                followers = stats.followers,
                following = stats.following,
                repos = stats.repos,
                totalStars = stats.totalStars,
            )
        } else {
            DefaultGitHubProfile
        }
    }

    companion object {
        // GitHub API のレートリミット消費を抑えつつ、統計のずれが目立たない程度の鮮度に保つ TTL。
        private const val STATS_TTL_MILLIS = 10L * 60L * 1000L
    }
}
