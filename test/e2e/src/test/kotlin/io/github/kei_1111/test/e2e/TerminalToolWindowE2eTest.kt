package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * Terminal ツールウィンドウの開閉とコマンド実行。
 * 出力テキストは canvas 描画で DOM から読めないため、コマンドの効果が DOM に現れる
 * theme コマンド（テーマトグルの aria ラベル変化）で実行を検証する。
 */
class TerminalToolWindowE2eTest : PlaywrightTestBase() {

    @Test
    fun togglingTerminalToolWindowShowsAndHidesThePanel() {
        val profile = ProfilePage(page)

        // 初期状態ではパネルはコンポーズされていない
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

        // 初期テーマはダーク（PlaywrightTestBase は ja ロケール固定）
        assertThat(page.getByLabel("ライトモードに切り替え")).isVisible()

        // パネルを開くと入力欄へ自動フォーカスされるため、キーボード入力がそのまま届く
        page.keyboard().type("theme light")
        page.keyboard().press("Enter")

        assertThat(page.getByLabel("ダークモードに切り替え")).isVisible()
        assertThat(page.getByLabel("ライトモードに切り替え")).hasCount(0)
    }
}
