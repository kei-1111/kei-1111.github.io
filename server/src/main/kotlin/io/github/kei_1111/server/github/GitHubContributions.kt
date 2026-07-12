package io.github.kei_1111.server.github

import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.ContributionDay
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

// 期間をデフォルトに依存させると既存 UI の「LAST YEAR」表示と境界がずれるため from/to を明示する。
internal val CONTRIBUTIONS_QUERY = """
    query(${'$'}login: String!, ${'$'}from: DateTime!, ${'$'}to: DateTime!) {
      user(login: ${'$'}login) {
        contributionsCollection(from: ${'$'}from, to: ${'$'}to) {
          contributionCalendar {
            totalContributions
            weeks { contributionDays { date contributionCount contributionLevel } }
          }
        }
      }
    }
""".trimIndent()

@Serializable
internal data class ContributionsData(val user: ContributionsUser? = null)

@Serializable
internal data class ContributionsUser(val contributionsCollection: ContributionsCollection)

@Serializable
internal data class ContributionsCollection(val contributionCalendar: ContributionCalendarNode)

@Serializable
internal data class ContributionCalendarNode(
    val totalContributions: Int = 0,
    val weeks: List<WeekNode> = emptyList(),
)

@Serializable
internal data class WeekNode(val contributionDays: List<ContributionDayNode> = emptyList())

@Serializable
internal data class ContributionDayNode(
    val date: String,
    val contributionCount: Int = 0,
    // enum で厳密に decode すると GitHub 側の将来の enum 追加でレスポンス全体が壊れるため String で受ける。
    val contributionLevel: String,
)

private const val LOOKBACK_DAYS = 365L

// 未知の level は黙って 0 に畳まず null で取得全体を失敗にし、契約異常に気付けるようにする。
@Suppress("MagicNumber")
private fun contributionLevelToInt(level: String): Int? = when (level) {
    "NONE" -> 0
    "FIRST_QUARTILE" -> 1
    "SECOND_QUARTILE" -> 2
    "THIRD_QUARTILE" -> 3
    "FOURTH_QUARTILE" -> 4
    else -> null
}

internal suspend fun GitHubClient.fetchContributions(): ContributionCalendar? {
    val to = Instant.now().truncatedTo(ChronoUnit.SECONDS)
    val from = to.minus(LOOKBACK_DAYS, ChronoUnit.DAYS)
    val variables = mapOf(
        "login" to PROFILE_LOGIN,
        "from" to from.toString(),
        "to" to to.toString(),
    )
    return execute<ContributionsData>(CONTRIBUTIONS_QUERY, variables)
        ?.user
        ?.contributionsCollection
        ?.contributionCalendar
        ?.toContributionCalendar()
}

private fun ContributionCalendarNode.toContributionCalendar(): ContributionCalendar? {
    val days = weeks.flatMap { it.contributionDays }.map { day ->
        val level = contributionLevelToInt(day.contributionLevel) ?: return null
        ContributionDay(date = day.date, count = day.contributionCount, level = level)
    }
    return ContributionCalendar(totalLastYear = totalContributions, days = days.toImmutableList())
}
