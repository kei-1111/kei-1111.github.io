package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.LinkServiceType
import io.github.kei_1111.shared.model.TerminalTextCommand

/** 実行（状態更新・Effect 発火）は ProfileViewModel が担う。 */
internal sealed interface ParsedTerminalCommand {
    data object Empty : ParsedTerminalCommand
    data object Help : ParsedTerminalCommand
    data object Ls : ParsedTerminalCommand
    data object Whoami : ParsedTerminalCommand
    data class OpenPage(val page: EditorPage) : ParsedTerminalCommand
    data class OpenLink(val service: LinkServiceType) : ParsedTerminalCommand
    data class OpenInvalid(val target: String) : ParsedTerminalCommand
    data object OpenUsage : ParsedTerminalCommand
    data class Theme(val isDark: Boolean) : ParsedTerminalCommand
    data object ThemeUsage : ParsedTerminalCommand
    data class Lang(val language: KeiLanguage) : ParsedTerminalCommand
    data object LangUsage : ParsedTerminalCommand
    data object GradleBuild : ParsedTerminalCommand
    data class Unknown(val name: String) : ParsedTerminalCommand
    data class Text(val lines: List<String>) : ParsedTerminalCommand
}

private const val HELP_KEYWORD_COLUMN_WIDTH = 18

// パーサーがビルトイン優先で照合するため、これらの keyword を持つサーバー定義は実行不能 — help に掲載しない。
private val reservedTerminalKeywords = setOf("help", "whoami", "ls", "open", "theme", "lang")

/** IDE クローム扱いの英語固定テキスト。 */
internal fun terminalHelpLines(serverCommands: List<TerminalTextCommand>): List<String> =
    listOf(
        "Available commands:",
        "  help              list available commands",
        "  whoami            print a short profile summary",
        "  ls                list files in the project",
        "  open <target>     open a file or link (readme|profile|works|licenses|github|x|qiita|note)",
        "  theme dark|light  switch the IDE theme",
        "  lang en|ja        switch the display language",
        "  ./gradlew build   run a build",
    ) + serverCommands
        .filterNot { it.keyword in reservedTerminalKeywords }
        .map { "  ${it.keyword.padEnd(HELP_KEYWORD_COLUMN_WIDTH - 1)} ${it.description}" }

/** `./gradlew build` がリプレイする Splash 風ビルドログ（行前の遅延 + 行）。 */
@Suppress("MagicNumber") // 遅延はステップごとに異なる演出値で、定数化しても意味が生まれない
internal val TERMINAL_BUILD_LOG_STEPS = listOf(
    TerminalBuildStep(300L, TerminalLine("Loading JetBrains Mono… done", TerminalLineKind.Output)),
    TerminalBuildStep(300L, TerminalLine("Loading Noto Sans JP… done", TerminalLineKind.Output)),
    TerminalBuildStep(300L, TerminalLine("Loading Zen Kaku Gothic New… done", TerminalLineKind.Output)),
    TerminalBuildStep(500L, TerminalLine("Rendering ProfilePreview… done", TerminalLineKind.Output)),
    TerminalBuildStep(200L, TerminalLine("", TerminalLineKind.Output)),
    TerminalBuildStep(0L, TerminalLine("BUILD SUCCESSFUL in 2s", TerminalLineKind.Success)),
    TerminalBuildStep(0L, TerminalLine("4 actionable tasks: 4 executed", TerminalLineKind.Output)),
)

internal data class TerminalBuildStep(val delayMillis: Long, val line: TerminalLine)

internal fun parseTerminalCommand(
    input: String,
    serverCommands: List<TerminalTextCommand>,
): ParsedTerminalCommand {
    val trimmed = input.trim()
    val tokens = trimmed.split(Regex("\\s+"))
    return when {
        trimmed.isEmpty() -> ParsedTerminalCommand.Empty
        // 他コマンドと同様、語間の空白の揺れを許容するためトークン列で照合する
        tokens == listOf("./gradlew", "build") -> ParsedTerminalCommand.GradleBuild
        else -> when (tokens.first()) {
            "help" -> ParsedTerminalCommand.Help
            "ls" -> ParsedTerminalCommand.Ls
            "whoami" -> ParsedTerminalCommand.Whoami
            "open" -> parseOpenTarget(tokens.getOrNull(1))
            "theme" -> parseThemeTarget(tokens.getOrNull(1))
            "lang" -> parseLangTarget(tokens.getOrNull(1))
            else ->
                serverCommands
                    .firstOrNull { it.keyword == tokens.first() }
                    ?.let { ParsedTerminalCommand.Text(it.lines) }
                    ?: ParsedTerminalCommand.Unknown(tokens.first())
        }
    }
}

private fun parseThemeTarget(target: String?): ParsedTerminalCommand = when (target) {
    "dark" -> ParsedTerminalCommand.Theme(isDark = true)
    "light" -> ParsedTerminalCommand.Theme(isDark = false)
    else -> ParsedTerminalCommand.ThemeUsage
}

private fun parseLangTarget(target: String?): ParsedTerminalCommand = when (target) {
    "en" -> ParsedTerminalCommand.Lang(KeiLanguage.En)
    "ja" -> ParsedTerminalCommand.Lang(KeiLanguage.Ja)
    else -> ParsedTerminalCommand.LangUsage
}

private fun parseOpenTarget(target: String?): ParsedTerminalCommand {
    if (target == null) return ParsedTerminalCommand.OpenUsage
    val page = EditorPage.entries.firstOrNull { page ->
        target.equals(page.fileName, ignoreCase = true) || target.equals(page.name, ignoreCase = true)
    }
    val service = LinkServiceType.entries.firstOrNull { it.name.equals(target, ignoreCase = true) }
    return when {
        page != null -> ParsedTerminalCommand.OpenPage(page)
        service != null -> ParsedTerminalCommand.OpenLink(service)
        else -> ParsedTerminalCommand.OpenInvalid(target)
    }
}
