package io.github.kei_1111.model

import org.jetbrains.compose.resources.DrawableResource

data class Skill(
    val image: DrawableResource,
    val name: String,
    val rating: Int,
)
