package io.github.kei_1111.app.feature.profile.destination.profile.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KotlinStringLiteralsTest {

    @Test
    fun escapesEverySpecialCharacter() {
        assertEquals("""a\\b\"c\nd\re\tf\${'$'}g""", escapeKotlinString("a\\b\"c\nd\re\tf\$g"))
    }

    @Test
    fun passesPlainTextThroughUnchanged() {
        assertEquals("日本語 plain 123", escapeKotlinString("日本語 plain 123"))
    }

    @Test
    fun roundTripsEscapedValues() {
        val original = "say \"hi\" \\ ok\n\ttabbed \$var"

        assertEquals(original, unescapeKotlinString(escapeKotlinString(original)))
    }

    @Test
    fun unescapesSequencesTheGeneratorDoesNotEmit() {
        assertEquals("\b'", unescapeKotlinString("""\b\'"""))
    }

    @Test
    fun rejectsUnknownEscapeSequences() {
        assertNull(unescapeKotlinString("""a\qb"""))
    }

    @Test
    fun rejectsATrailingLoneBackslash() {
        assertNull(unescapeKotlinString("""abc\"""))
    }
}
