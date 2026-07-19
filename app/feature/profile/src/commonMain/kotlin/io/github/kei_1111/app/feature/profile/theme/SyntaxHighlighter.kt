package io.github.kei_1111.app.feature.profile.theme

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme

/**
 * 文字列を与えるだけで IDE 風シンタックスハイライト付きの行リストを生成する簡易ハイライタ。
 * 色は呼び出し側から渡される [KeiColorScheme] の syntax* トークンに従う
 * （ダーク = Islands Dark 実測値 / ライト = IntelliJ Light 既定スキーム）。
 *
 * Kotlin ([highlightKotlin]):
 * - キーワード / アノテーション / 文字列 / 数値 / コメント / 名前付き引数を色分けする
 * - `fun` で宣言された関数名は [KeiColorScheme.syntaxFunction]、その関数の呼び出しは
 *   [KeiColorScheme.syntaxComposableCall]
 * - `.` に続く PascalCase は enum エントリ（[KeiColorScheme.syntaxEnumEntry]・イタリック）
 * - 型参照・コンストラクタ呼び出し・通常の関数呼び出しは [KeiColorScheme.textCode] のままプレーン
 * - `github.com/xxx` のようなスキーム無し URL は自動で https:// リンクになり、ホバーで下線が付く
 */

private enum class TokenKind {
    Keyword,
    Annotation,
    FunctionName,
    ComposableCall,
    EnumEntry,
    StringLit,
    Number,
    NamedArg,
    Comment,
    Link,
    Base,
}

private data class CodeToken(
    val start: Int,
    val text: String,
    val kind: TokenKind,
)

private val kotlinKeywords = setOf(
    "package", "import", "internal", "private", "public", "protected",
    "fun", "val", "var", "object", "class", "interface", "data",
    "return", "when", "if", "else", "true", "false", "null",
)

private val commentRegex = Regex("//.*")
private val stringRegex = Regex("\"[^\"]*\"")
private val annotationRegex = Regex("@\\w+")
private val urlRegex = Regex("""[\w-]+(?:\.[\w-]+)*\.(?:com|io|dev|org)/[\w\-./]+""")
private val wordRegex = Regex("[A-Za-z_][A-Za-z0-9_]*")
private val numberRegex = Regex("""\d+(?:\.\d+)?f?""")
private val funDeclRegex = Regex("""\bfun\s+(\w+)""")

private val fixedPatterns = listOf(
    TokenKind.Comment to commentRegex,
    TokenKind.StringLit to stringRegex,
    TokenKind.Annotation to annotationRegex,
    TokenKind.Link to urlRegex,
)

/**
 * Kotlin コードをハイライトし、行ごとの AnnotatedString を返す。
 * [japaneseFontFamily] は日本語区間へ明示適用するファミリー（[withJapaneseFont] を参照）。
 * [colors] は syntax* トークンの参照元。グローバル状態を読まない純粋関数にするため、呼び出し側
 * （`KeiTheme.colors`）から明示的に渡す。
 */
internal fun highlightKotlin(
    code: String,
    japaneseFontFamily: FontFamily,
    colors: KeiColorScheme,
): List<AnnotatedString> {
    val declaredFunctions = funDeclRegex.findAll(code).map { it.groupValues[1] }.toSet()
    return code.lines().map { line ->
        renderLine(line, scanLine(line, declaredFunctions), colors).withJapaneseFont(japaneseFontFamily)
    }
}

/** 編集可能フィールド用。トークンへ [TextFieldBuffer.addStyle] でスタイルを重ねる（リンク注釈は不可、色のみ）。 */
internal fun highlightBuffer(
    buffer: TextFieldBuffer,
    japaneseFontFamily: FontFamily,
    colors: KeiColorScheme,
) {
    val text = buffer.toString()
    val declaredFunctions = funDeclRegex.findAll(text).map { it.groupValues[1] }.toSet()
    var offset = 0
    // lines() は CRLF を1区切りに畳むため offset の +1 加算とずれる。split('\n') で区切りを1文字に固定する
    text.split('\n').forEach { line ->
        scanLine(line, declaredFunctions).forEach { token ->
            if (token.kind != TokenKind.Base) {
                val start = offset + token.start
                buffer.addStyle(styleOf(token.kind, colors), start, start + token.text.length)
            }
        }
        offset += line.length + 1
    }
    japaneseRanges(text).forEach { range ->
        buffer.addStyle(SpanStyle(fontFamily = japaneseFontFamily), range.first, range.last + 1)
    }
}

// 日本語として扱う Unicode ブロック（CJK 記号・かな・漢字・全角形）
@Suppress("MagicNumber")
private val japaneseCharRanges = listOf(
    0x3000..0x303F, // CJK の記号及び句読点
    0x3040..0x309F, // ひらがな
    0x30A0..0x30FF, // カタカナ
    0x4E00..0x9FFF, // CJK 統合漢字
    0xFF00..0xFFEF, // 半角・全角形
)

private fun isJapanese(char: Char): Boolean = japaneseCharRanges.any { char.code in it }

private fun japaneseRanges(text: String): List<IntRange> {
    val ranges = mutableListOf<IntRange>()
    var index = 0
    while (index < text.length) {
        if (isJapanese(text[index])) {
            val start = index
            while (index < text.length && isJapanese(text[index])) index++
            ranges += start until index
        } else {
            index++
        }
    }
    return ranges
}

/**
 * 日本語の連続区間へ [family] を明示適用した AnnotatedString を返す。
 * JetBrains Mono に無いグリフを skiko のフォールバック解決に任せると、wasm では
 * `softWrap = false` でも計測幅が実描画幅より狭くなり日本語行が折り返されるため、
 * フォールバックが選ぶのと同じフォントを計測前に確定させる（見た目は変わらない）。
 */
internal fun AnnotatedString.withJapaneseFont(family: FontFamily): AnnotatedString {
    val ranges = japaneseRanges(text)
    if (ranges.isEmpty()) return this
    return buildAnnotatedString {
        append(this@withJapaneseFont)
        ranges.forEach { range ->
            addStyle(SpanStyle(fontFamily = family), range.first, range.last + 1)
        }
    }
}

/** 1行を走査してトークン列にする。どのパターンにも一致しない文字は Base として扱われる。 */
private fun scanLine(line: String, declaredFunctions: Set<String>): List<CodeToken> {
    val tokens = mutableListOf<CodeToken>()
    var index = 0
    while (index < line.length) {
        val token = tokenAt(line, index, declaredFunctions)
        if (token != null) {
            tokens += token
            index += token.text.length
        } else {
            index++
        }
    }
    return tokens
}

private fun tokenAt(line: String, index: Int, declaredFunctions: Set<String>): CodeToken? =
    fixedPatterns.firstNotNullOfOrNull { (kind, regex) ->
        regex.matchAt(line, index)?.let { CodeToken(index, it.value, kind) }
    } ?: wordRegex.matchAt(line, index)?.let {
        CodeToken(index, it.value, classifyWord(line, index, it.value, declaredFunctions))
    } ?: numberRegex.matchAt(line, index)?.let {
        CodeToken(index, it.value, TokenKind.Number)
    }

/** 識別子を前後の文脈（直前の有意文字・直後の有意文字・fun 宣言）から分類する。 */
private fun classifyWord(
    line: String,
    start: Int,
    word: String,
    declaredFunctions: Set<String>,
): TokenKind {
    val before = line.take(start)
    val prevSignificant = before.lastOrNull { !it.isWhitespace() }
    val nextSignificant = line.drop(start + word.length).firstOrNull { !it.isWhitespace() }
    val isNamedArgPosition = prevSignificant == null || prevSignificant == '(' || prevSignificant == ','
    return when {
        // 名前付き引数をキーワードより先に判定する（`data = ...` の data をキーワード扱いしない）
        nextSignificant == '=' && isNamedArgPosition -> TokenKind.NamedArg
        // パラメータ名として使われるソフトキーワード（`data: ...`）はプレーン
        word == "data" && nextSignificant == ':' -> TokenKind.Base
        word in kotlinKeywords -> TokenKind.Keyword
        before.trimEnd().endsWith("fun") -> TokenKind.FunctionName
        // 宣言済み（= このファイルの Composable）関数の呼び出しは AS の Composable 呼び出し色
        nextSignificant == '(' && word in declaredFunctions -> TokenKind.ComposableCall
        // `.` に続く PascalCase は enum エントリ。型参照や通常の呼び出しは実 AS 同様プレーン
        prevSignificant == '.' && word.first().isUpperCase() -> TokenKind.EnumEntry
        else -> TokenKind.Base
    }
}

private fun renderLine(
    line: String,
    tokens: List<CodeToken>,
    colors: KeiColorScheme,
): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    tokens.forEach { token ->
        if (cursor < token.start) appendBase(line.substring(cursor, token.start), colors)
        if (token.kind == TokenKind.Link) {
            appendLink(text = token.text, url = "https://${token.text}", colors = colors)
        } else {
            withStyle(styleOf(token.kind, colors)) { append(token.text) }
        }
        cursor = token.start + token.text.length
    }
    if (cursor < line.length) appendBase(line.substring(cursor), colors)
}

private fun AnnotatedString.Builder.appendBase(text: String, colors: KeiColorScheme) {
    withStyle(SpanStyle(color = colors.textCode)) { append(text) }
}

/** リンク色 + ホバー時下線のスタイルで [text] を [url] へのリンクとして追加する。 */
internal fun AnnotatedString.Builder.appendLink(
    text: String,
    url: String,
    colors: KeiColorScheme,
    linkInteractionListener: LinkInteractionListener? = null,
) {
    withLink(
        LinkAnnotation.Url(
            url = url,
            styles = TextLinkStyles(
                style = SpanStyle(color = colors.syntaxLink),
                hoveredStyle = SpanStyle(
                    color = colors.syntaxLink,
                    textDecoration = TextDecoration.Underline,
                ),
            ),
            linkInteractionListener = linkInteractionListener,
        ),
    ) {
        append(text)
    }
}

private fun styleOf(kind: TokenKind, colors: KeiColorScheme): SpanStyle = when (kind) {
    TokenKind.Keyword -> SpanStyle(color = colors.syntaxKeyword)
    TokenKind.Annotation -> SpanStyle(color = colors.syntaxAnnotation)
    TokenKind.FunctionName -> SpanStyle(color = colors.syntaxFunction)
    TokenKind.ComposableCall -> SpanStyle(color = colors.syntaxComposableCall)
    TokenKind.EnumEntry -> SpanStyle(color = colors.syntaxEnumEntry, fontStyle = FontStyle.Italic)
    TokenKind.StringLit -> SpanStyle(color = colors.syntaxString)
    TokenKind.Number -> SpanStyle(color = colors.syntaxNumber)
    TokenKind.NamedArg -> SpanStyle(color = colors.syntaxNamedArg)
    TokenKind.Comment -> SpanStyle(color = colors.syntaxComment)
    TokenKind.Link -> SpanStyle(color = colors.syntaxLink)
    TokenKind.Base -> SpanStyle(color = colors.textCode)
}
