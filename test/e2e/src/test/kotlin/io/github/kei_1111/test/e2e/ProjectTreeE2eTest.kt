package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

class ProjectTreeE2eTest : PlaywrightTestBase() {

    @Test
    fun selectingTreeItemsOpensEditorTabs() {
        val profile = ProfilePage(page)

        assertThat(profile.tab("readme")).isVisible()

        profile.clickTreeItem("profile")
        assertThat(profile.tab("profile")).isVisible()

        profile.clickTreeItem("licenses")
        assertThat(profile.tab("licenses")).isVisible()
    }
}
