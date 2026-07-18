package io.github.kei_1111.shared.model

import kotlinx.serialization.Serializable

/** client / server 間で共有する二言語テキスト。表示言語の解決はクライアント側で行う。 */
@Serializable
data class LocalizedText(
    val ja: String,
    val en: String,
)
