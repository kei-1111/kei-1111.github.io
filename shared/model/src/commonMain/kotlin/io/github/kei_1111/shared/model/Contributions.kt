package io.github.kei_1111.shared.model

import io.github.kei_1111.shared.model.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContributionDay(
    @SerialName("date")
    val date: String,
    @SerialName("count")
    val count: Int,
    @SerialName("level")
    val level: Int,
)

/**
 * client / server 間で共有する JSON 契約。互換性ルールは [GitHubProfile] の KDoc を参照。
 */
@Serializable
data class ContributionCalendar(
    @SerialName("totalLastYear")
    val totalLastYear: Int,
    @SerialName("days")
    @Serializable(with = ImmutableListSerializer::class)
    val days: ImmutableList<ContributionDay>,
)
