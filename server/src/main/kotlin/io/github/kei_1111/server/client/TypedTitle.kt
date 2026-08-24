package io.github.kei_1111.server.client

// タイトル規約は #238 で Conventional Commits へ移行したが、既存 Issue/PR は改名しないため
// 旧規約 `[<Type>]: <title>` も引き続き受理する(.claude/rules/git-workflow.md)。
private val TYPED_TITLE_REGEX = Regex("""^([a-z]+(?:\([^()]+\))?!?):\s*(.*)$""")
private val LEGACY_TYPED_TITLE_REGEX = Regex("""^\[(.+?)]:\s*(.*)$""")

internal data class TypedTitle(val type: String?, val title: String)

internal fun parseTypedTitle(raw: String): TypedTitle {
    val match = TYPED_TITLE_REGEX.matchEntire(raw) ?: LEGACY_TYPED_TITLE_REGEX.matchEntire(raw)
    return if (match != null) {
        TypedTitle(match.groupValues[1], match.groupValues[2])
    } else {
        TypedTitle(null, raw)
    }
}
