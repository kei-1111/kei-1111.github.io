package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions
import io.github.kei_1111.test.e2e.page.SplashPage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance

/**
 * baseURL は Gradle の `-PbaseUrl=...` で渡す。未指定時は Gradle がタスクごと SKIP するため
 * （build.gradle.kts の `onlyIf`）、ローカル配信へのフォールバックが効くのは IDE などから
 * JUnit を直接実行した場合のみ。
 *
 * 要素のクリックは `.click()` ではなく `dispatchEvent("click")` を使うこと。canvas が
 * a11y ミラーの上に重なって実ポインタイベントを奪うため、合成 DOM click だけが
 * CMP のリスナーに届く。
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class PlaywrightTestBase {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

    protected lateinit var page: Page

    protected open val viewport: Pair<Int, Int>? = null

    /** ナビゲーション前にページを設定するフック（ネットワークの route 差し込みなど）。 */
    protected open fun configurePage(page: Page) {}

    protected fun bootDeskProperty(): String =
        page.evaluate("() => document.documentElement.style.getPropertyValue('--boot-desk')") as String

    protected fun bodyBackgroundColor(): String =
        page.evaluate("() => getComputedStyle(document.body).backgroundColor") as String

    protected fun cssRgbOf(hexColor: String): String {
        val red = hexColor.substring(1, 3).toInt(HEX_RADIX)
        val green = hexColor.substring(3, 5).toInt(HEX_RADIX)
        val blue = hexColor.substring(5, 7).toInt(HEX_RADIX)
        return "rgb($red, $green, $blue)"
    }

    @BeforeAll
    fun setUpBrowser() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch()
        PlaywrightAssertions.setDefaultAssertionTimeout(DEFAULT_TIMEOUT_MS)
    }

    @BeforeEach
    fun setUpPage() {
        // 表示言語はブラウザロケール検出（browserLanguageTag）で決まるため、
        // ja に固定して初期言語を環境非依存にする（文言への断定は行わず testTag のみで特定する）
        val contextOptions = Browser.NewContextOptions()
            .setBaseURL(BASE_URL)
            .setLocale("ja-JP")
        viewport?.let { (width, height) ->
            contextOptions.setViewportSize(width, height)
        }
        context = browser.newContext(contextOptions)
        page = context.newPage()
        page.setDefaultTimeout(DEFAULT_TIMEOUT_MS)
        configurePage(page)
        page.navigate("/")
        SplashPage(page).waitUntilProfileAppears()
    }

    @AfterEach
    fun tearDownPage() {
        // route ハンドラを残したまま閉じると、close() の往復中に届いた route イベントが
        // 閉鎖中のターゲットへ再送を試み、TargetClosedError が teardown から飛ぶ
        if (::page.isInitialized) page.unrouteAll()
        if (::context.isInitialized) context.close()
    }

    @AfterAll
    fun tearDownBrowser() {
        if (::browser.isInitialized) browser.close()
        if (::playwright.isInitialized) playwright.close()
    }

    private companion object {
        val BASE_URL: String = System.getProperty("baseUrl") ?: "http://localhost:8083"
        const val DEFAULT_TIMEOUT_MS = 40_000.0
        const val HEX_RADIX = 16
    }
}
