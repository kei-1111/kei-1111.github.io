package io.github.kei_1111.server.service

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.fetchContributions
import io.github.kei_1111.server.util.TtlCache
import io.github.kei_1111.shared.model.ContributionCalendar

class ContributionsService(private val gitHubClient: GitHubClient) {
    private val calendarCache = TtlCache<ContributionCalendar>(GITHUB_DATA_TTL_MILLIS, name = "contributions")

    suspend fun getContributions(): ContributionCalendar? =
        calendarCache.get { gitHubClient.fetchContributions() }
}
