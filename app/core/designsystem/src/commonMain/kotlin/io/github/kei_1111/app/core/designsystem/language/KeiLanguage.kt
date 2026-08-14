package io.github.kei_1111.app.core.designsystem.language

import androidx.compose.runtime.staticCompositionLocalOf

/** [tag] は BCP 47 の言語タグ（document.lang / リソース解決に使用）。 */
enum class KeiLanguage(val tag: String) {
    Ja("ja"),
    En("en"),
    ;

    companion object {
        fun fromTag(tag: String?): KeiLanguage = if (tag?.startsWith("ja") == true) Ja else En
    }
}

/** Preview は提供者を経由せず描画されるため、既定値は error ではなく [KeiLanguage.Ja] にする。 */
val LocalKeiLanguage = staticCompositionLocalOf { KeiLanguage.Ja }
