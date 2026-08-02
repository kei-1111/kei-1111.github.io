package io.github.kei_1111.app.core.api.network

import io.github.kei_1111.app.core.common.coroutines.recoverOrElse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

/** GET [url] し、200 なら [T] にデシリアライズ、それ以外・失敗は null に畳む（cancellation は伝播）。 */
internal suspend inline fun <reified T> HttpClient.getOrNull(url: String): T? =
    recoverOrElse({
        val response = get(url)
        if (response.status == HttpStatusCode.OK) response.body<T>() else null
    }) { null }
