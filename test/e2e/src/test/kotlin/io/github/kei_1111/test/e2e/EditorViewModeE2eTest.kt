package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

class EditorViewModeE2eTest : PlaywrightTestBase() {

    @Test
    fun switchingViewModeShowsAndHidesPreview() {
        val profile = ProfilePage(page)
        val licenseRow = profile.licenseRow("intellij-platform-icons")

        profile.clickTreeItem("licenses")
        assertThat(licenseRow).isVisible()

        profile.viewModeCode()
        assertThat(licenseRow).hasCount(0)

        profile.viewModePreview()
        assertThat(licenseRow).isVisible()

        profile.viewModeSplit()
        assertThat(licenseRow).isVisible()
    }
}
