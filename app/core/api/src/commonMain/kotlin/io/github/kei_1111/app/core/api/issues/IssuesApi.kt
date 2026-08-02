package io.github.kei_1111.app.core.api.issues

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.app.core.api.network.getOrNull
import io.github.kei_1111.shared.model.GitHubIssues
import io.ktor.client.HttpClient

interface IssuesApi {
    suspend fun fetchIssues(): GitHubIssues?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class IssuesApiImpl(
    private val client: HttpClient,
) : IssuesApi {

    override suspend fun fetchIssues(): GitHubIssues? =
        client.getOrNull("$API_BASE_URL/api/issues")
}
