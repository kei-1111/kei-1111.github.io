package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * 出力テキストは canvas 描画で DOM から読めないため、コマンドの効果が DOM に現れる
 * theme コマンド（テーマトグルの aria ラベル変化）で実行を検証する。
 */
class TerminalToolWindowE2eTest : PlaywrightTestBase() {

    @Test
    fun togglingTerminalToolWindowShowsAndHidesThePanel() {
        val profile = ProfilePage(page)

        assertThat(profile.terminalInput()).hasCount(0)

        profile.toggleTerminalRail()
        assertThat(profile.terminalInput()).isVisible()

        profile.toggleTerminalRail()
        assertThat(profile.terminalInput()).hasCount(0)
    }

    @Test
    fun hidingAndClosingTerminalRemovesPanel() {
        val profile = ProfilePage(page)

        profile.toggleTerminalRail()
        assertThat(profile.terminalHide()).isVisible()
        profile.terminalHide().dispatchEvent("click")
        assertThat(profile.terminalHide()).hasCount(0)

        profile.toggleTerminalRail()
        assertThat(profile.terminalTabClose()).isVisible()
        profile.terminalTabClose().dispatchEvent("click")
        assertThat(profile.terminalTabClose()).hasCount(0)
    }

    @Test
    fun executingThemeCommandSwitchesTheme() {
        val profile = ProfilePage(page)

        profile.toggleTerminalRail()
        assertThat(profile.terminalInput()).isVisible()

        // テーマ状態は testTag で特定したトグルの状態値の変化で観測する（文言は固定しない）
        val before = profile.themeState()

        // パネルを開くと入力欄へ自動フォーカスされるため、キーボード入力がそのまま届く
        page.keyboard().type("theme light")
        page.keyboard().press("Enter")

        assertThat(profile.themeToggle()).not().hasAttribute(ProfilePage.ARIA_LABEL_ATTRIBUTE, before)
    }
}
