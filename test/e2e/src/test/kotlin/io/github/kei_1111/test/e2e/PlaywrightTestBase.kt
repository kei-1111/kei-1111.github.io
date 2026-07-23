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
 * Playwright のライフサイクルと接続先を共通化する基底クラス。
 *
 * baseURL は Gradle の `-PbaseUrl=...` で渡し、未指定時はローカル配信を使用する。
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class PlaywrightTestBase {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

    protected lateinit var page: Page

    @BeforeAll
    fun setUpBrowser() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch()
        PlaywrightAssertions.setDefaultAssertionTimeout(DEFAULT_TIMEOUT_MS)
    }

    @BeforeEach
    fun setUpPage() {
        context = browser.newContext(Browser.NewContextOptions().setBaseURL(BASE_URL))
        page = context.newPage()
        page.setDefaultTimeout(DEFAULT_TIMEOUT_MS)
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
