package io.github.kei_1111.app.feature.profile.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.common.logging.LogLevel
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme

/** タグ列の桁揃え幅（実 AS 同様、はみ出すタグは切らずに列がずれる）。 */
private const val TAG_COLUMN_WIDTH = 20

/** wasm アプリにプロセスは無いので、実 Logcat の PID-TID 列は固定の遊び値で埋める。 */
private const val PID_TID = "1111-1111"

/** ログの発生元として表示するパッケージ名（エディタのパンくずと同じ架空のパッケージ）。 */
private const val PACKAGE_NAME = "io.github.kei_1111"

/**
 * 実 AS New UI の Logcat 1 行（時刻・PID-TID・タグ・パッケージ・レベルバッジ・メッセージ）を組み立てる。
 * SyntaxHighlighter と同じく、色は [KeiColorScheme] を引数で受け取る純関数。
 */
internal fun logcatLineFor(entry: LogEntry, colors: KeiColorScheme): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = colors.textCode)) {
        append("${entry.timestamp}  $PID_TID  ")
    }
    withStyle(SpanStyle(color = tagColorFor(entry.tag, colors))) {
        append(entry.tag.padEnd(TAG_COLUMN_WIDTH))
    }
    withStyle(SpanStyle(color = colors.textCode)) {
        append("  $PACKAGE_NAME  ")
    }
    // レベルバッジ: 実 AS のレベル色チップ（角丸は Span では出せないが 12px ではチップに見える）
    withStyle(SpanStyle(color = badgeTextColorFor(entry.level, colors), background = badgeColorFor(entry.level, colors))) {
        append(" ${entry.level.letter} ")
    }
    withStyle(SpanStyle(color = levelColorFor(entry.level, colors))) {
        append("  ${entry.message}")
    }
}

private fun levelColorFor(level: LogLevel, colors: KeiColorScheme): Color = when (level) {
    LogLevel.Debug -> colors.logcatDebug
    LogLevel.Info -> colors.logcatInfo
    LogLevel.Warn -> colors.logcatWarning
    LogLevel.Error -> colors.logcatError
}

private fun badgeColorFor(level: LogLevel, colors: KeiColorScheme): Color = when (level) {
    LogLevel.Debug -> colors.logcatDebugBadge
    LogLevel.Info -> colors.logcatInfoBadge
    LogLevel.Warn -> colors.logcatWarningBadge
    LogLevel.Error -> colors.logcatErrorBadge
}

private fun badgeTextColorFor(level: LogLevel, colors: KeiColorScheme): Color = when (level) {
    LogLevel.Debug -> colors.logcatDebugBadgeText
    LogLevel.Info -> colors.logcatInfoBadgeText
    LogLevel.Warn -> colors.logcatWarningBadgeText
    LogLevel.Error -> colors.logcatErrorBadgeText
}

/** 実 AS 同様、タグ名からパレットの色を決定的に割り当てる。 */
private fun tagColorFor(tag: String, colors: KeiColorScheme): Color =
    colors.logcatTagColors[tag.hashCode().mod(colors.logcatTagColors.size)]
