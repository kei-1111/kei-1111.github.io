package io.github.kei_1111.server.service

// コンテンツ更新は低頻度のため、GCS 読み出しを抑えつつ公開後数分で反映される鮮度に保つ TTL。
internal const val PUBLISHED_CONTENT_TTL_MILLIS = 5L * 60L * 1000L
