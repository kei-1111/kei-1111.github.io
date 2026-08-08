package io.github.kei_1111.server.routing

import io.github.kei_1111.server.service.WorksService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.works(worksService: WorksService) {
    get("/api/works") {
        call.respond(worksService.getWorks())
    }
}
