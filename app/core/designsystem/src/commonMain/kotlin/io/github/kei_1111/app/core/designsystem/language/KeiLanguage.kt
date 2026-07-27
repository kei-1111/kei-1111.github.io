package io.github.kei_1111.app.core.designsystem.language

/** サイトの表示言語。[tag] は BCP 47 の言語タグ（document.lang / リソース解決に使用）。 */
enum class KeiLanguage(val tag: String) {
    Ja("ja"),
    En("en"),
    ;

    companion object {
        /** ブラウザロケールタグから初期言語を解決する。ja 系のみ日本語、それ以外は英語。 */
        fun fromTag(tag: String?): KeiLanguage = if (tag?.startsWith("ja") == true) Ja else En
    }
}
