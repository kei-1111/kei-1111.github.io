package io.github.kei_1111.app.core.api.works

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.app.core.api.network.getOrNull
import io.github.kei_1111.shared.model.Work
import io.ktor.client.HttpClient

interface WorksApi {
    suspend fun fetchWorks(): List<Work>?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class WorksApiImpl(
    private val client: HttpClient,
) : WorksApi {

    override suspend fun fetchWorks(): List<Work>? =
        client.getOrNull("$API_BASE_URL/api/works")
}
