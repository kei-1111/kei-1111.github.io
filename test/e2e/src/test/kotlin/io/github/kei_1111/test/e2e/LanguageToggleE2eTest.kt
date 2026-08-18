package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import io.github.kei_1111.test.e2e.page.SplashPage
import io.github.kei_1111.test.tags.TestTags
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

    @Test
    fun keepsTheSelectedLanguageAcrossReload() {
        val html = page.locator("html")
        switchToEnglishAndAwaitPersistence()

        page.reload()
        SplashPage(page).waitUntilProfileAppears()

        assertThat(html).hasAttribute(LANG_ATTRIBUTE, LANG_EN)
    }

    @Test
    fun fallsBackToBrowserLocaleAfterStoredSettingsAreCleared() {
        val html = page.locator("html")
        switchToEnglishAndAwaitPersistence()
        page.evaluate("() => localStorage.clear()")

        page.reload()
        SplashPage(page).waitUntilProfileAppears()

        assertThat(html).hasAttribute(LANG_ATTRIBUTE, LANG_JA)
    }

    /**
     * 保存はトグルから非同期に走るため、リロードが書き込みを追い越さないよう保存先キーの出現まで待つ。
     */
    private fun switchToEnglishAndAwaitPersistence() {
        page.locator("#${TestTags.Profile.TITLE_BAR_LANGUAGE_TOGGLE}").dispatchEvent("click")
        assertThat(page.locator("html")).hasAttribute(LANG_ATTRIBUTE, LANG_EN)
        page.waitForFunction("() => localStorage.getItem('$SETTINGS_STORE_KEY') !== null")
    }

    private companion object {
        /** Web 標準の属性名と BCP 47 言語タグ（UI 文言ではない固定語彙）。 */
        const val LANG_ATTRIBUTE = "lang"
        const val LANG_JA = "ja"
        const val LANG_EN = "en"

        /** 永続化先の localStorage キー（DataStore 名）。保存完了を決定的に待つために参照する。 */
        const val SETTINGS_STORE_KEY = "settings.preferences_pb"
    }
}
