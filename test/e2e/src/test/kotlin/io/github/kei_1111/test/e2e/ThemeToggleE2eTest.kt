package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * URL / ブラウザのセットアップは基底クラス側で共通化する。
 * テーマは canvas 描画のため、testTag 付きトグルの状態値を不透明な before/after の変化として観測し、
 * ラベル文言は固定しない。
 */
class ThemeToggleE2eTest : PlaywrightTestBase() {

    @Test
    fun clickingThemeToggleUpdatesBrowserThemeColor() {
        val profile = ProfilePage(page)
        val initial = profile.browserThemeColorValue()

        profile.themeToggle().dispatchEvent("click")

        profile.assertBrowserThemeColorChangedFrom(initial)
    }

    @Test
    fun clickingThemeToggleFlipsTheme() {
        val profile = ProfilePage(page)
        val initial = profile.themeState()

        // canvas がポインタを奪うので、スクリーンリーダーと同じく合成 click をディスパッチする
        profile.themeToggle().dispatchEvent("click")

        profile.assertThemeStateChangedFrom(initial)

        // もう一度押すと戻る
        profile.themeToggle().dispatchEvent("click")
        assertThat(profile.themeToggle()).hasAttribute(ProfilePage.ARIA_LABEL_ATTRIBUTE, initial)
    }
}
