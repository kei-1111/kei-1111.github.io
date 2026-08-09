package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.fetchOpenIssues
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.GitHubIssues

class IssuesService(private val gitHubClient: GitHubClient) {
    private val issuesCache = TtlCache<GitHubIssues>(GITHUB_DATA_TTL_MILLIS, name = "issues")

    suspend fun getIssues(): GitHubIssues? =
        issuesCache.get { gitHubClient.fetchOpenIssues() }
}
