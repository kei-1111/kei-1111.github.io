package org.example.project.ui.feature.profile

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.model.SkillSet
import org.example.project.ui.component.BodyMediumText
import org.example.project.ui.feature.profile.theme.ProfileAnimations
import org.example.project.ui.theme.dimensions.IconSizes
import org.example.project.ui.theme.dimensions.Paddings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        SectionTitle(
            title = "スキル",
        )

        SectionContent(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Paddings.Large),
                ) {
                    SkillSet.ratedSkills.forEach { skill ->
                        RatedSkill(
                            modifier = Modifier.fillMaxWidth(),
                            skillIcon = skill.image,
                            rate = skill.rating,
                        )
                    }
                    SectionSubTitle(
                        title = "使用したことのあるライブラリ",
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        SkillSet.usedLibraries.forEach { libraryName ->
                            UsedLibrary(
                                libraryName = libraryName,
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
fun UsedLibrary(
    libraryName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                CircleShape,
            )
            .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            .padding(
                horizontal = Paddings.Small,
                vertical = Paddings.ExtraSmall,
            ),
        contentAlignment = Alignment.Center,
    ) {
        BodyMediumText(
            text = libraryName,
        )
    }
}

@Composable
fun RatedSkill(
    skillIcon: DrawableResource,
    rate: Int,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkillIcon(
            skillIcon = skillIcon,
        )
        Spacer(modifier = Modifier.padding(Paddings.Small))
        for (i in SkillSet.MinRating..SkillSet.MaxRating) {
            val alpha by infiniteTransition.animateFloat(
                initialValue = ProfileAnimations.RatedInitialAlpha,
                targetValue = ProfileAnimations.RatedFinalAlpha,
                animationSpec = infiniteRepeatable(
                    animation = tween(ProfileAnimations.RatedDuration),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * ProfileAnimations.RatedInitialStartOffset),
                ),
            )

            Icon(
                imageVector = Icons.Rounded.StarRate,
                contentDescription = "Skill Icon",
                modifier = Modifier.size(IconSizes.Medium),
                tint = if (i <= rate) {
                    MaterialTheme.colorScheme.inversePrimary.copy(
                        alpha = alpha,
                    )
                } else {
                    MaterialTheme.colorScheme.surfaceDim
                },
            )
        }
    }
}

@Composable
fun SkillIcon(
    skillIcon: DrawableResource,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(skillIcon),
        contentDescription = null,
        modifier = modifier.size(IconSizes.Small),
    )
}
