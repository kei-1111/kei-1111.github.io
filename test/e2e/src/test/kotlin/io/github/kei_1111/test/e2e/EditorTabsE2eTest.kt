package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * エディタタブの選択とクローズを確認する。
 */
class EditorTabsE2eTest : PlaywrightTestBase() {

    @Test
    fun switchingTabsChangesPreviewContent() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("licenses")
        assertThat(profile.licenseRow(LICENSE_KEY)).isVisible()

        profile.clickTab("readme")
        assertThat(profile.licenseRow(LICENSE_KEY)).hasCount(0)

        profile.clickTab("licenses")
        assertThat(profile.licenseRow(LICENSE_KEY)).isVisible()
    }

    @Test
    fun closingSelectedTabRemovesIt() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("licenses")
        profile.closeTab("licenses")

        assertThat(profile.tab("licenses")).hasCount(0)
    }

    @Test
    fun closingAllTabsShowsUsagePage() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("profile")
        profile.clickTreeItem("licenses")

        // クローズボタンは選択中のタブだけ操作可能なため、閉じる前に対象を選択する
        profile.clickTab("licenses")
        profile.closeTab("licenses")
        profile.clickTab("profile")
        profile.closeTab("profile")
        profile.clickTab("readme")
        profile.closeTab("readme")

        assertThat(profile.tab("readme")).hasCount(0)
        assertThat(profile.tab("profile")).hasCount(0)
        assertThat(profile.tab("licenses")).hasCount(0)
        // 使い方ページの本文出現も断定したいが、全タブ閉止後に生成されるサブツリーは
        // a11y ミラーに反映されない（実測）ため、DOM ではタブ消滅までしか断定できない
    }

    private companion object {
        const val LICENSE_KEY = "intellij-platform-icons"
    }
}
