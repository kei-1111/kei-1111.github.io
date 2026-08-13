package io.github.kei_1111.server

import io.github.kei_1111.server.client.GcsPublishedContentClient
import io.github.kei_1111.server.client.GitHubClient
import io.github.kei_1111.server.client.NoPublishedContent
import io.github.kei_1111.server.client.PublishedContentClient
import io.github.kei_1111.server.plugins.ApiRateLimiterName
import io.github.kei_1111.server.plugins.configureCors
import io.github.kei_1111.server.plugins.configureMonitoring
import io.github.kei_1111.server.plugins.configureRateLimit
import io.github.kei_1111.server.plugins.configureSerialization
import io.github.kei_1111.server.plugins.configureStatusPages
import io.github.kei_1111.server.routing.changelog
import io.github.kei_1111.server.routing.contributions
import io.github.kei_1111.server.routing.issues
import io.github.kei_1111.server.routing.profile
import io.github.kei_1111.server.routing.readme
import io.github.kei_1111.server.routing.terminalCommands
import io.github.kei_1111.server.routing.works
import io.github.kei_1111.server.service.ChangelogService
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
            "GITHUB_TOKEN is not configured; the profile statistics will be absent, " +
                "while contributions, issues, and the changelog remain unavailable",
        )
    }

    configureApplication(GitHubClient(token), publishedContentClient())
}

private fun Application.publishedContentClient(): PublishedContentClient {
    val bucket = System.getenv("CONTENT_BUCKET")?.takeIf { it.isNotBlank() }
    val assetBaseUrl = System.getenv("PUBLISHED_ASSET_BASE_URL")?.takeIf { it.isNotBlank() }
    if (bucket == null || assetBaseUrl == null) {
        log.warn(
            "CONTENT_BUCKET and PUBLISHED_ASSET_BASE_URL must both be configured; " +
                "profile, works, readme, and terminal-command requests will answer 503",
        )
        return NoPublishedContent
    }
    // ADC 解決などの構築時エラーで GitHub 由来のエンドポイントごと起動失敗させないよう、公開コンテンツ無しで起動する
    return try {
        GcsPublishedContentClient(bucket = bucket, assetBaseUrl = assetBaseUrl)
    } catch (e: Exception) {
        log.warn(
            "failed to initialize the GCS published-content client; " +
                "profile, works, readme, and terminal-command requests will answer 503",
            e,
        )
        NoPublishedContent
    }
}

/** テストからは MockEngine を積んだ GitHubClient(+必要なら fake の公開コンテンツ)を渡して呼ぶ。 */
internal fun Application.configureApplication(
    gitHubClient: GitHubClient,
    publishedContentClient: PublishedContentClient = NoPublishedContent,
) {
    val profileService = ProfileService(gitHubClient, publishedContentClient)
    val contributionsService = ContributionsService(gitHubClient)
    val issuesService = IssuesService(gitHubClient)
    val changelogService = ChangelogService(gitHubClient)
    val worksService = WorksService(publishedContentClient)
    val readmeService = ReadmeService(publishedContentClient)
    val terminalCommandsService = TerminalCommandsService(publishedContentClient)
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
            changelog(changelogService)
            works(worksService)
            readme(readmeService)
            terminalCommands(terminalCommandsService)
        }
    }
}
