package io.github.kei_1111.test.e2e.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags

/**
 * Splash → Profile への遷移を共通化する Page Object。
 * Splash 自体は操作対象を持たないため、Profile 側の要素（タイトルバーのテーマ切替ボタン。
 * Desktop/Mobile どちらのレイアウトでも常に描画される）の出現をもって遷移完了とみなす。
 */
class SplashPage(private val page: Page) {

    fun waitUntilProfileAppears() {
        assertThat(page.locator("#${TestTags.TITLE_BAR_THEME_TOGGLE}")).isVisible()
    }
}
