package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import io.github.kei_1111.test.e2e.page.SearchEverywherePage
import org.junit.jupiter.api.Test

/** Search Everywhere の検索、絞り込み、アクションを確認する。 */
class SearchEverywhereE2eTest : PlaywrightTestBase() {

    @Test
    fun typingQueryFiltersResults() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        profile.clickSearch()
        assertThat(search.field).isVisible()
        // 空クエリでは全ページが並ぶため、README が消えることでタイピング到達を断定する
        assertThat(search.result("readme")).isVisible()

        search.typeQuery("License")

        assertThat(search.result("readme")).hasCount(0)
        assertThat(search.result("licenses")).isVisible()
    }

    @Test
    fun selectingResultOpensPageAndClosesSearch() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        profile.clickSearch()
        assertThat(search.result("licenses")).isVisible()

        search.clickResult("licenses")

        search.assertClosed()
        assertThat(profile.tab("licenses")).isVisible()
    }

    @Test
    fun escapeClosesSearch() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        profile.clickSearch()
        assertThat(search.field).isVisible()

        search.pressEscape()

        search.assertClosed()
        assertThat(profile.tab("readme")).isVisible()
    }

    @Test
    fun clickingOutsideClosesSearchAndKeepsProfileMirrorUsable() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        profile.clickSearch()
        assertThat(search.field).isVisible()
        page.mouse().click(OUTSIDE_PANEL_X, OUTSIDE_PANEL_Y)

        search.assertClosed()
        assertThat(profile.tab("readme")).isVisible()
    }

    @Test
    fun closingSearchKeepsProfileMirrorUsable() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        profile.clickSearch()
        assertThat(search.field).isVisible()
        search.pressEscape()

        assertThat(profile.tab("readme")).isVisible()
        profile.clickTreeItem("licenses")
        assertThat(profile.tab("licenses")).isVisible()
    }

    @Test
    fun switchThemeActionChangesRenderedThemeAndClosesSearch() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        val before = profile.themeState()

        profile.clickSearch()
        search.clickTab("actions")
        assertThat(search.result("switch-theme")).isVisible()
        search.clickResult("switch-theme")

        search.assertClosed()
        profile.assertThemeStateChangedFrom(before)
    }

    private companion object {
        const val OUTSIDE_PANEL_X = 8.0
        const val OUTSIDE_PANEL_Y = 8.0
    }
}
