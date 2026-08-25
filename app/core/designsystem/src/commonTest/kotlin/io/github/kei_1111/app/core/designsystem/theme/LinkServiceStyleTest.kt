package io.github.kei_1111.app.core.designsystem.theme

import io.github.kei_1111.shared.model.LinkServiceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LinkServiceStyleTest {

    // 明暗の出し分けは drawable / drawable-dark の修飾子が解決するため、ここでは
    // サービスとアイコン・色の対応だけを固定する（テーマ選択は CMP の責務）。
    @Test
    fun mapsEveryServiceToItsOwnIcon() {
        val icons = LinkServiceType.entries.map { it.icon() }

        assertEquals(LinkServiceType.entries.size, icons.toSet().size)
    }

    @Test
    fun paintsOnlyQiitaInItsBrandColor() {
        assertEquals(KeiDarkColorScheme.brandQiita, LinkServiceType.Qiita.brandColor(KeiDarkColorScheme))
        assertNotEquals(KeiDarkColorScheme.brandQiita, LinkServiceType.GitHub.brandColor(KeiDarkColorScheme))
        LinkServiceType.entries.filterNot { it == LinkServiceType.Qiita }.forEach { service ->
            assertEquals(KeiDarkColorScheme.textPrimary, service.brandColor(KeiDarkColorScheme))
        }
    }

    @Test
    fun followsTheColorSchemeForTheBrandColor() {
        assertEquals(KeiLightColorScheme.textPrimary, LinkServiceType.GitHub.brandColor(KeiLightColorScheme))
        assertEquals(KeiLightColorScheme.brandQiita, LinkServiceType.Qiita.brandColor(KeiLightColorScheme))
    }
}
