package io.github.kei_1111.test.e2e.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags

/**
 * Search Everywhere の検索と絞り込みをまとめる Page Object。
 */
class SearchEverywherePage(private val page: Page) {

    val field: Locator
        get() = page.locator("#${TestTags.SearchEverywhere.FIELD}")

    fun tab(key: String): Locator = page.locator("#${TestTags.SearchEverywhere.tab(key)}")

    fun result(key: String): Locator = page.locator("#${TestTags.SearchEverywhere.result(key)}")

    fun clickTab(key: String) {
        tab(key).dispatchEvent("click")
    }

    fun clickResult(key: String) {
        result(key).dispatchEvent("click")
    }

    fun typeQuery(text: String) {
        field.dispatchEvent("click")
        page.keyboard().type(text)
    }

    fun pressEscape() {
        page.keyboard().press("Escape")
    }

    fun assertClosed() {
        assertThat(field).hasCount(0)
    }
}
