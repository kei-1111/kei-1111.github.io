package io.github.kei_1111.app.core.data.network

/** Android ターゲットは IDE Preview 専用のため取得しない（常に取得失敗として扱われる）。 */
internal actual suspend fun fetchText(url: String): String? = null
