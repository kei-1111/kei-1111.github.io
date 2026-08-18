package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.fetchMergedPullRequests
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.GitHubChangelog

class ChangelogService(private val gitHubClient: GitHubClient) {
    private val changelogCache = TtlCache<GitHubChangelog>(CHANGELOG_TTL_MILLIS, name = "changelog")

    suspend fun getChangelog(): GitHubChangelog? =
        changelogCache.get { gitHubClient.fetchMergedPullRequests() }

    companion object {
        // GitHub API のレートリミット消費を抑えつつ、changelog のずれが目立たない程度の鮮度に保つ TTL。
        private const val CHANGELOG_TTL_MILLIS = 10L * 60L * 1000L
    }
}
