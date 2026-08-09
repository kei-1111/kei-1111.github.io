package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route

/**
 * GitHub プレビューカードの E2E が使うフィクスチャ。shared/model の GitHubProfile と同じ JSON 契約。
 * アバター画像は取得させない (未取得でも既定画像が出る設計で、カードの描画には影響しない)。
 */
internal object ProfileApiFixture {
    val JSON = """
        {
          "name": { "ja": "フィクスチャ", "en": "Fixture" },
          "handle": "@fixture",
          "location": "Japan",
          "role": "Android developer",
          "followers": 16,
          "following": 21,
          "repos": 33,
          "totalStars": 41,
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

    fun fulfill(page: Page) {
        page.route("**/api/profile") { route ->
            route.fulfill(
                Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(JSON),
            )
        }
        page.route("**/images/profile/**") { it.abort() }
    }
}
