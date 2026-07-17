package io.github.kei_1111.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    install(CORS) {
        allowHost("kei-1111.github.io", schemes = listOf("https"))
        allowHost("localhost:8080")
    }
}
