package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * URL / ブラウザのセットアップは基底クラス側で共通化する（ロケールは ja 固定）。
 * 言語トグルのラベルは「押した結果の言語」を現在言語で示すため、ロケール間で値が交差する。
 */
class LanguageToggleE2eTest : PlaywrightTestBase() {

    @Test
    fun clickingLanguageToggleSwitchesLanguage() {
        // ja ロケール起動なので初期は日本語 → ラベルは「英語に切り替え」
        val toggleJa = page.getByLabel("英語に切り替え")
        assertThat(toggleJa).isVisible()
        assertEquals("ja", page.evaluate("document.documentElement.lang"))

        // canvas がポインタを奪うので、スクリーンリーダーと同じく合成 click をディスパッチする
        toggleJa.dispatchEvent("click")

        val toggleEn = page.getByLabel("Switch to Japanese")
        assertThat(toggleEn).isVisible()
        assertThat(page.getByLabel("英語に切り替え")).hasCount(0)
        assertEquals("en", page.evaluate("document.documentElement.lang"))

        // もう一度押すと日本語へ戻る
        toggleEn.dispatchEvent("click")
        assertThat(page.getByLabel("英語に切り替え")).isVisible()
        assertEquals("ja", page.evaluate("document.documentElement.lang"))
    }
}
