package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.tags.TestTags
import org.junit.jupiter.api.Test

/**
 * URL / ブラウザのセットアップは基底クラス側で共通化する。
 */
class ThemeToggleE2eTest : PlaywrightTestBase() {

    @Test
    fun clickingThemeToggleFlipsTheme() {
        // Splash 通過は PlaywrightTestBase 側で完了済み。testTag は DOM の id になる。
        val toggle = page.locator("#${TestTags.TITLE_BAR_THEME_TOGGLE}")

        // 初期はダークテーマ → ラベルは「ライトモードに切り替え」
        assertThat(page.getByLabel("ライトモードに切り替え")).isVisible()

        // canvas がポインタを奪うので、スクリーンリーダーと同じく合成 click をディスパッチする
        toggle.dispatchEvent("click")

        assertThat(page.getByLabel("ダークモードに切り替え")).isVisible()
        assertThat(page.getByLabel("ライトモードに切り替え")).hasCount(0)

        // もう一度押すと戻る
        toggle.dispatchEvent("click")
        assertThat(page.getByLabel("ライトモードに切り替え")).isVisible()
    }
}
