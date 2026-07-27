package io.github.kei_1111.app.feature.profile.destination.profile.model

/** 文字列を欠損なく往復させるため、生成と解析で共有する本文パターン。 */
internal const val KOTLIN_STRING_BODY_PATTERN: String = """(?:[^"\\]|\\.)*"""

/** 解析との往復で値を失わないよう、Kotlin 文字列の本文へエスケープする。 */
internal fun escapeKotlinString(value: String): String = buildString {
    value.forEach { char ->
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '$' -> append("\\\$")
            else -> append(char)
        }
    }
}

/** 共有する生成規則に沿って復元し、未対応のエスケープは解析エラーとする。 */
internal fun unescapeKotlinString(literal: String): String? {
    val value = StringBuilder(literal.length)
    var index = 0
    while (index < literal.length) {
        val char = literal[index]
        if (char != '\\') {
            value.append(char)
            index++
            continue
        }
        val unescaped = when (literal.getOrNull(index + 1)) {
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'b' -> '\b'
            '"' -> '"'
            '\'' -> '\''
            '\\' -> '\\'
            '$' -> '$'
            else -> return null
        }
        value.append(unescaped)
        index += 2
    }
    return value.toString()
}
