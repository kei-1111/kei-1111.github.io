package io.github.kei_1111.app.feature.profile.destination.profile.model

/** ProfileSourceCode / WorksSourceCode のパーサが共有する行カーソル。 */
internal class LineCursor(private val lines: List<String>) {
    private var index = 0

    fun peek(): String? = lines.getOrNull(index)

    fun take(): String? = lines.getOrNull(index++)

    fun expect(expected: String): Boolean = take() == expected

    fun isAtEnd(): Boolean = index == lines.size
}
