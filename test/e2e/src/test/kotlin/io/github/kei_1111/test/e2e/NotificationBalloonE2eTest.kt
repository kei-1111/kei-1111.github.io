package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Test

/**
 * 初回訪問（コンテキスト毎に localStorage が空）で更新バルーンが出ることと、そのアクションで
 * Git ツールウィンドウが開くことを route スタブで決定的に検証する。
 */
class NotificationBalloonE2eTest : PlaywrightTestBase() {

    override fun configurePage(page: Page) {
        ChangelogApiFixture.fulfill(page)
        ProfileApiFixture.fulfill(page, hasStatistics = true)
    }

    @Test
    fun showsTheUpdateBalloonOnAFirstVisit() {
        assertThat(page.locator("#${TestTags.Profile.NOTIFICATION_BALLOON_SITE_UPDATED}")).isVisible()
        assertThat(page.locator("#${TestTags.Profile.NOTIFICATION_BALLOON_FALLBACK_WARNING}")).hasCount(0)
    }

    @Test
    fun opensTheChangelogFromTheUpdateBalloon() {
        page.locator("#${TestTags.Profile.NOTIFICATION_BALLOON_SITE_UPDATED_ACTION}").dispatchEvent("click")

        assertThat(page.locator("#${TestTags.Profile.CHANGELOG_PANEL}")).isVisible()
        assertThat(page.locator("#${TestTags.Profile.NOTIFICATION_BALLOON_SITE_UPDATED}")).hasCount(0)
    }
}
