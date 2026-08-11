package io.github.kei_1111.test.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route

/**
 * バルーン通知の E2E が使うフィクスチャ。shared/model の Profile と同じ JSON 契約
 * （フィールド名は SerialName に一致させる）。`isFallback` はサーバーが GitHub 取得に失敗して
 * ビルトインの静的プロフィールを配信した状態を表す。
 */
internal object ProfileApiFixture {
    fun fulfill(page: Page, isFallback: Boolean) {
        page.route("**/api/profile") { route ->
            route.fulfill(
                Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(json(isFallback)),
            )
        }
    }

    private fun json(isFallback: Boolean) = """
        {
          "name": { "ja": "ケイ", "en": "Kei" },
          "handle": "kei-1111",
          "location": "Tokyo, Japan",
          "role": "Android Engineer",
          "followers": 10,
          "following": 10,
          "repos": 20,
          "totalStars": 5,
          "pinnedRepos": [],
          "languages": [],
          "links": [],
          "isFallback": $isFallback
        }
    """.trimIndent()
}
