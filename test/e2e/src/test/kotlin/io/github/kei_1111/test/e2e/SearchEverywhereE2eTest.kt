package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import io.github.kei_1111.test.e2e.page.SearchEverywherePage
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Search Everywhere の検索、絞り込み、アクションを確認する。
 *
 * ダイアログを閉じた後は a11y ミラーが再構築されない（CMP wasm の制約）ため、
 * 閉じた後の断定はミラー非依存の信号（ヘッダー帯の画素変化・localStorage）で行う。
 * 結果選択 → ページを開く状態遷移そのものは SearchEverywhereViewModelTest /
 * ProfileViewModelTest が担う。
 */
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
    fun selectingResultClosesDialog() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        profile.clickSearch()
        assertThat(search.result("licenses")).isVisible()

        search.assertClosesDialog {
            search.result("licenses").dispatchEvent("click")
        }
    }

    @Test
    fun escapeClosesSearch() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        profile.clickSearch()
        assertThat(search.field).isVisible()

        search.assertClosesDialog {
            search.pressEscape()
        }
    }

    @Test
    fun switchThemeActionPersistsThemeChange() {
        val profile = ProfilePage(page)
        val search = SearchEverywherePage(page)

        // テーマは変更時のみ DataStore(WebLocalStorage) に保存されるため、実行前後の値比較で断定する
        val before = themeStorageValue()

        profile.clickSearch()
        search.tab("actions").dispatchEvent("click")
        assertThat(search.result("switch-theme")).isVisible()
        search.result("switch-theme").dispatchEvent("click")

        val deadline = System.currentTimeMillis() + STORAGE_TIMEOUT_MS
        var after = themeStorageValue()
        while (after == before && System.currentTimeMillis() < deadline) {
            Thread.sleep(POLL_INTERVAL_MS)
            after = themeStorageValue()
        }
        assertNotEquals(before, after, "Switch Theme 実行後もテーマの保存値が変化しなかった")
    }

    /** app:core:data の THEME_DATA_STORE_NAME と同じキー。ドリフト時はこのテストが落ちて検知される。 */
    private fun themeStorageValue(): String? =
        page.evaluate("() => localStorage.getItem('theme.preferences_pb')") as String?

    private companion object {
        const val STORAGE_TIMEOUT_MS = 10_000L
        const val POLL_INTERVAL_MS = 300L
    }
}
