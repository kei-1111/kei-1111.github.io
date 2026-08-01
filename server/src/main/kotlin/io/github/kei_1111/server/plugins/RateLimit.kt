package io.github.kei_1111.server.plugins

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.request.header
import kotlin.time.Duration.Companion.minutes

internal const val API_RATE_LIMIT_PER_MINUTE = 60

internal val ApiRateLimiterName = RateLimitName("api")

fun Application.configureRateLimit() {
    install(RateLimit) {
        register(ApiRateLimiterName) {
            rateLimiter(limit = API_RATE_LIMIT_PER_MINUTE, refillPeriod = 1.minutes)
            requestKey { call ->
                // Cloud Run のフロントエンドが末尾に追加するクライアント IP のみを信頼する。
                call.request.header(HttpHeaders.XForwardedFor)
                    ?.substringAfterLast(',')
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: call.request.origin.remoteHost
            }
        }
    }
}
