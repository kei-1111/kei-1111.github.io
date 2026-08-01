package io.github.kei_1111.app.core.api.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TIMEOUT_MS = 8_000L

@BindingContainer
@ContributesTo(AppScope::class)
interface HttpClientBindings {

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideHttpClient(): HttpClient = createHttpClient {
            install(ContentNegotiation) {
                // ignoreUnknownKeys は契約互換性(旧 client + 新 server)のため必須 — shared/model の互換性ルールを参照。
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MS
            }
        }
    }
}
