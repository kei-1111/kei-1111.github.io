package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.LinkServiceType

/** 実行（状態更新・Effect 発火）は ProfileViewModel が担う。 */
internal sealed interface TerminalCommand {
    data object Empty : TerminalCommand
    data object Help : TerminalCommand
    data object Ls : TerminalCommand
    data object Whoami : TerminalCommand
    data class OpenPage(val page: EditorPage) : TerminalCommand
    data class OpenLink(val service: LinkServiceType) : TerminalCommand
    data class OpenInvalid(val target: String) : TerminalCommand
    data object OpenUsage : TerminalCommand
    data class Theme(val isDark: Boolean) : TerminalCommand
    data object ThemeUsage : TerminalCommand
    data class Lang(val language: KeiLanguage) : TerminalCommand
    data object LangUsage : TerminalCommand
    data object GradleBuild : TerminalCommand
    data class Unknown(val name: String) : TerminalCommand
}

/** IDE チュローム扱いの英語固定テキスト。 */
internal val TERMINAL_HELP_LINES = listOf(
    "Available commands:",
    "  help              list available commands",
    "  whoami            print a short profile summary",
    "  ls                list files in the project",
    "  open <target>     open a file or link (readme|profile|licenses|github|x|qiita|note)",
    "  theme dark|light  switch the IDE theme",
    "  lang en|ja        switch the display language",
    "  ./gradlew build   run a build",
)

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

internal fun parseTerminalCommand(input: String): TerminalCommand {
    val trimmed = input.trim()
    val tokens = trimmed.split(Regex("\\s+"))
    return when {
        trimmed.isEmpty() -> TerminalCommand.Empty
        // 他コマンドと同様、語間の空白の揺れを許容するためトークン列で照合する
        tokens == listOf("./gradlew", "build") -> TerminalCommand.GradleBuild
        else -> when (tokens.first()) {
            "help" -> TerminalCommand.Help
            "ls" -> TerminalCommand.Ls
            "whoami" -> TerminalCommand.Whoami
            "open" -> parseOpenTarget(tokens.getOrNull(1))
            "theme" -> parseThemeTarget(tokens.getOrNull(1))
            "lang" -> parseLangTarget(tokens.getOrNull(1))
            else -> TerminalCommand.Unknown(tokens.first())
        }
    }
}

private fun parseThemeTarget(target: String?): TerminalCommand = when (target) {
    "dark" -> TerminalCommand.Theme(isDark = true)
    "light" -> TerminalCommand.Theme(isDark = false)
    else -> TerminalCommand.ThemeUsage
}

private fun parseLangTarget(target: String?): TerminalCommand = when (target) {
    "en" -> TerminalCommand.Lang(KeiLanguage.En)
    "ja" -> TerminalCommand.Lang(KeiLanguage.Ja)
    else -> TerminalCommand.LangUsage
}

private fun parseOpenTarget(target: String?): TerminalCommand {
    if (target == null) return TerminalCommand.OpenUsage
    val page = EditorPage.entries.firstOrNull { page ->
        target.equals(page.fileName, ignoreCase = true) || target.equals(page.name, ignoreCase = true)
    }
    val service = LinkServiceType.entries.firstOrNull { it.name.equals(target, ignoreCase = true) }
    return when {
        page != null -> TerminalCommand.OpenPage(page)
        service != null -> TerminalCommand.OpenLink(service)
        else -> TerminalCommand.OpenInvalid(target)
    }
}
