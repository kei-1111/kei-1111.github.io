package io.github.kei_1111.core.data

import io.github.kei_1111.core.model.Skill
import kei_1111.core.data.generated.resources.Res
import kei_1111.core.data.generated.resources.img_compose_multiplatform
import kei_1111.core.data.generated.resources.img_github
import kei_1111.core.data.generated.resources.img_jetpack_compose
import kei_1111.core.data.generated.resources.img_kotlin

data object SkillSet {
    const val maxRating = 100f
    const val minRating = 1f

    val ratedSkills = listOf(
        Skill(
            image = Res.drawable.img_kotlin,
            name = "Kotlin",
            rating = 70,
        ),
        Skill(
            image = Res.drawable.img_jetpack_compose,
            name = "Compose",
            rating = 80,
        ),
        Skill(
            image = Res.drawable.img_compose_multiplatform,
            name = "CMP",
            rating = 40,
        ),
        Skill(
            image = Res.drawable.img_github,
            name = "GitHub",
            rating = 60,
        ),
    )
}
