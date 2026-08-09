package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Test

/**
 * PR 一覧は本番 API に依存させず route スタブで固定する（ui-testing.md — Scope:
 * 本番サーバーへのエンドポイント未デプロイ期間でも決定的に通る）。
 */
class ChangelogToolWindowE2eTest : PlaywrightTestBase() {

    override fun configurePage(page: Page) {
        ChangelogApiFixture.fulfill(page)
    }

    @Test
    fun togglingChangelogToolWindowShowsAndHidesThePanel() {
        val toggle = page.locator("#${TestTags.Profile.TOOL_RAIL_CHANGELOG}")
        val panel = page.locator("#${TestTags.Profile.CHANGELOG_PANEL}")

        assertThat(panel).hasCount(0)

        // canvas がポインタを奪うので、スクリーンリーダーと同じく合成 click をディスパッチする
        toggle.dispatchEvent("click")
        assertThat(panel).isVisible()

        toggle.dispatchEvent("click")
        assertThat(panel).hasCount(0)
    }

    @Test
    fun showsStubbedMergedPullRequestRows() {
        page.locator("#${TestTags.Profile.TOOL_RAIL_CHANGELOG}").dispatchEvent("click")

        assertThat(page.locator("#${TestTags.Profile.changelogRow("204")}")).isVisible()
        assertThat(page.locator("#${TestTags.Profile.changelogRow("203")}")).isVisible()
    }

    @Test
    fun hidesThePanelFromTheHeaderHideButton() {
        page.locator("#${TestTags.Profile.TOOL_RAIL_CHANGELOG}").dispatchEvent("click")
        val panel = page.locator("#${TestTags.Profile.CHANGELOG_PANEL}")
        assertThat(panel).isVisible()

        page.locator("#${TestTags.Profile.CHANGELOG_HIDE}").dispatchEvent("click")

        assertThat(panel).hasCount(0)
    }
}
