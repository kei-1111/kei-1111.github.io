package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

class BackNavigationE2eTest : PlaywrightTestBase() {

    @Test
    fun escapeOnProfileKeepsProfileMountedAndOperable() {
        val profile = ProfilePage(page)

        profile.pressEscape()
        page.waitForTimeout(POP_TRANSITION_SETTLE_MS)

        assertThat(profile.themeToggle()).isVisible()
        profile.clickTreeItem("licenses")
        assertThat(profile.tab("licenses")).isVisible()
    }

    private companion object {
        /**
         * 退場中のデスティネーションは pop トランジションの間 DOM に残るため、これを待たずに
         * 断定するとバックスタックが減っていても素通りする。pop トランジションを上回る長さにする。
         */
        const val POP_TRANSITION_SETTLE_MS = 2500.0
    }
}
