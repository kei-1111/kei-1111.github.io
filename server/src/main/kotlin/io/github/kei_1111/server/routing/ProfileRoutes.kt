package io.github.kei_1111.server.routing

import io.github.kei_1111.server.service.ProfileService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.profile(profileService: ProfileService) {
    get("/api/profile") {
        val profile = profileService.getProfile()
        if (profile != null) {
            call.respond(profile)
        } else {
            call.respond(HttpStatusCode.ServiceUnavailable)
        }
    }
}
