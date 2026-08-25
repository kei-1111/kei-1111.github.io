package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

/**
 * InteractionLog → Logcat パネルの配線。行の文言はライブ操作に依存するため固定せず、
 * 操作を挟んだ before/after の不透明な変化として観測する。
 */
class LogcatEntriesE2eTest : PlaywrightTestBase() {

    @Test
    fun appendsAnEntryWhenAnInteractionIsLogged() {
        val profile = ProfilePage(page)

        profile.toggleLogcatRail()
        assertThat(profile.logcatEntries()).not().isEmpty()
        val before = profile.logcatEntries().textContent()

        profile.clickTreeItem("licenses")

        assertThat(profile.logcatEntries())
            .hasText(Pattern.compile("^(?!${Pattern.quote(before)}$).+$", Pattern.DOTALL))
    }

    @Test
    fun clearsTheEntriesOnDemand() {
        val profile = ProfilePage(page)

        profile.toggleLogcatRail()
        assertThat(profile.logcatEntries()).not().isEmpty()

        profile.clearLogcat()

        assertThat(profile.logcatEntries()).isEmpty()
    }
}
