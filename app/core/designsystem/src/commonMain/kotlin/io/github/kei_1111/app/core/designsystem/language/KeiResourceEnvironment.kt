@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.kei_1111.app.core.designsystem.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.ComposeEnvironment
import org.jetbrains.compose.resources.DensityQualifier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.ThemeQualifier

/**
 * Res.string の解決言語を [language] に追従させるための
 * LocalComposeEnvironment オーバーライド。CMP 1.11 には実行時ロケール切替の公開 API が無く
 * （ComposeEnvironment / LocalComposeEnvironment は internal）、INVISIBLE_MEMBER 抑制で
 * internal API に依存している。CMP 更新で内部が変われば実行時ではなくコンパイルエラーとして顕在化する。
 *
 * 内部 API 依存をこのファイルへ閉じ込めるため、KeiTheme からはこの入口だけを呼ぶ。
 * アプリ全体が読む [LocalKeiLanguage] もここで提供する。
 */
@Composable
internal fun KeiResourceEnvironment(
    isDark: Boolean,
    language: KeiLanguage,
    content: @Composable () -> Unit,
) {
    val environment = remember(isDark, language) { KeiComposeEnvironment(isDark, language) }
    CompositionLocalProvider(
        LocalKeiLanguage provides language,
        LocalComposeEnvironment provides environment,
        content = content,
    )
}

@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
private class KeiComposeEnvironment(
    private val isDark: Boolean,
    private val language: KeiLanguage,
) : ComposeEnvironment {
    @Composable
    override fun rememberEnvironment(): ResourceEnvironment {
        return remember(language, isDark) {
            ResourceEnvironment(
                language = LanguageQualifier(language.tag),
                region = RegionQualifier(""),
                theme = if (isDark) ThemeQualifier.DARK else ThemeQualifier.LIGHT,
                density = DensityQualifier.MDPI,
            )
        }
    }
}
