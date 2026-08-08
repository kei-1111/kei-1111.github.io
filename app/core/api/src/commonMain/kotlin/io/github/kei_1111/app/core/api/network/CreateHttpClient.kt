package io.github.kei_1111.app.core.api.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/** プラットフォーム毎のエンジン選択。Android は非出荷ターゲットのため通信しないエンジンを使う。 */
internal expect fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
