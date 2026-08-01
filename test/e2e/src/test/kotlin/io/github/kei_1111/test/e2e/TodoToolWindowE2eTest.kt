package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Test

/**
 * Issue 一覧の中身はライブデータ（API 到達性に依存）なので
 * アサートせず、クロームの挙動だけを検証する（ui-testing.md — Scope）。
 */
class TodoToolWindowE2eTest : PlaywrightTestBase() {

    @Test
    fun togglingTodoToolWindowShowsAndHidesThePanel() {
        val toggle = page.locator("#${TestTags.Profile.TOOL_RAIL_TODO_TOGGLE}")
        val panel = page.locator("#${TestTags.Profile.TODO_PANEL}")

        assertThat(panel).hasCount(0)

        // canvas がポインタを奪うので、スクリーンリーダーと同じく合成 click をディスパッチする
        toggle.dispatchEvent("click")
        assertThat(panel).isVisible()

        toggle.dispatchEvent("click")
        assertThat(panel).hasCount(0)
    }
}
