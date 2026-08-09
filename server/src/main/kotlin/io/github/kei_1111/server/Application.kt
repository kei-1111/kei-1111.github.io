package io.github.kei_1111.server

import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.plugins.ApiRateLimiterName
import io.github.kei_1111.server.plugins.configureCors
import io.github.kei_1111.server.plugins.configureMonitoring
import io.github.kei_1111.server.plugins.configureRateLimit
import io.github.kei_1111.server.plugins.configureSerialization
import io.github.kei_1111.server.plugins.configureStatusPages
import io.github.kei_1111.server.routing.contributions
import io.github.kei_1111.server.routing.issues
import io.github.kei_1111.server.routing.profile
import io.github.kei_1111.server.routing.readme
import io.github.kei_1111.server.routing.terminalCommands
import io.github.kei_1111.server.routing.works
import io.github.kei_1111.server.service.ContributionsService
import io.github.kei_1111.server.service.IssuesService
import io.github.kei_1111.server.service.ProfileService
import io.github.kei_1111.server.service.ReadmeService
import io.github.kei_1111.server.service.TerminalCommandsService
import io.github.kei_1111.server.service.WorksService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.ratelimit.rateLimit
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
    val token = System.getenv("GITHUB_TOKEN")?.takeIf { it.isNotBlank() }
    if (token == null) {
        log.warn(
            "GITHUB_TOKEN is not configured; static profile content will be served, " +
                "while contributions and issues remain unavailable",
        )
    }

    configureApplication(GitHubClient(token))
}

/** テストからは MockEngine を積んだ GitHubClient を渡して呼ぶ。 */
internal fun Application.configureApplication(gitHubClient: GitHubClient) {
    val profileService = ProfileService(gitHubClient)
    val contributionsService = ContributionsService(gitHubClient)
    val issuesService = IssuesService(gitHubClient)
    val worksService = WorksService()
    val readmeService = ReadmeService()
    val terminalCommandsService = TerminalCommandsService()
    monitor.subscribe(ApplicationStopped) { gitHubClient.close() }

    configureSerialization()
    configureCors()
    configureRateLimit()
    configureMonitoring()
    configureStatusPages()

    routing {
        // Cloud Run では Google のフロントエンドが /healthz を横取りして 404 を返すため、この名前は使えない。
        get("/health") {
            call.respondText("OK")
        }
        rateLimit(ApiRateLimiterName) {
            profile(profileService)
            contributions(contributionsService)
            issues(issuesService)
            works(worksService)
            readme(readmeService)
            terminalCommands(terminalCommandsService)
        }
    }
}
