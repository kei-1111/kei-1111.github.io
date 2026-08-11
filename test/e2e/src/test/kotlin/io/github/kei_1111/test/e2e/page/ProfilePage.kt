package io.github.kei_1111.test.e2e.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import java.util.regex.Pattern

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

    fun browserThemeColor(): Locator = page.locator("meta[name=theme-color]")

    fun browserThemeColorValue(): String {
        assertThat(browserThemeColor()).hasAttribute(CONTENT_ATTRIBUTE, Pattern.compile(".+"))
        return checkNotNull(browserThemeColor().getAttribute(CONTENT_ATTRIBUTE))
    }

    fun assertBrowserThemeColorChangedFrom(before: String) {
        val changed = Pattern.compile("^(?!${Pattern.quote(before)}$).+$")
        assertThat(browserThemeColor()).hasAttribute(CONTENT_ATTRIBUTE, changed)
    }

    /**
     * テーマ状態のスナップショット。テーマは canvas 描画で DOM に色が現れないため、testTag で
     * 特定したトグルの aria-label を状態値として返す。文言は検証に使わない — 呼び出し側は
     * before/after の変化のみを比較する。マウント直後はミラーの aria-label が一瞬空になるため、
     * 非空になるのを待ってから読む。
     */
    fun themeState(): String {
        assertThat(themeToggle()).hasAttribute(ARIA_LABEL_ATTRIBUTE, Pattern.compile(".+"))
        return checkNotNull(themeToggle().getAttribute(ARIA_LABEL_ATTRIBUTE))
    }

    /**
     * テーマ状態が [before] から変化したことを検証する。ミラー更新中に aria-label が一瞬空に
     * なっても早期成功しないよう、「非空かつ before 以外」を単一の auto-wait 正規表現で待つ。
     */
    fun assertThemeStateChangedFrom(before: String) {
        val changed = Pattern.compile("^(?!${Pattern.quote(before)}$).+$")
        assertThat(themeToggle()).hasAttribute(ARIA_LABEL_ATTRIBUTE, changed)
    }

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

    fun usagePage(): Locator = page.locator("#${TestTags.Profile.EDITOR_USAGE_PAGE}")

    fun licenseRow(key: String): Locator = page.locator("#${TestTags.Profile.licenseRow(key)}")

    fun clickLicenseRow(key: String) {
        licenseRow(key).dispatchEvent("click")
    }

    fun sheetClose(): Locator = page.locator("#${TestTags.Profile.LICENSE_SHEET_CLOSE}")

    fun sheetCloseFooter(): Locator = page.locator("#${TestTags.Profile.LICENSE_SHEET_CLOSE_FOOTER}")

    fun sheetScrim(): Locator = page.locator("#${TestTags.Profile.LICENSE_SHEET_SCRIM}")

    fun previewRetry(): Locator = page.locator("#${TestTags.Profile.PREVIEW_RETRY}")

    fun worksPosition(): Locator = page.locator("#${TestTags.Profile.WORKS_POSITION}")

    fun worksNext(): Locator = page.locator("#${TestTags.Profile.WORKS_NEXT}")

    fun worksPrev(): Locator = page.locator("#${TestTags.Profile.WORKS_PREV}")

    fun clickWorksNext() {
        worksNext().dispatchEvent("click")
    }

    fun clickWorksPrev() {
        worksPrev().dispatchEvent("click")
    }

    /**
     * 作品カルーセルの位置表示（"n / total"）のスナップショット。[themeState] と同じ理由で
     * 非空になるのを待ってから読む — マウント直後はミラーのテキストが一瞬空になる。
     */
    fun worksPositionText(): String {
        assertThat(worksPosition()).hasText(Pattern.compile(".+"))
        return checkNotNull(worksPosition().textContent())
    }

    /**
     * 作品カルーセルの位置表示が [before] から変化したことを検証する。文言は固定せず、
     * before/after の変化のみを見る（testTag-only + change-observation policy）。
     */
    fun assertWorksPositionChangedFrom(before: String) {
        val changed = Pattern.compile("^(?!${Pattern.quote(before)}$).+$")
        assertThat(worksPosition()).hasText(changed)
    }

    fun worksDetail(): Locator = page.locator("#${TestTags.Profile.WORKS_DETAIL}")

    fun clickWorksDetail() {
        worksDetail().dispatchEvent("click")
    }

    fun worksSheetClose(): Locator = page.locator("#${TestTags.Profile.WORKS_SHEET_CLOSE}")

    fun worksSheetScrim(): Locator = page.locator("#${TestTags.Profile.WORKS_SHEET_SCRIM}")

    companion object {
        /** Web 標準の属性名（"id" と同種の固定語彙であり、UI 文言ではない）。 */
        const val ARIA_LABEL_ATTRIBUTE = "aria-label"
        private const val CONTENT_ATTRIBUTE = "content"
    }
}
