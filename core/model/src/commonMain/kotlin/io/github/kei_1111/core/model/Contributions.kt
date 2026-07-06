package io.github.kei_1111.core.model

import kotlinx.collections.immutable.ImmutableList

data class ContributionDay(
    val date: String,
    val count: Int,
    val level: Int,
)

data class ContributionCalendar(
    val totalLastYear: Int,
    val days: ImmutableList<ContributionDay>,
)
