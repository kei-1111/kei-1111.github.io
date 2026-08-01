package io.github.kei_1111.app.core.api.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode

/** Android ターゲットは IDE Preview 専用のため通信しない。全リクエストへ 503 を返す MockEngine（常に取得失敗として扱われる）。 */
internal actual fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }) { config() }
