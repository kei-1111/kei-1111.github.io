package io.github.kei_1111.app.feature.profile.destination.profile.theme

import io.github.kei_1111.app.core.common.logging.LogEntry
import io.github.kei_1111.app.core.common.logging.LogLevel
import io.github.kei_1111.app.core.designsystem.theme.KeiDarkColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogcatFormatterTest {

    private fun entry(
        level: LogLevel = LogLevel.Info,
        tag: String = "Navigation",
        message: String = "navigate to Profile",
    ) = LogEntry(
        timestamp = "2026-08-10 12:34:56.789",
        level = level,
        tag = tag,
        message = message,
    )

    @Test
    fun laysOutTimestampPidTagPackageBadgeAndMessage() {
        val line = logcatLineFor(entry(), KeiDarkColorScheme).text

        assertEquals(
            "2026-08-10 12:34:56.789  1111-1111  Navigation            io.github.kei_1111   I   navigate to Profile",
            line,
        )
    }

    @Test
    fun keepsOverlongTagsUntruncated() {
        val longTag = "AVeryLongInteractionTagName"

        assertTrue(longTag in logcatLineFor(entry(tag = longTag), KeiDarkColorScheme).text)
    }

    @Test
    fun rendersTheLevelLetterBadgePerLevel() {
        LogLevel.entries.forEach { level ->
            assertTrue(" ${level.letter} " in logcatLineFor(entry(level = level), KeiDarkColorScheme).text)
        }
    }

    @Test
    fun colorsTheMessageByLevel() {
        val debug = logcatLineFor(entry(level = LogLevel.Debug), KeiDarkColorScheme)
        val error = logcatLineFor(entry(level = LogLevel.Error), KeiDarkColorScheme)

        assertEquals(KeiDarkColorScheme.logcatDebug, debug.spanStyles.last().item.color)
        assertEquals(KeiDarkColorScheme.logcatError, error.spanStyles.last().item.color)
    }

    @Test
    fun assignsTheSameTagColorDeterministically() {
        val first = logcatLineFor(entry(), KeiDarkColorScheme).spanStyles[1].item.color
        val second = logcatLineFor(entry(message = "back"), KeiDarkColorScheme).spanStyles[1].item.color

        assertEquals(first, second)
        assertTrue(first in KeiDarkColorScheme.logcatTagColors)
    }
}
