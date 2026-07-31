package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Test

/**
 * Terminal ツールウィンドウの開閉とコマンド実行の E2E。
 * 出力テキストは canvas 描画で DOM から読めないため、コマンドの効果が DOM に現れる
 * theme コマンド（テーマトグルの aria ラベル変化）で実行を検証する。
 */
class TerminalE2eTest : PlaywrightTestBase() {

    @Test
    fun opensTerminalAndSwitchesThemeViaCommand() {
        val toggle = page.locator("#${TestTags.Profile.TOOL_RAIL_TERMINAL}")
        // canvas がポインタを奪うので、スクリーンリーダーと同じく合成 click をディスパッチする
        toggle.dispatchEvent("click")

        val input = page.locator("#${TestTags.Profile.TERMINAL_INPUT}")
        assertThat(input).isVisible()

        // 初期テーマはダーク（PlaywrightTestBase は ja ロケール固定）
        assertThat(page.getByLabel("ライトモードに切り替え")).isVisible()

        // パネルを開くと入力欄へ自動フォーカスされるため、キーボード入力がそのまま届く
        page.keyboard().type("theme light")
        page.keyboard().press("Enter")

        assertThat(page.getByLabel("ダークモードに切り替え")).isVisible()
        assertThat(page.getByLabel("ライトモードに切り替え")).hasCount(0)
    }

    @Test
    fun closesTerminalOnSecondRailClick() {
        val toggle = page.locator("#${TestTags.Profile.TOOL_RAIL_TERMINAL}")
        toggle.dispatchEvent("click")
        assertThat(page.locator("#${TestTags.Profile.TERMINAL_INPUT}")).isVisible()

        toggle.dispatchEvent("click")
        assertThat(page.locator("#${TestTags.Profile.TERMINAL_INPUT}")).hasCount(0)
    }
}
