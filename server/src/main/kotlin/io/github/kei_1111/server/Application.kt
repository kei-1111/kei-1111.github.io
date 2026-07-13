package io.github.kei_1111.server

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.plugins.configureCors
import io.github.kei_1111.server.plugins.configureMonitoring
import io.github.kei_1111.server.plugins.configureSerialization
import io.github.kei_1111.server.plugins.configureStatusPages
import io.github.kei_1111.server.routing.contributions
import io.github.kei_1111.server.routing.profile
import io.github.kei_1111.server.service.ContributionsService
import io.github.kei_1111.server.service.ProfileService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

// wasm dev server (8080) との衝突を避けたローカル既定ポート。Cloud Run では PORT が注入される。
private const val DEFAULT_PORT = 8081

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT
    embeddedServer(CIO, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    // 空文字も未設定として扱う。シークレット設定ミスが無言で恒久フォールバック化するのを防ぐため起動時に警告する。
    val token = System.getenv("GITHUB_TOKEN")?.takeIf { it.isNotBlank() }
    if (token == null) {
        log.warn("GITHUB_TOKEN is not configured; GitHub-backed data is disabled and static fallbacks will be served")
    }

    configureApplication(GitHubClient(token))
}

/** プラグイン・サービス・ルートの配線。テストからは MockEngine を積んだ GitHubClient を渡して呼ぶ。 */
internal fun Application.configureApplication(gitHubClient: GitHubClient) {
    val profileService = ProfileService(gitHubClient)
    val contributionsService = ContributionsService(gitHubClient)
    monitor.subscribe(ApplicationStopped) { gitHubClient.close() }

    configureSerialization()
    configureCors()
    configureMonitoring()
    configureStatusPages()

    routing {
        // Cloud Run では Google のフロントエンドが /healthz を横取りして 404 を返すため、この名前は使えない。
        get("/health") {
            call.respondText("OK")
        }
        profile(profileService)
        contributions(contributionsService)
    }
}
