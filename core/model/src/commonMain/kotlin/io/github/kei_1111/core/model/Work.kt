package io.github.kei_1111.core.model

import org.jetbrains.compose.resources.DrawableResource

data class Work(
    val image: DrawableResource,
    val name: String,
    val logo: DrawableResource?,
    val developmentType: DevelopmentType,
    val description: String,
    val movieUrl: String?,
    val slideUrl: String?,
    val githubUrl: String?,
    val googlePlayUrl: String?,
)

enum class DevelopmentType {
    Individual,
    Team,
}
