package io.github.kei_1111.server.routing

import io.github.kei_1111.server.service.ReadmeService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.readme(readmeService: ReadmeService) {
    get("/api/readme") {
        call.respond(readmeService.getReadme())
    }
}
