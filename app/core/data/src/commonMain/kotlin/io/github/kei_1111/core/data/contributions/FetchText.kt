package io.github.kei_1111.core.data.contributions

/** プラットフォーム毎のテキスト取得。失敗時は null（Android は Preview 専用のため常に null）。 */
internal expect suspend fun fetchText(url: String): String?
