package io.github.kei_1111.app.core.api.contributions

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.app.core.api.network.getOrNull
import io.github.kei_1111.shared.model.ContributionCalendar
import io.ktor.client.HttpClient

interface ContributionsApi {
    suspend fun fetchContributions(): ContributionCalendar?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ContributionsApiImpl(
    private val client: HttpClient,
) : ContributionsApi {

    override suspend fun fetchContributions(): ContributionCalendar? =
        client.getOrNull("$API_BASE_URL/api/contributions")
}
