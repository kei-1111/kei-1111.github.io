package io.github.kei_1111.server.routing

import io.github.kei_1111.server.service.WorksService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.works(worksService: WorksService) {
    get("/api/works") {
        val works = worksService.getWorks()
        if (works != null) {
            call.respond(works)
        } else {
            call.respond(HttpStatusCode.ServiceUnavailable)
        }
    }
}
