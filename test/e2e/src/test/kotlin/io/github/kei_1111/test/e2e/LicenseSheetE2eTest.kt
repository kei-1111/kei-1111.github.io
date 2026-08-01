package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

class LicenseSheetE2eTest : PlaywrightTestBase() {

    @Test
    fun sheetClosesFromHeaderFooterAndScrim() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("licenses")
        profile.clickLicenseRow(LICENSE_KEY)
        assertThat(profile.sheetClose()).isVisible()
        profile.sheetClose().dispatchEvent("click")
        assertThat(profile.sheetClose()).hasCount(0)

        profile.clickLicenseRow(LICENSE_KEY)
        assertThat(profile.sheetCloseFooter()).isVisible()
        profile.sheetCloseFooter().dispatchEvent("click")
        assertThat(profile.sheetClose()).hasCount(0)

        profile.clickLicenseRow(LICENSE_KEY)
        assertThat(profile.sheetScrim()).isVisible()
        profile.sheetScrim().dispatchEvent("click")
        assertThat(profile.sheetClose()).hasCount(0)
    }

    private companion object {
        const val LICENSE_KEY = "intellij-platform-icons"
    }
}
