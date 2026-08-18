package io.github.kei_1111.server.routing

import io.github.kei_1111.server.service.TerminalCommandsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.terminalCommands(terminalCommandsService: TerminalCommandsService) {
    get("/api/terminal-commands") {
        val commands = terminalCommandsService.getTerminalCommands()
        if (commands != null) {
            call.respond(commands)
        } else {
            call.respond(HttpStatusCode.ServiceUnavailable)
        }
    }
}
