package io.github.kei_1111.server.contributions

import io.github.kei_1111.server.github.GitHubClient
import io.github.kei_1111.server.github.TtlCache
import io.github.kei_1111.server.github.fetchContributions
import io.github.kei_1111.shared.model.ContributionCalendar
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

// GitHub API のレートリミット消費を抑えつつ、統計のずれが目立たない程度の鮮度に保つ TTL。
private const val CONTRIBUTIONS_TTL_MILLIS = 10L * 60L * 1000L

internal fun Route.contributions(gitHubClient: GitHubClient) {
    val calendarCache = TtlCache<ContributionCalendar>(CONTRIBUTIONS_TTL_MILLIS)
    get("/api/contributions") {
        val calendar = calendarCache.get { gitHubClient.fetchContributions() }
        if (calendar != null) {
            call.respond(calendar)
        } else {
            // 取得不能時はクライアント側の FallbackContributions が受け止める設計のため 503 を返す。
            call.respond(HttpStatusCode.ServiceUnavailable)
        }
    }
}
