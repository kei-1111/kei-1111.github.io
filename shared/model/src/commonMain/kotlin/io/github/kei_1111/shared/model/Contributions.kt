package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

@Serializable
data class ContributionDay(
    val date: String,
    val count: Int,
    val level: Int,
)

/**
 * client / server 間で共有する JSON 契約。
 */
@Serializable
data class ContributionCalendar(
    val totalLastYear: Int,
    @Serializable(with = ImmutableListSerializer::class)
    val days: ImmutableList<ContributionDay>,
)
