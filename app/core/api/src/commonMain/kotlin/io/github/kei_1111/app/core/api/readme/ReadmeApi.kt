package io.github.kei_1111.app.core.api.readme

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.app.core.api.network.getOrNull
import io.github.kei_1111.shared.model.Readme
import io.ktor.client.HttpClient

interface ReadmeApi {
    suspend fun fetchReadme(): Readme?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ReadmeApiImpl(
    private val client: HttpClient,
) : ReadmeApi {

    override suspend fun fetchReadme(): Readme? =
        client.getOrNull("$API_BASE_URL/api/readme")
}
