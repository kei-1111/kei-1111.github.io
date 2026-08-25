package io.github.kei_1111.server.service

/** GitHub API のレートリミット消費を抑えつつ、データのずれが目立たない程度の鮮度に保つ共通 TTL。 */
internal const val GITHUB_DATA_TTL_MILLIS = 10L * 60L * 1000L
