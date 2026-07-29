package io.github.kei_1111.test.e2e.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Assertions.fail

/**
 * Search Everywhere の検索と絞り込みをまとめる Page Object。
 */
class SearchEverywherePage(private val page: Page) {

    val field: Locator
        get() = page.locator("#${TestTags.SearchEverywhere.FIELD}")

    fun tab(key: String): Locator = page.locator("#${TestTags.SearchEverywhere.tab(key)}")

    fun result(key: String): Locator = page.locator("#${TestTags.SearchEverywhere.result(key)}")

    fun typeQuery(text: String) {
        field.dispatchEvent("click")
        page.keyboard().type(text)
    }

    fun pressEscape() {
        page.keyboard().press("Escape")
    }

    /**
     * [closeAction] がダイアログを閉じることを、パネルヘッダー帯（検索フィールド上方の
     * タブチップ行）の画素変化で断定する。ダイアログを閉じると a11y ミラー全体が破棄された
     * まま再構築されない（CMP wasm の制約）ため、閉じた後の状態は DOM から断定できない。
     * 帯はキャレット点滅を含まず開いている間は描画が静的なので、変化 = ダイアログ消失とみなせる。
     */
    fun assertClosesDialog(closeAction: () -> Unit) {
        val box = checkNotNull(field.boundingBox()) { "検索フィールドが未レイアウトのため帯の位置を決められない" }
        val clip = Page.ScreenshotOptions()
            .setClip(box.x, box.y - HEADER_STRIP_OFFSET, box.width, HEADER_STRIP_HEIGHT)
        val openShot = stableShot(clip)

        closeAction()

        val deadline = System.currentTimeMillis() + CLOSE_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            if (!page.screenshot(clip).contentEquals(openShot)) return
            Thread.sleep(POLL_INTERVAL_MS)
        }
        fail<Unit>("Search Everywhere ダイアログが閉じたことを画素変化で確認できなかった")
    }

    /** 入場アニメーション中の基準取得を避けるため、2 フレーム連続で一致するまで待ってから返す。 */
    private fun stableShot(clip: Page.ScreenshotOptions): ByteArray {
        var previous = page.screenshot(clip)
        val deadline = System.currentTimeMillis() + CLOSE_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(POLL_INTERVAL_MS)
            val current = page.screenshot(clip)
            if (current.contentEquals(previous)) return current
            previous = current
        }
        return previous
    }

    private companion object {
        const val HEADER_STRIP_OFFSET = 40.0
        const val HEADER_STRIP_HEIGHT = 26.0
        const val CLOSE_TIMEOUT_MS = 10_000L
        const val POLL_INTERVAL_MS = 300L
    }
}
