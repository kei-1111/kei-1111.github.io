package io.github.kei_1111.app.core.api.changelog

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.app.core.api.network.getOrNull
import io.github.kei_1111.shared.model.GitHubChangelog
import io.ktor.client.HttpClient

interface ChangelogApi {
    suspend fun fetchChangelog(): GitHubChangelog?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ChangelogApiImpl(
    private val client: HttpClient,
) : ChangelogApi {

    override suspend fun fetchChangelog(): GitHubChangelog? =
        client.getOrNull("$API_BASE_URL/api/changelog")
}
