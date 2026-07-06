package io.github.kei_1111.core.data.contributions

/** Android ターゲットは IDE Preview 専用のため取得しない（フォールバックが使われる）。 */
internal actual suspend fun fetchText(url: String): String? = null
