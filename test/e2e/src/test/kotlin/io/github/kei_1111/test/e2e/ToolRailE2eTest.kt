package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * ツールレールからサイドバーと Logcat を開閉できることを確認する。
 */
class ToolRailE2eTest : PlaywrightTestBase() {

    @Test
    fun togglingProjectRailShowsAndHidesTree() {
        val profile = ProfilePage(page)

        profile.toggleProjectRail()
        assertThat(profile.treeItem("readme")).hasCount(0)

        profile.toggleProjectRail()
        // 再生成された a11y ミラーノードはサイズ 0 のままで isVisible にならない（CMP wasm の制約）。
        // 存在の断定に加え、ツリーからページを開けることで機能面から再表示を断定する
        assertThat(profile.treeItem("readme")).hasCount(1)
        profile.clickTreeItem("licenses")
        assertThat(profile.tab("licenses")).isVisible()
    }

    @Test
    fun hidingAndClosingLogcatRemovesPanel() {
        val profile = ProfilePage(page)

        profile.toggleLogcatRail()
        assertThat(profile.logcatHide()).isVisible()
        profile.logcatHide().dispatchEvent("click")
        assertThat(profile.logcatHide()).hasCount(0)

        profile.toggleLogcatRail()
        assertThat(profile.logcatTabClose()).isVisible()
        profile.logcatTabClose().dispatchEvent("click")
        assertThat(profile.logcatTabClose()).hasCount(0)
    }
}
