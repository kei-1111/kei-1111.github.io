package io.github.kei_1111.app.core.data.contributions

import io.github.kei_1111.shared.model.ContributionCalendar
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

internal fun parseContributions(body: String): ContributionCalendar? = try {
    json.decodeFromString<ContributionCalendar>(body)
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}
