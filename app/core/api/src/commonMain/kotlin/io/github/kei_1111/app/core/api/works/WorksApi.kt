package io.github.kei_1111.app.core.api.works

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.app.core.api.network.getOrNull
import io.github.kei_1111.shared.model.Works
import io.ktor.client.HttpClient

interface WorksApi {
    suspend fun fetchWorks(): Works?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class WorksApiImpl(
    private val client: HttpClient,
) : WorksApi {

    override suspend fun fetchWorks(): Works? =
        client.getOrNull("$API_BASE_URL/api/works")
}
