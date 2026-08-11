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

    private fun bootDeskProperty(): String =
        page.evaluate("() => document.documentElement.style.getPropertyValue('--boot-desk')") as String

    private fun bodyBackgroundColor(): String =
        page.evaluate("() => getComputedStyle(document.body).backgroundColor") as String

    private fun cssRgbOf(hexColor: String): String {
        val red = hexColor.substring(1, 3).toInt(HEX_RADIX)
        val green = hexColor.substring(3, 5).toInt(HEX_RADIX)
        val blue = hexColor.substring(5, 7).toInt(HEX_RADIX)
        return "rgb($red, $green, $blue)"
    }

    private companion object {
        const val CLIENT_LOADER_GLOB = "**/webApp.js"
        const val HEX_RADIX = 16
    }
}
