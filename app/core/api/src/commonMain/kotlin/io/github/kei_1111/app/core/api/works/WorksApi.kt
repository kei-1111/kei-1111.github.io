package io.github.kei_1111.app.core.api.works

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.Work
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

interface WorksApi {
    suspend fun fetchWorks(): List<Work>?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class WorksApiImpl(
    private val client: HttpClient,
) : WorksApi {

    override suspend fun fetchWorks(): List<Work>? = try {
        val response = client.get("$API_BASE_URL/api/works")
        if (response.status == HttpStatusCode.OK) response.body<List<Work>>() else null
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }
}
