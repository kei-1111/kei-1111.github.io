package org.example.project.model

import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_android_studio
import kei_1111.composeapp.generated.resources.img_figma
import kei_1111.composeapp.generated.resources.img_fleet
import kei_1111.composeapp.generated.resources.img_github
import kei_1111.composeapp.generated.resources.img_intellij_idea
import org.jetbrains.compose.resources.DrawableResource

data object ToolSet {
    val tools = listOf(
        Tool(
            image = Res.drawable.img_android_studio,
            name = "Android Studio",
        ),
        Tool(
            image = Res.drawable.img_intellij_idea,
            name = "IntelliJ IDEA",
        ),
        Tool(
            image = Res.drawable.img_fleet,
            name = "Fleet",
        ),
        Tool(
            image = Res.drawable.img_github,
            name = "Git",
        ),
        Tool(
            image = Res.drawable.img_figma,
            name = "Figma",
        ),
    )
}

data class Tool(
    val image: DrawableResource,
    val name: String,
)
