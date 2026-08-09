package io.github.kei_1111.app.core.designsystem.language

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KeiLanguageControllerTest {

    // アプリスコープのシングルトンのため、他テストへ状態を持ち越さない
    @AfterTest
    fun resetLanguage() {
        KeiLanguageController.initialize(KeiLanguage.Ja)
    }

    @Test
    fun startsFromTheDetectedInitialLanguage() {
        KeiLanguageController.initialize(KeiLanguage.En)

        assertEquals(KeiLanguage.En, KeiLanguageController.language)
    }

    @Test
    fun togglesBetweenBothLanguages() {
        KeiLanguageController.initialize(KeiLanguage.Ja)

        KeiLanguageController.toggle()
        assertEquals(KeiLanguage.En, KeiLanguageController.language)

        KeiLanguageController.toggle()
        assertEquals(KeiLanguage.Ja, KeiLanguageController.language)
    }
}
