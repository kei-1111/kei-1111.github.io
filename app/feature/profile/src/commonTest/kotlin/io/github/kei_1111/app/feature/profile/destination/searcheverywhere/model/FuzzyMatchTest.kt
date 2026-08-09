package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FuzzyMatchTest {

    @Test
    fun returnsNullWhenTheQueryIsNotASubsequence() {
        assertNull(fuzzyScore("x", "abc"))
    }

    @Test
    fun returnsNullWhenCharactersAppearOutOfOrder() {
        assertNull(fuzzyScore("ca", "abc"))
    }

    @Test
    fun scoresTheEmptyQueryAsZero() {
        assertEquals(0, fuzzyScore("", "abc"))
    }

    @Test
    fun matchesCaseInsensitively() {
        assertEquals(fuzzyScore("w", "works"), fuzzyScore("W", "works"))
    }

    @Test
    fun addsTheCandidateStartBonus() {
        // 一致1 + 語頭3 + 先頭5
        assertEquals(9, fuzzyScore("w", "works"))
        // 先頭でない語中一致は一致1のみ
        assertEquals(1, fuzzyScore("w", "aworks"))
    }

    @Test
    fun addsTheConsecutiveBonusPerAdjacentMatch() {
        // 先頭 "a"(1+3) + 連続 "b"(1+2) + 先頭ボーナス5
        assertEquals(12, fuzzyScore("ab", "ab"))
        // 離れた一致は連続ボーナスなし: "a"(1+3) + "c"(1) + 先頭5
        assertEquals(10, fuzzyScore("ac", "abc"))
    }

    @Test
    fun addsTheWordStartBonusAfterSeparators() {
        // 空白区切りの語頭: 一致1 + 語頭3
        assertEquals(4, fuzzyScore("b", "a b"))
        // camelCase 境界も語頭扱い
        assertEquals(4, fuzzyScore("b", "aB"))
    }

    @Test
    fun ranksWordStartMatchesAboveMidWordMatches() {
        val wordStart = assertNotNull(fuzzyScore("s", "a screen"))
        val midWord = assertNotNull(fuzzyScore("s", "as"))

        assertTrue(wordStart > midWord)
    }
}
