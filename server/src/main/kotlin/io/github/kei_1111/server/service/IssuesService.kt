package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.fetchOpenIssues
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.GitHubIssues

class IssuesService(private val gitHubClient: GitHubClient) {
    private val issuesCache = TtlCache<GitHubIssues>(ISSUES_TTL_MILLIS, name = "issues")

    suspend fun getIssues(): GitHubIssues? =
        issuesCache.get { gitHubClient.fetchOpenIssues() }

    companion object {
        // GitHub API のレートリミット消費を抑えつつ、Issue 一覧のずれが目立たない程度の鮮度に保つ TTL。
        private const val ISSUES_TTL_MILLIS = 10L * 60L * 1000L
    }
}
