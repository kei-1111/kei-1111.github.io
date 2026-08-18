package io.github.kei_1111.test.e2e

import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * 起動前ペイントの検証。wasm ローダーの取得を中断してクライアントを起動させないことで、
 * canvas 描画前のページだけを観測する（起動後は canvas が Shadow DOM 内にあるため、
 * canvas の有無では起動判定できない）。色は保存済みの値と突き合わせ、リテラルは固定しない。
 */
class PreBootThemeE2eTest : PlaywrightTestBase() {

    @Test
    fun storedColorPaintsThePageBeforeTheClientBoots() {
        val profile = ProfilePage(page)
        val initial = profile.browserThemeColorValue()
        profile.themeToggle().dispatchEvent("click")
        profile.assertBrowserThemeColorChangedFrom(initial)
        val stored = profile.browserThemeColorValue()

        reloadWithoutClient()

        assertEquals(stored, bootDeskProperty())
        assertEquals(cssRgbOf(stored), bodyBackgroundColor())
        assertEquals(stored, profile.browserThemeColorValue())
    }

    @Test
    fun missingStoredColorFallsBackToTheStylesheetDefault() {
        val profile = ProfilePage(page)
        val fallback = profile.browserThemeColorValue()
        page.evaluate("() => localStorage.clear()")

        reloadWithoutClient()

        assertEquals("", bootDeskProperty())
        assertEquals(cssRgbOf(fallback), bodyBackgroundColor())
        assertEquals(fallback, profile.browserThemeColorValue())
    }

    private fun reloadWithoutClient() {
        page.route(CLIENT_LOADER_GLOB) { it.abort() }
        page.reload()
    }

    private companion object {
        const val CLIENT_LOADER_GLOB = "**/webApp.js"
    }
}
