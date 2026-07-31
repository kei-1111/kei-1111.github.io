package io.github.kei_1111.app.core.api.profile

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.network.API_BASE_URL
import io.github.kei_1111.shared.model.GitHubProfile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

interface ProfileApi {
    /** プロフィールを取得してパースする。失敗時は null。 */
    suspend fun fetchProfile(): GitHubProfile?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ProfileApiImpl(
    private val client: HttpClient,
) : ProfileApi {

    // 失敗（非200・ネットワークエラー・タイムアウト・パース失敗）はすべて null に畳む。キャンセルだけは伝播する。
    override suspend fun fetchProfile(): GitHubProfile? = try {
        val response = client.get("$API_BASE_URL/api/profile")
        if (response.status == HttpStatusCode.OK) response.body<GitHubProfile>() else null
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }
}
