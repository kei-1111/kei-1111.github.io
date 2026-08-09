package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * URL / ブラウザのセットアップは基底クラス側で共通化する（ロケールは ja 固定）。
 */
class LanguageToggleE2eTest : PlaywrightTestBase() {

    @Test
    fun clickingLanguageToggleSwitchesLanguage() {
        val toggle = ProfilePage(page).languageToggle()
        val html = page.locator("html")

        assertThat(html).hasAttribute(LANG_ATTRIBUTE, LANG_JA)

        // canvas がポインタを奪うので、スクリーンリーダーと同じく合成 click をディスパッチする
        toggle.dispatchEvent("click")

        assertThat(html).hasAttribute(LANG_ATTRIBUTE, LANG_EN)

        // もう一度押すと日本語へ戻る
        toggle.dispatchEvent("click")
        assertThat(html).hasAttribute(LANG_ATTRIBUTE, LANG_JA)
    }

    private companion object {
        /** Web 標準の属性名と BCP 47 言語タグ（UI 文言ではない固定語彙）。 */
        const val LANG_ATTRIBUTE = "lang"
        const val LANG_JA = "ja"
        const val LANG_EN = "en"
    }
}
