package io.github.kei_1111.server.routing

import io.github.kei_1111.server.content.DefaultWorks
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.works() {
    get("/api/works") {
        call.respond(DefaultWorks)
    }
}
