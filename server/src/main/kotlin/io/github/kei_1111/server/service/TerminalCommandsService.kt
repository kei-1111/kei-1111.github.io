package io.github.kei_1111.server.service

import io.github.kei_1111.server.content.DefaultTerminalTextCommands
import io.github.kei_1111.shared.model.TerminalTextCommands

class TerminalCommandsService {
    fun getTerminalCommands(): TerminalTextCommands = DefaultTerminalTextCommands
}
