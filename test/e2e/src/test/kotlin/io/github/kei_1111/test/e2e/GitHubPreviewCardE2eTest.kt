package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/** プロフィール取得が成功したときの表示側。失敗側は PreviewRetryE2eTest が担当する。 */
class GitHubPreviewCardE2eTest : PlaywrightTestBase() {

    override fun configurePage(page: Page) {
        ProfileApiFixture.fulfill(page)
    }

    @Test
    fun rendersTheProfileCardAndItsSourceTogether() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("profile")

        // 実 AS 同様、コードペインとプレビューが同じデータで同時に立つ
        assertThat(profile.gitHubCard()).isVisible()
        assertThat(profile.editorInput()).isVisible()
        assertThat(profile.previewRetry()).hasCount(0)
    }

    @Test
    fun hidesTheCardInCodeOnlyModeAndBringsItBack() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("profile")
        assertThat(profile.gitHubCard()).isVisible()

        profile.viewModeCode()
        assertThat(profile.gitHubCard()).hasCount(0)

        profile.viewModeSplit()
        assertThat(profile.gitHubCard()).isVisible()
    }
}
