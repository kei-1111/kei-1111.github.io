package io.github.kei_1111.server.routing

import io.github.kei_1111.server.service.ContributionsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.contributions(contributionsService: ContributionsService) {
    get("/api/contributions") {
        val calendar = contributionsService.getContributions()
        if (calendar != null) {
            call.respond(calendar)
        } else {
            // 取得不能時はクライアント側がエラー表示＋再試行で受け止める設計のため 503 を返す。
            call.respond(HttpStatusCode.ServiceUnavailable)
        }
    }
}
