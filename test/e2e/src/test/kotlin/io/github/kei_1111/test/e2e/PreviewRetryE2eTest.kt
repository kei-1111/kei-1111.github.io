package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class PreviewRetryE2eTest : PlaywrightTestBase() {

    // 本番 API の到達可否（CORS 設定など）に依存させず、プロフィール取得の失敗状態を決定的に作る
    override fun configurePage(page: Page) {
        page.route("**/api/profile") { it.abort() }
    }

    @Test
    fun retryRequestsProfileAgain() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("profile")
        assertThat(profile.previewRetry()).isVisible()

        val request = page.waitForRequest("**/api/profile") {
            profile.previewRetry().dispatchEvent("click")
        }

        assertNotNull(request)
    }
}
