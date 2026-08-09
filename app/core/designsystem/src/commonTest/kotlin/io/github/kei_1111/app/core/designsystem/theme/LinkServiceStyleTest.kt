package io.github.kei_1111.app.core.designsystem.theme

import io.github.kei_1111.shared.model.LinkServiceType
import kei_1111.app.core.designsystem.generated.resources.Res
import kei_1111.app.core.designsystem.generated.resources.ic_link_note
import kei_1111.app.core.designsystem.generated.resources.ic_link_note_light
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LinkServiceStyleTest {

    @Test
    fun switchesTheNoteIconByTheme() {
        assertEquals(Res.drawable.ic_link_note, LinkServiceType.Note.icon(KeiDarkColorScheme))
        assertEquals(Res.drawable.ic_link_note_light, LinkServiceType.Note.icon(KeiLightColorScheme))
    }

    @Test
    fun keepsThemeIndependentIconsForOtherServices() {
        LinkServiceType.entries.filterNot { it == LinkServiceType.Note }.forEach { service ->
            assertEquals(service.icon(KeiDarkColorScheme), service.icon(KeiLightColorScheme))
        }
    }

    @Test
    fun paintsOnlyQiitaInItsBrandColor() {
        assertEquals(KeiDarkColorScheme.brandQiita, LinkServiceType.Qiita.brandColor(KeiDarkColorScheme))
        assertNotEquals(KeiDarkColorScheme.brandQiita, LinkServiceType.GitHub.brandColor(KeiDarkColorScheme))
        LinkServiceType.entries.filterNot { it == LinkServiceType.Qiita }.forEach { service ->
            assertEquals(KeiDarkColorScheme.textPrimary, service.brandColor(KeiDarkColorScheme))
        }
    }
}
