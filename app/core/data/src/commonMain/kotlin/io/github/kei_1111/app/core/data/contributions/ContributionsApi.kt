package io.github.kei_1111.app.core.data.contributions

import io.github.kei_1111.shared.model.ContributionCalendar
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

// ignoreUnknownKeys は契約互換性(旧 client + 新 server)のため必須 — shared/model の互換性ルールを参照。
private val json = Json { ignoreUnknownKeys = true }

internal fun parseContributions(body: String): ContributionCalendar? = try {
    json.decodeFromString<ContributionCalendar>(body)
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}
