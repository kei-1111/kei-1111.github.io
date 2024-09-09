package org.example.project.model

import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_jetpack_compose
import kei_1111.composeapp.generated.resources.img_kotlin
import org.jetbrains.compose.resources.DrawableResource

data object SkillSet {
    const val MaxRating = 5
    const val MinRating = 1

    val ratedSkills = listOf(
        Skill(
            image = Res.drawable.img_kotlin,
            name = "Kotlin",
            rating = 4,
        ),
        Skill(
            image = Res.drawable.img_jetpack_compose,
            name = "Jetpack Compose",
            rating = 3,
        ),
    )

    val usedLibraries = listOf(
        "Coil",
        "Hilt",
        "KSP",
        "Kapt",
        "OkHttp",
        "kotlinx.serialization",
        "Retrofit",
        "Paging",
        "Room",
        "Preference DataStore",
        "Compose Navigation",
        "Cloud Firestore",
        "Firebase Authentication",
        "Jetpack Glance",
    )
}

data class Skill(
    val image: DrawableResource,
    val name: String,
    val rating: Int,
)
