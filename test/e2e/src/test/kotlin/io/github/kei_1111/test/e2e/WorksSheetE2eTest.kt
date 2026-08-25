package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

class WorksSheetE2eTest : PlaywrightTestBase() {

    // 本番 API の到達可否に依存させず、作品一覧を決定的なフィクスチャで返す。
    override fun configurePage(page: Page) {
        WorksApiFixture.fulfill(page)
    }

    @Test
    fun sheetOpensFromDetailButtonAndClosesFromCloseButtonOrScrim() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("works")

        profile.clickWorksDetail()
        assertThat(profile.worksSheetClose()).isVisible()
        profile.worksSheetClose().dispatchEvent("click")
        assertThat(profile.worksSheetClose()).hasCount(0)

        profile.clickWorksDetail()
        assertThat(profile.worksSheetScrim()).isVisible()
        profile.worksSheetScrim().dispatchEvent("click")
        assertThat(profile.worksSheetClose()).hasCount(0)
    }
}
