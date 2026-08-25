package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route

/**
 * README エディタ / プレビューの E2E が共有するフィクスチャ。shared/model の Readme と同じ JSON 契約
 * (sealed 判別子は `type`)。ライブ API に依存せず、編集前のプレビュー内容を決定的にする。
 */
internal object ReadmeApiFixture {
    val JSON = """
        {
          "ja": [
            { "type": "heading", "level": 1, "inlines": [{ "type": "plain_text", "text": "Fixture README" }] },
            { "type": "paragraph", "inlines": [{ "type": "plain_text", "text": "本文の初期値" }] }
          ],
          "en": [
            { "type": "heading", "level": 1, "inlines": [{ "type": "plain_text", "text": "Fixture README" }] },
            { "type": "paragraph", "inlines": [{ "type": "plain_text", "text": "Initial body" }] }
          ]
        }
    """.trimIndent()

    fun fulfill(page: Page) {
        page.route("**/api/readme") { route ->
            route.fulfill(
                Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(JSON),
            )
        }
    }
}
