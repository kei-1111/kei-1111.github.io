package io.github.kei_1111.server.content

import io.github.kei_1111.shared.model.TerminalTextCommand
import io.github.kei_1111.shared.model.TerminalTextCommands
import kotlinx.collections.immutable.persistentListOf

/** GCS 公開コンテンツが未公開・取得失敗のときに配信するターミナルのテキストコマンド一覧。 */
internal val DefaultTerminalTextCommands = TerminalTextCommands(
    items = persistentListOf(
        TerminalTextCommand(
            keyword = "neofetch",
            description = "show portfolio system info",
            lines = persistentListOf(
                " _  __  _____   ___    kei@kei-1111.github.io",
                "| |/ / | ____| |_ _|   ----------------------",
                "| ' /  |  _|    | |    OS: Android Studio New UI (Web Edition)",
                "| . \\  | |___   | |    Host: GitHub Pages",
                "|_|\\_\\ |_____| |___|   Kernel: Kotlin/Wasm + Compose Multiplatform",
                "                       Shell: zsh (portfolio flavored)",
                "                       Theme: Islands Dark / Islands Light",
                "                       Server: Ktor on Cloud Run",
            ),
        ),
        TerminalTextCommand(
            keyword = "sudo",
            description = "run a command as another user",
            lines = persistentListOf(
                "kei is not in the sudoers file. This incident will be reported.",
            ),
        ),
    ),
)
