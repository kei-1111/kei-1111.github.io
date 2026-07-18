package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.fetchContributions
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.ContributionCalendar

class ContributionsService(private val gitHubClient: GitHubClient) {
    private val calendarCache = TtlCache<ContributionCalendar>(CONTRIBUTIONS_TTL_MILLIS)

    suspend fun getContributions(): ContributionCalendar? =
        calendarCache.get { gitHubClient.fetchContributions() }

    companion object {
        // GitHub API のレートリミット消費を抑えつつ、統計のずれが目立たない程度の鮮度に保つ TTL。
        private const val CONTRIBUTIONS_TTL_MILLIS = 10L * 60L * 1000L
    }
}
