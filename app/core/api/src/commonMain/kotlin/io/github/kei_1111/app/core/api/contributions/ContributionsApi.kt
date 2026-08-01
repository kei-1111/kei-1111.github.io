package io.github.kei_1111.app.core.api.contributions

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.ContributionCalendar
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

interface ContributionsApi {
    suspend fun fetchContributions(): ContributionCalendar?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ContributionsApiImpl(
    private val client: HttpClient,
) : ContributionsApi {

    override suspend fun fetchContributions(): ContributionCalendar? = try {
        val response = client.get("$API_BASE_URL/api/contributions")
        if (response.status == HttpStatusCode.OK) response.body<ContributionCalendar>() else null
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }
}
