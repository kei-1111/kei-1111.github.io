package io.github.kei_1111.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    install(CORS) {
        allowHost("kei-1111.github.io", schemes = listOf("https"))
        allowHost("localhost:8080")
        // ローカル検証用ホストの許可(カンマ区切りの host[:port]。例: "localhost:8081,localhost:8123")。本番では未設定 = 挙動不変。
        System.getenv("DEV_CORS_HOSTS")
            ?.split(',')
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            ?.forEach { allowHost(it) }
    }
}
