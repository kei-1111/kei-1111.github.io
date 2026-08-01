package io.github.kei_1111.test.e2e.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import io.github.kei_1111.test.tags.TestTags

/**
 * Profile 画面の主要な操作対象をまとめる Page Object。
 */
class ProfilePage(private val page: Page) {

    fun treeItem(key: String): Locator = page.locator("#${TestTags.Profile.projectTreeItem(key)}")

    fun clickTreeItem(key: String) {
        treeItem(key).dispatchEvent("click")
    }

    fun tab(key: String): Locator = page.locator("#${TestTags.Profile.editorTab(key)}")

    fun clickTab(key: String) {
        tab(key).dispatchEvent("click")
    }

    fun closeTab(key: String) {
        page.locator("#${TestTags.Profile.editorTabClose(key)}").dispatchEvent("click")
    }

    fun clickSearch() {
        page.locator("#${TestTags.Profile.TITLE_BAR_SEARCH}").dispatchEvent("click")
    }

    fun themeToggle(): Locator = page.locator("#${TestTags.Profile.TITLE_BAR_THEME_TOGGLE}")

    /**
     * テーマ状態のスナップショット。テーマは canvas 描画で DOM に色が現れないため、testTag で
     * 特定したトグルの aria-label を状態値として返す。文言は検証に使わない — 呼び出し側は
     * before/after の変化のみを比較する。
     */
    fun themeState(): String = checkNotNull(themeToggle().getAttribute(ARIA_LABEL_ATTRIBUTE))

    fun toggleProjectRail() {
        page.locator("#${TestTags.Profile.TOOL_RAIL_PROJECT}").dispatchEvent("click")
    }

    fun toggleLogcatRail() {
        page.locator("#${TestTags.Profile.TOOL_RAIL_LOGCAT}").dispatchEvent("click")
    }

    fun logcatHide(): Locator = page.locator("#${TestTags.Profile.LOGCAT_HIDE}")

    fun logcatTabClose(): Locator = page.locator("#${TestTags.Profile.LOGCAT_TAB_CLOSE}")

    fun toggleTerminalRail() {
        page.locator("#${TestTags.Profile.TOOL_RAIL_TERMINAL}").dispatchEvent("click")
    }

    fun terminalInput(): Locator = page.locator("#${TestTags.Profile.TERMINAL_INPUT}")

    fun terminalHide(): Locator = page.locator("#${TestTags.Profile.TERMINAL_HIDE}")

    fun terminalTabClose(): Locator = page.locator("#${TestTags.Profile.TERMINAL_TAB_CLOSE}")

    fun viewModeCode() {
        page.locator("#${TestTags.Profile.VIEW_MODE_CODE}").dispatchEvent("click")
    }

    fun viewModeSplit() {
        page.locator("#${TestTags.Profile.VIEW_MODE_SPLIT}").dispatchEvent("click")
    }

    fun viewModePreview() {
        page.locator("#${TestTags.Profile.VIEW_MODE_PREVIEW}").dispatchEvent("click")
    }

    fun licenseRow(key: String): Locator = page.locator("#${TestTags.Profile.licenseRow(key)}")

    fun clickLicenseRow(key: String) {
        licenseRow(key).dispatchEvent("click")
    }

    fun sheetClose(): Locator = page.locator("#${TestTags.Profile.LICENSE_SHEET_CLOSE}")

    fun sheetCloseFooter(): Locator = page.locator("#${TestTags.Profile.LICENSE_SHEET_CLOSE_FOOTER}")

    fun sheetScrim(): Locator = page.locator("#${TestTags.Profile.LICENSE_SHEET_SCRIM}")

    fun previewRetry(): Locator = page.locator("#${TestTags.Profile.PREVIEW_RETRY}")

    companion object {
        /** Web 標準の属性名（"id" と同種の固定語彙であり、UI 文言ではない）。 */
        const val ARIA_LABEL_ATTRIBUTE = "aria-label"
    }
}
