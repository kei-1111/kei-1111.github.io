package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WorksSourceCodeTest {

    @Test
    fun roundTripParsesGeneratedCode() {
        val parsed = parseWorksCode(worksCode(baseWorks, KeiLanguage.Ja))

        assertNotNull(parsed)
        assertEquals(2, parsed.size)
        assertEquals("withmo", parsed[0].name)
        assertEquals("Android Launcher App", parsed[0].kind)
        assertEquals("2024–", parsed[0].period)
        assertEquals("ランチャー", parsed[0].description.ja)
        assertEquals(listOf("Kotlin", "Unity as a Library"), parsed[0].tags.map { it.name })
        assertEquals(listOf("Android 側の実装"), parsed[0].roles.map { it.ja })
        assertEquals("site", parsed[1].name)
    }

    @Test
    fun overlayRestoresAssetsAndTagAccents() {
        val parsed = assertNotNull(parseWorksCode(worksCode(baseWorks, KeiLanguage.Ja)))

        val merged = overlayWorksAssets(parsed, baseWorks)

        assertEquals("withmo", merged[0].id)
        assertEquals("images/withmo-icon.webp", merged[0].iconUrl)
        assertEquals(persistentListOf("images/withmo-1.webp"), merged[0].screenshots)
        assertEquals("https://example.com/source", merged[1].sourceUrl)
        assertEquals(listOf(true, false), merged[0].tags.map { it.accent })
    }

    @Test
    fun addedWorkBlockParsesWithoutAssets() {
        val extraBlock = listOf(
            "            Work(",
            "                name = \"new\", kind = \"App\", period = \"2026–\",",
            "                description = \"added\",",
            "                tags = listOf(\"Kotlin\"),",
            "            ),",
        ).joinToString("\n")
        val code = worksCode(baseWorks, KeiLanguage.Ja)
            .replace("        ),\n    )", "$extraBlock\n        ),\n    )")

        val parsed = assertNotNull(parseWorksCode(code))
        val merged = overlayWorksAssets(parsed, baseWorks)

        assertEquals(3, merged.size)
        assertEquals("new", merged[2].name)
        assertNull(merged[2].iconUrl)
        assertEquals(persistentListOf(), merged[2].screenshots)
        // 追加ブロックのタグも既存作品のアクセント指定を引き継ぐ
        assertEquals(listOf(true), merged[2].tags.map { it.accent })
    }

    @Test
    fun removedWorkBlockParses() {
        val parsed = parseWorksCode(worksCode(persistentListOf(baseWorks[0]), KeiLanguage.Ja))

        assertEquals(1, assertNotNull(parsed).size)
    }

    @Test
    fun emptyTagsAndMissingRolesParse() {
        val work = baseWorks[1].copy(tags = persistentListOf(), roles = persistentListOf())

        val parsed = parseWorksCode(worksCode(persistentListOf(work), KeiLanguage.Ja))

        assertNotNull(parsed)
        assertEquals(emptyList(), parsed[0].tags.map { it.name })
        assertEquals(emptyList(), parsed[0].roles)
    }

    @Test
    fun escapedQuotesRoundTrip() {
        val work = baseWorks[0].copy(name = """say "hi" \ ok""")

        val parsed = parseWorksCode(worksCode(persistentListOf(work), KeiLanguage.Ja))

        assertEquals("""say "hi" \ ok""", assertNotNull(parsed)[0].name)
    }

    @Test
    fun returnsNullOnBrokenCode() {
        val code = worksCode(baseWorks, KeiLanguage.Ja).replace("name = \"withmo\"", "name = withmo")

        assertNull(parseWorksCode(code))
    }

    @Test
    fun returnsNullWhenAllWorksAreRemoved() {
        val single = worksCode(persistentListOf(baseWorks[0]), KeiLanguage.Ja)
        val entry = single.substringAfter("works = listOf(\n").substringBefore("        ),\n    )")

        val code = single.replace(entry, "")

        assertNull(parseWorksCode(code))
    }
}

private val baseWorks = persistentListOf(
    Work(
        id = "withmo",
        name = "withmo",
        kind = "Android Launcher App",
        period = "2024–",
        description = LocalizedText(ja = "ランチャー", en = "Launcher"),
        tags = persistentListOf(WorkTag(name = "Kotlin", accent = true), WorkTag(name = "Unity as a Library")),
        roles = persistentListOf(LocalizedText(ja = "Android 側の実装", en = "Android side")),
        iconUrl = "images/withmo-icon.webp",
        screenshots = persistentListOf("images/withmo-1.webp"),
    ),
    Work(
        id = "site",
        name = "site",
        kind = "Website",
        period = "2025–",
        description = LocalizedText(ja = "サイト", en = "Site"),
        tags = persistentListOf(WorkTag(name = "Ktor")),
        sourceUrl = "https://example.com/source",
    ),
)
