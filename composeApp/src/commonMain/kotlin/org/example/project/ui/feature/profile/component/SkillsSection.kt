package org.example.project.ui.feature.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.example.project.data.SkillSet
import org.example.project.model.Skill
import org.example.project.ui.component.LabelMediumText
import org.example.project.ui.component.TitleSmallText
import org.example.project.ui.feature.profile.theme.ProfileAnimations
import org.example.project.ui.theme.dimensions.IconSizes
import org.example.project.ui.theme.dimensions.Paddings
import org.example.project.ui.theme.dimensions.Weights
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SkillsSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Paddings.Small),
    ) {
        TitleSmallText(
            text = "Skills",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Paddings.Small),
        ) {
            SkillSet.ratedSkills.forEach { skill ->
                SkillItem(
                    skill = skill,
                    modifier = Modifier.weight(Weights.Medium),
                )
            }
        }
    }
}

@Composable
private fun SkillItem(
    skill: Skill,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(Paddings.ExtraSmall),
            verticalArrangement = Arrangement.spacedBy(Paddings.Small),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RatingSkill(
                rating = skill.rating.toFloat(),
                image = skill.image,
                name = skill.name,
            )
            LabelMediumText(
                text = skill.name,
            )
        }
    }
}

@Composable
private fun RatingSkill(
    rating: Float,
    image: DrawableResource,
    name: String,
    modifier: Modifier = Modifier,
) {
    var currentProgress by remember { mutableStateOf(SkillSet.minRating) }

    LaunchedEffect(Unit) {
        loadProgress(rating, updateProgress = { currentProgress = it })
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { currentProgress },
            modifier = Modifier.size(IconSizes.Medium),
        )
        Image(
            painter = painterResource(image),
            contentDescription = name,
            modifier = Modifier.size(IconSizes.Small),
        )
    }
}

suspend fun loadProgress(
    rating: Float,
    updateProgress: (Float) -> Unit,
) {
    for (i in SkillSet.minRating.toInt()..rating.toInt()) {
        updateProgress(i.toFloat() / SkillSet.maxRating)
        delay(ProfileAnimations.RatedDuration)
    }
}
