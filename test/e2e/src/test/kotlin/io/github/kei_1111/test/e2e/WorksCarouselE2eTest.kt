package io.github.kei_1111.test.e2e

import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * Works プレビューカードのヘッダーの ‹ / › は選択中の作品を切り替える。位置表示は
 * testTag のみで特定し、文言は固定せず before/after の変化だけを見る。
 */
class WorksCarouselE2eTest : PlaywrightTestBase() {

    @Test
    fun clickingNextCyclesSelectedWork() {
        val profile = ProfilePage(page)

        profile.clickTreeItem("works")

        val initial = profile.worksPositionText()
        profile.clickWorksNext()
        profile.assertWorksPositionChangedFrom(initial)
    }
}
