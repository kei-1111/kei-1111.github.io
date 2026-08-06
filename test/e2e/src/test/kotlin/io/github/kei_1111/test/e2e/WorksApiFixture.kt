package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route

/**
 * Works カード / シートの E2E が共有するフィクスチャ。shared/model の Work と同じ JSON 契約
 * (フィールド名は SerialName に一致させる)。スクショ画像は取得させない
 * (失敗時はプレースホルダ面が出る設計で、カード/シートの操作には影響しない)。
 */
internal object WorksApiFixture {
    val JSON = """
        [
          {
            "id": "fixture-1",
            "name": "Fixture One",
            "kind": "Sample App",
            "period": "2024–",
            "description": { "ja": "テスト用の作品1", "en": "Fixture work one" },
            "tags": [
              { "name": "Kotlin", "accent": true },
              { "name": "Compose" }
            ],
            "roles": [
              { "ja": "設計・実装", "en": "Design & implementation" }
            ],
            "screenshots": [
              "https://kei-1111.github.io/images/works/fixture-1-1.webp",
              "https://kei-1111.github.io/images/works/fixture-1-2.webp"
            ],
            "storeUrl": "https://example.com/store"
          },
          {
            "id": "fixture-2",
            "name": "Fixture Two",
            "kind": "Sample Site",
            "period": "2025–",
            "description": { "ja": "テスト用の作品2", "en": "Fixture work two" },
            "tags": [
              { "name": "Wasm", "accent": true }
            ],
            "screenshots": [
              "https://kei-1111.github.io/images/works/fixture-2-1.webp"
            ],
            "sourceUrl": "https://example.com/source"
          }
        ]
    """.trimIndent()

    fun fulfill(page: Page) {
        page.route("**/api/works") { route ->
            route.fulfill(
                Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(JSON),
            )
        }
        page.route("**/images/works/**") { it.abort() }
    }
}
