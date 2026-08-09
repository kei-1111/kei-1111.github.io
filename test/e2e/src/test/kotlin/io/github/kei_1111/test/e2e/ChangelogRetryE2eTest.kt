package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Test

/**
 * 取得失敗 → retry 回復の分岐を route スタブで決定的に検証する
 * （起動時 fetch を 503 に固定し、retry の前に成功応答へ差し替える）。
 */
class ChangelogRetryE2eTest : PlaywrightTestBase() {

    override fun configurePage(page: Page) {
        ChangelogApiFixture.fulfillUnavailable(page)
    }

    @Test
    fun recoversFromAFailedFetchViaRetry() {
        page.locator("#${TestTags.Profile.TOOL_RAIL_CHANGELOG}").dispatchEvent("click")
        val retry = page.locator("#${TestTags.Profile.CHANGELOG_RETRY}")
        assertThat(retry).isVisible()

        page.unroute("**/api/changelog")
        ChangelogApiFixture.fulfill(page)
        retry.dispatchEvent("click")

        assertThat(page.locator("#${TestTags.Profile.changelogRow("204")}")).isVisible()
    }
}
