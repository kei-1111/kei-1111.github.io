package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Work
import io.github.kei_1111.shared.model.WorkTag
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private val quotedListBody =
    """(?:"$KOTLIN_STRING_BODY_PATTERN"(?:, "$KOTLIN_STRING_BODY_PATTERN")*)?"""
private val workHeaderRegex = Regex(
    """name = "($KOTLIN_STRING_BODY_PATTERN)", kind = "($KOTLIN_STRING_BODY_PATTERN)", period = "($KOTLIN_STRING_BODY_PATTERN)",""",
)
private val workDescriptionRegex = Regex("""description = "($KOTLIN_STRING_BODY_PATTERN)",""")
private val workTagsRegex = Regex("""tags = listOf\(($quotedListBody)\),""")
private val workRolesRegex = Regex("""roles = listOf\(($quotedListBody)\),""")
private val quotedItemRegex = Regex("\"($KOTLIN_STRING_BODY_PATTERN)\"")

private const val PERIOD_GROUP = 3

private val worksHead = listOf(
    "package io.github.kei_1111.ui.works",
    "import ...",
    "@Composable",
    "internal fun WorksScreen(",
    "works: List<Work>,",
    "modifier: Modifier = Modifier,",
    ") { ... }",
    "@Preview",
    "@Composable",
    "private fun WorksScreenPreview() {",
    "WorksScreen(",
    "works = listOf(",
)

private val worksTail = listOf(
    "),",
    ")",
    "}",
)

private fun workEntryCode(work: Work, language: KeiLanguage): String {
    val name = escapeKotlinString(work.name)
    val kind = escapeKotlinString(work.kind)
    val period = escapeKotlinString(work.period)
    val description = escapeKotlinString(work.description.forLanguage(language))
    val tags = work.tags.joinToString(", ") { "\"${escapeKotlinString(it.name)}\"" }
    val roles = work.roles.joinToString(", ") { "\"${escapeKotlinString(it.forLanguage(language))}\"" }
    return listOfNotNull(
        "|            Work(",
        "|                name = \"$name\", kind = \"$kind\", period = \"$period\",",
        "|                description = \"$description\",",
        "|                tags = listOf($tags),",
        "|                roles = listOf($roles),".takeIf { work.roles.isNotEmpty() },
        "|            ),",
    ).joinToString("\n")
}

internal fun worksCode(works: ImmutableList<Work>, language: KeiLanguage): String = """
    |package io.github.kei_1111.ui.works
    |
    |import ...
    |
    |@Composable
    |internal fun WorksScreen(
    |    works: List<Work>,
    |    modifier: Modifier = Modifier,
    |) { ... }
    |
    |@Preview
    |@Composable
    |private fun WorksScreenPreview() {
    |    WorksScreen(
    |        works = listOf(
    ${works.joinToString("\n") { workEntryCode(it, language) }}
    |        ),
    |    )
    |}
""".trimMargin()

/**
 * [worksCode] が生成する形の編集結果を解析する。逸脱と作品0件は null（プレビューは最終成功データを保持）。
 * スクショ等のコード面に出ないフィールドは空のまま返す — 復元は [overlayWorksAssets]。
 */
@Suppress("ReturnCount")
internal fun parseWorksCode(code: String): List<Work>? {
    val cursor = LineCursor(code.split('\n').map(String::trim).filter(String::isNotEmpty))
    if (worksHead.any { !cursor.expect(it) }) return null
    val works = parseWorkEntries(cursor) ?: return null
    if (worksTail.any { !cursor.expect(it) } || !cursor.isAtEnd()) return null
    return works.takeIf { it.isNotEmpty() }
}

@Suppress("ReturnCount", "CyclomaticComplexMethod")
private fun parseWorkEntries(cursor: LineCursor): List<Work>? {
    val works = mutableListOf<Work>()
    while (cursor.peek() == "Work(") {
        cursor.take()
        val header = cursor.take()?.let(workHeaderRegex::matchEntire) ?: return null
        val description = cursor.take()?.let(workDescriptionRegex::matchEntire) ?: return null
        val tags = cursor.take()?.let(workTagsRegex::matchEntire) ?: return null
        val roles = cursor.peek()?.let(workRolesRegex::matchEntire)?.also { cursor.take() }
        if (!cursor.expect("),")) return null
        val name = unescapeKotlinString(header.groupValues[1]) ?: return null
        val kind = unescapeKotlinString(header.groupValues[2]) ?: return null
        val period = unescapeKotlinString(header.groupValues[PERIOD_GROUP]) ?: return null
        val descriptionText = unescapeKotlinString(description.groupValues[1]) ?: return null
        val tagNames = parseQuotedItems(tags.groupValues[1]) ?: return null
        val roleTexts = roles?.let { parseQuotedItems(it.groupValues[1]) ?: return null }.orEmpty()
        works += Work(
            id = "",
            name = name,
            kind = kind,
            period = period,
            description = LocalizedText(ja = descriptionText, en = descriptionText),
            tags = tagNames.map { WorkTag(name = it) }.toImmutableList(),
            roles = roleTexts.map { LocalizedText(ja = it, en = it) }.toImmutableList(),
        )
    }
    return works
}

private fun parseQuotedItems(listBody: String): List<String>? {
    val items = mutableListOf<String>()
    for (match in quotedItemRegex.findAll(listBody)) {
        items += unescapeKotlinString(match.groupValues[1]) ?: return null
    }
    return items
}

/**
 * 解析結果へ、コード面に出ないフィールド（id・画像・リンク・タグのアクセント）を元データから復元する。
 * 作品は位置対応、タグのアクセントは全作品横断の名前一致。対応先のない追加ブロックは素の状態のまま。
 */
internal fun overlayWorksAssets(parsed: List<Work>, base: List<Work>): ImmutableList<Work> {
    val accentByName = base.flatMap { it.tags }.associate { it.name to it.accent }
    return parsed.mapIndexed { index, work ->
        val origin = base.getOrNull(index)
        work.copy(
            id = origin?.id ?: "edited-$index",
            tags = work.tags.map { it.copy(accent = accentByName[it.name] ?: false) }.toImmutableList(),
            iconUrl = origin?.iconUrl,
            screenshots = origin?.screenshots ?: persistentListOf(),
            storeUrl = origin?.storeUrl,
            sourceUrl = origin?.sourceUrl,
        )
    }.toImmutableList()
}
