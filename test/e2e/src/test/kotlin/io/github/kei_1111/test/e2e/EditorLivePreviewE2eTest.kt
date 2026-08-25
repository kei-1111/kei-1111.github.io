package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

/**
 * 看板機能であるコード編集 → ライブプレビューの往復。プレビューは canvas 描画のため、
 * testTag で特定した要素のテキストを before/after の不透明な変化として観測する（文言は固定しない）。
 * README は Markdown なので任意の入力でもパースに成功し、常にプレビューへ反映される。
 */
class EditorLivePreviewE2eTest : PlaywrightTestBase() {

    override fun configurePage(page: Page) {
        ReadmeApiFixture.fulfill(page)
    }

    @Test
    fun typingInTheEditorUpdatesThePreview() {
        val profile = ProfilePage(page)

        // 起動直後の既定タブが README。フィクスチャ到着でプレビューが埋まるのを待つ
        assertThat(profile.readmePreview()).not().isEmpty()
        val before = profile.readmePreview().textContent()

        profile.typeInEditor("E2E")

        assertThat(profile.readmePreview())
            .hasText(Pattern.compile("^(?!${Pattern.quote(before)}$).+$", Pattern.DOTALL))
    }

    @Test
    fun keepsTheEditorAndPreviewOnTheSamePageAfterSwitchingTabs() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("licenses")
        assertThat(profile.readmePreview()).hasCount(0)

        profile.clickTab("readme")

        assertThat(profile.readmePreview()).isVisible()
        assertThat(profile.editorInput()).isVisible()
    }
}
