package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route

/**
 * Git ツールウィンドウの E2E が使うフィクスチャ。shared/model の GitHubChangelog と同じ
 * JSON 契約（フィールド名は SerialName に一致させる）。
 */
internal object ChangelogApiFixture {
    val JSON = """
        {
          "pullRequests": [
            {
              "number": 204,
              "title": "Introduce KeiAsyncImage",
              "url": "https://github.com/kei-1111/kei-1111.github.io/pull/204",
              "headRefName": "refactor/#201",
              "mergedAt": "2026-08-09T06:02:11Z",
              "type": "Refactor",
              "author": "kei-1111"
            },
            {
              "number": 203,
              "title": "Fetch pinned repositories live",
              "url": "https://github.com/kei-1111/kei-1111.github.io/pull/203",
              "headRefName": "feature/#195",
              "mergedAt": "2026-08-09T05:58:44Z",
              "type": "Feature",
              "author": "kei-1111"
            }
          ]
        }
    """.trimIndent()

    fun fulfill(page: Page) {
        page.route("**/api/changelog") { route ->
            route.fulfill(
                Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(JSON),
            )
        }
    }

    fun fulfillUnavailable(page: Page) {
        page.route("**/api/changelog") { route ->
            route.fulfill(Route.FulfillOptions().setStatus(503))
        }
    }
}
