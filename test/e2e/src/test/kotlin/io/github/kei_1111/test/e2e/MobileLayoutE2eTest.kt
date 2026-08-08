package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

class MobileLayoutE2eTest : PlaywrightTestBase() {

    override val viewport = 800 to 900

    @Test
    fun selectingTreeItemClosesMobileOverlay() {
        val profile = ProfilePage(page)

        // README.md はツリー最下部でオーバーレイの表示範囲外になりうるため、上部の profile 行を可視性のアンカーにする
        assertThat(profile.treeItem("profile")).hasCount(0)

        profile.toggleProjectRail()
        assertThat(profile.treeItem("profile")).isVisible()

        profile.clickTreeItem("licenses")
        assertThat(profile.tab("licenses")).isVisible()
        assertThat(profile.treeItem("licenses")).hasCount(0)
    }
}
