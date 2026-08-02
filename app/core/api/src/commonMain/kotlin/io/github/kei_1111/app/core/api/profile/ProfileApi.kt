package io.github.kei_1111.app.core.api.profile

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.app.core.api.network.getOrNull
import io.github.kei_1111.shared.model.GitHubProfile
import io.ktor.client.HttpClient

interface ProfileApi {
    suspend fun fetchProfile(): GitHubProfile?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ProfileApiImpl(
    private val client: HttpClient,
) : ProfileApi {

    override suspend fun fetchProfile(): GitHubProfile? =
        client.getOrNull("$API_BASE_URL/api/profile")
}
