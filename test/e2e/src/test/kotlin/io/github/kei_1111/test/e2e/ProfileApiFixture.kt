package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route

/**
 * バルーン通知の E2E が使うフィクスチャ。shared/model の Profile と同じ JSON 契約
 * （フィールド名は SerialName に一致させる）。統計フィールドの欠落はサーバーが GitHub に
 * 到達できなかった状態を表す。
 */
internal object ProfileApiFixture {
    fun fulfill(page: Page, hasStatistics: Boolean) {
        page.route("**/api/profile") { route ->
            route.fulfill(
                Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(json(hasStatistics)),
            )
        }
    }

    private fun json(hasStatistics: Boolean): String {
        val statistics = if (hasStatistics) {
            """"followers": 10, "following": 10, "repos": 20, "totalStars": 5,"""
        } else {
            ""
        }
        return """
            {
              "name": { "ja": "ケイ", "en": "Kei" },
              "handle": "kei-1111",
              "location": "Tokyo, Japan",
              "role": "Android Engineer",
              $statistics
              "pinnedRepos": [],
              "languages": [],
              "links": []
            }
        """.trimIndent()
    }
}
