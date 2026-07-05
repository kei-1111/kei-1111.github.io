package io.github.kei_1111.core.data.contributions

import io.github.kei_1111.core.model.ContributionCalendar
import io.github.kei_1111.core.model.ContributionDay
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal const val CONTRIBUTIONS_API = "https://github-contributions-api.jogruber.de/v4/"

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class ContributionDayDto(
    val date: String,
    val count: Int,
    val level: Int,
)

@Serializable
private data class ContributionsResponseDto(
    val total: Map<String, Int>,
    val contributions: List<ContributionDayDto>,
)

internal fun parseContributions(body: String): ContributionCalendar? = try {
    val response = json.decodeFromString<ContributionsResponseDto>(body)
    ContributionCalendar(
        totalLastYear = response.total.values.sum(),
        days = response.contributions.map { it.toModel() }.toImmutableList(),
    )
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}

private fun ContributionDayDto.toModel() = ContributionDay(
    date = date,
    count = count,
    level = level,
)
