package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route

/**
 * GitHub プレビューカードとバルーン通知の E2E が使うフィクスチャ。shared/model の Profile と同じ
 * JSON 契約（フィールド名は SerialName に一致させる）。統計フィールドの欠落はサーバーが GitHub に
 * 到達できなかった状態を表す。アバター画像は取得させない（未取得でも既定画像が出る設計で、
 * カードの描画には影響しない）。
 */
internal object ProfileApiFixture {
    fun fulfill(page: Page, hasStatistics: Boolean = true) {
        page.route("**/api/profile") { route ->
            route.fulfill(
                Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(json(hasStatistics)),
            )
        }
        page.route("**/images/profile/**") { it.abort() }
    }

    private fun json(hasStatistics: Boolean): String {
        val statistics = if (hasStatistics) {
            """"followers": 16, "following": 21, "repos": 33, "totalStars": 41,"""
        } else {
            ""
        }
        return """
            {
              "name": { "ja": "フィクスチャ", "en": "Fixture" },
              "handle": "@fixture",
              "location": "Japan",
              "role": "Android developer",
              $statistics
              "pinnedRepos": [
                {
                  "name": "fixture-repo",
                  "description": { "ja": "フィクスチャ", "en": "Fixture repository" },
                  "url": "https://github.com/kei-1111/fixture-repo",
                  "language": "Kotlin"
                }
              ],
              "languages": [
                { "language": "Kotlin", "share": 1.0 }
              ],
              "links": [
                { "type": "GitHub", "name": "GitHub", "url": "https://github.com/kei-1111" }
              ]
            }
        """.trimIndent()
    }
}
