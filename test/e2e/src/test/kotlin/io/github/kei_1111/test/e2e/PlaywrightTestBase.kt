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

    @BeforeAll
    fun setUpBrowser() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch()
        PlaywrightAssertions.setDefaultAssertionTimeout(DEFAULT_TIMEOUT_MS)
    }

    @BeforeEach
    fun setUpPage() {
        // 表示言語はブラウザロケール検出（browserLanguageTag）で決まるため、
        // ja に固定して日本語ラベルへの断定を環境非依存にする
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
    }
}
