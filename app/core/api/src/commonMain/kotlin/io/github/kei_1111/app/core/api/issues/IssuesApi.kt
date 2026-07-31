package io.github.kei_1111.app.core.api.issues

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.GitHubIssues
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

interface IssuesApi {
    /** オープン Issue 一覧を取得してパースする。失敗時は null。 */
    suspend fun fetchIssues(): GitHubIssues?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class IssuesApiImpl(
    private val client: HttpClient,
) : IssuesApi {

    // 失敗（非200・ネットワークエラー・タイムアウト・パース失敗）はすべて null に畳む。キャンセルだけは伝播する。
    override suspend fun fetchIssues(): GitHubIssues? = try {
        val response = client.get("$API_BASE_URL/api/issues")
        if (response.status == HttpStatusCode.OK) response.body<GitHubIssues>() else null
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }
}
