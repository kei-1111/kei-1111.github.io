package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Test

/**
 * サーバーがビルトインの静的プロフィールを配信した（`isFallback`）ときだけ警告バルーンが出ることを
 * route スタブで決定的に検証する。
 */
class FallbackWarningBalloonE2eTest : PlaywrightTestBase() {

    override fun configurePage(page: Page) {
        ProfileApiFixture.fulfill(page, isFallback = true)
    }

    @Test
    fun warnsThatBuiltInContentIsShown() {
        assertThat(page.locator("#${TestTags.Profile.NOTIFICATION_BALLOON_FALLBACK_WARNING}")).isVisible()
    }
}
