package io.github.kei_1111.app.core.designsystem.language

import kotlin.test.Test
import kotlin.test.assertEquals

class KeiLanguageTest {

    @Test
    fun resolvesJapaneseFromExactTag() {
        assertEquals(KeiLanguage.Ja, KeiLanguage.fromTag("ja"))
    }

    @Test
    fun resolvesJapaneseFromRegionalTagByPrefix() {
        assertEquals(KeiLanguage.Ja, KeiLanguage.fromTag("ja-JP"))
    }

    @Test
    fun fallsBackToEnglishForNonJapaneseTag() {
        assertEquals(KeiLanguage.En, KeiLanguage.fromTag("en-US"))
    }

    @Test
    fun fallsBackToEnglishForNullTag() {
        assertEquals(KeiLanguage.En, KeiLanguage.fromTag(null))
    }

    @Test
    fun doesNotMatchJapaneseAppearingMidTag() {
        assertEquals(KeiLanguage.En, KeiLanguage.fromTag("en-ja"))
    }
}
