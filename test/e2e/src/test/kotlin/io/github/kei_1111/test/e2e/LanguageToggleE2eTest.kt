package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * 言語トグルのラベルは「押した結果の言語」を現在言語で示すため、ロケール間で値が交差する。
 */
class LanguageToggleE2eTest : PlaywrightTestBase() {

    @Test
    fun clickingLanguageToggleSwitchesLanguage() {
        val toggle = page.locator("#${TestTags.Profile.TITLE_BAR_LANGUAGE_TOGGLE}")

        // ja ロケール起動なので初期は日本語 → ラベルは「英語に切り替え」
        assertThat(page.getByLabel("英語に切り替え")).isVisible()
        assertEquals("ja", page.evaluate("document.documentElement.lang"))

        // canvas がポインタを奪うので、スクリーンリーダーと同じく合成 click をディスパッチする
        toggle.dispatchEvent("click")

        assertThat(page.getByLabel("Switch to Japanese")).isVisible()
        assertThat(page.getByLabel("英語に切り替え")).hasCount(0)
        assertEquals("en", page.evaluate("document.documentElement.lang"))

        toggle.dispatchEvent("click")
        assertThat(page.getByLabel("英語に切り替え")).isVisible()
        assertEquals("ja", page.evaluate("document.documentElement.lang"))
    }
}
