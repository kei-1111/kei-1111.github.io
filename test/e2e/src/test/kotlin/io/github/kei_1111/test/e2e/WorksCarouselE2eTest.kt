package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * Works プレビューカードのヘッダーの ‹ / › は選択中の作品を切り替える。位置表示は
 * testTag のみで特定し、文言は固定せず before/after の変化だけを見る。
 */
class WorksCarouselE2eTest : PlaywrightTestBase() {

    // 本番 API の到達可否に依存させず、作品一覧を決定的なフィクスチャで返す。
    // スクショ画像は取得させない（失敗時はプレースホルダ面が出る設計で、カルーセル操作には影響しない）
    override fun configurePage(page: Page) {
        page.route("**/api/works") { route ->
            route.fulfill(
                Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(WORKS_FIXTURE_JSON),
            )
        }
        page.route("**/images/works/**") { it.abort() }
    }

    @Test
    fun clickingNextCyclesSelectedWork() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("works")

        val initial = profile.worksPositionText()
        profile.clickWorksNext()
        profile.assertWorksPositionChangedFrom(initial)
    }

    companion object {
        // shared/model の Work と同じ JSON 契約（フィールド名は SerialName に一致させる）
        private val WORKS_FIXTURE_JSON = """
            [
              {
                "id": "fixture-1",
                "name": "Fixture One",
                "stack": "Kotlin",
                "description": { "ja": "テスト用の作品1", "en": "Fixture work one" },
                "tags": ["Compose"],
                "screenshots": [
                  "https://kei-1111.github.io/images/works/fixture-1-1.webp",
                  "https://kei-1111.github.io/images/works/fixture-1-2.webp"
                ],
                "storeUrl": "https://example.com/store"
              },
              {
                "id": "fixture-2",
                "name": "Fixture Two",
                "stack": "Kotlin",
                "description": { "ja": "テスト用の作品2", "en": "Fixture work two" },
                "tags": ["Wasm"],
                "screenshots": [
                  "https://kei-1111.github.io/images/works/fixture-2-1.webp"
                ],
                "sourceUrl": "https://example.com/source"
              }
            ]
        """.trimIndent()
    }
}
