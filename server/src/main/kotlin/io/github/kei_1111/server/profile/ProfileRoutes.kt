package io.github.kei_1111.server.profile

import io.github.kei_1111.server.github.GitHubClient
import io.github.kei_1111.server.github.ProfileStats
import io.github.kei_1111.server.github.TtlCache
import io.github.kei_1111.server.github.fetchProfileStats
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

// GitHub API のレートリミット消費を抑えつつ、統計のずれが目立たない程度の鮮度に保つ TTL。
private const val STATS_TTL_MILLIS = 10L * 60L * 1000L

internal fun Route.profile(gitHubClient: GitHubClient) {
    val statsCache = TtlCache<ProfileStats>(STATS_TTL_MILLIS)
    get("/api/profile") {
        val stats = statsCache.get { gitHubClient.fetchProfileStats() }
        val profile = if (stats != null) {
            DefaultGitHubProfile.copy(
                followers = stats.followers,
                following = stats.following,
                repos = stats.repos,
                totalStars = stats.totalStars,
            )
        } else {
            DefaultGitHubProfile
        }
        call.respond(profile)
    }
}
