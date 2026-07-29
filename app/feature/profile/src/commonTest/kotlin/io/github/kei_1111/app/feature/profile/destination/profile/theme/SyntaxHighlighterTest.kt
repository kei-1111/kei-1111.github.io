package io.github.kei_1111.app.feature.profile.destination.profile.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class SyntaxHighlighterTest {

    private fun kinds(
        line: String,
        declaredFunctions: Set<String> = emptySet(),
    ): List<Pair<String, TokenKind>> = scanLine(line, declaredFunctions).map { it.text to it.kind }

    @Test
    fun classifiesKeywordAndDeclaredFunctionName() {
        assertEquals(
            listOf("fun" to TokenKind.Keyword, "main" to TokenKind.FunctionName),
            kinds("fun main() {}"),
        )
    }

    @Test
    fun classifiesNamedArgumentBeforeKeyword() {
        assertEquals(
            listOf("Card" to TokenKind.Base, "data" to TokenKind.NamedArg, "x" to TokenKind.Base),
            kinds("Card(data = x)"),
        )
    }

    @Test
    fun keepsSoftKeywordDataAsKeywordInDeclarationAndPlainAsParameterName() {
        assertEquals(
            listOf("data" to TokenKind.Keyword, "class" to TokenKind.Keyword, "A" to TokenKind.Base),
            kinds("data class A"),
        )
        assertEquals(
            listOf("fun" to TokenKind.Keyword, "f" to TokenKind.FunctionName, "data" to TokenKind.Base, "Int" to TokenKind.Base),
            kinds("fun f(data: Int)"),
        )
    }

    @Test
    fun classifiesDeclaredFunctionCallAsComposableCall() {
        assertEquals(
            listOf("TitleBar" to TokenKind.ComposableCall),
            kinds("TitleBar()", declaredFunctions = setOf("TitleBar")),
        )
    }

    @Test
    fun classifiesPascalCaseAfterDotAsEnumEntry() {
        assertEquals(
            listOf("theme" to TokenKind.Base, "Dark" to TokenKind.EnumEntry),
            kinds("theme.Dark"),
        )
    }

    @Test
    fun classifiesFixedPatterns() {
        assertEquals(listOf("// note" to TokenKind.Comment), kinds("// note"))
        assertEquals(listOf("\"hi\"" to TokenKind.StringLit), kinds("\"hi\""))
        assertEquals(listOf("@Composable" to TokenKind.Annotation), kinds("@Composable"))
    }

    @Test
    fun linksSchemelessUrls() {
        assertEquals(
            listOf("github.com/kei-1111" to TokenKind.Link),
            kinds("github.com/kei-1111"),
        )
    }

    @Test
    fun linksUrlWithLeadingHyphenSegment() {
        assertEquals(listOf("-x.com/a" to TokenKind.Link), kinds("-x.com/a"))
    }

    @Test
    fun classifiesNumbersIncludingFloatSuffix() {
        assertEquals(
            listOf("size" to TokenKind.NamedArg, "12.5f" to TokenKind.Number),
            kinds("size = 12.5f"),
        )
    }

    @Test
    fun detectsContiguousJapaneseRanges() {
        assertEquals(listOf(1..2), japaneseRanges("aあいb"))
        assertEquals(emptyList(), japaneseRanges("ascii only"))
    }
}
