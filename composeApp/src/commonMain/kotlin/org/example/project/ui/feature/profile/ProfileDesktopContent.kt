package org.example.project.ui.feature.profile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_profile_icon
import org.example.project.ui.feature.profile.theme.ProfileDimensions
import org.example.project.ui.theme.animations.Durations
import org.example.project.ui.theme.dimensions.IconSizes
import org.example.project.ui.theme.dimensions.Paddings
import org.example.project.ui.theme.dimensions.Weights

@Composable
fun ProfileDesktopContent(
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val animatedSize by animateDpAsState(
        targetValue = if (isHovered) IconSizes.ExtraLarge else IconSizes.Large,
        animationSpec = tween(durationMillis = Durations.Short),
    )

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(Paddings.Content),
            ) {
                ProfileHeader(
                    modifier = Modifier.fillMaxWidth(),
                    profileIcon = Res.drawable.img_profile_icon,
                    name = "けい",
                )

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(
                            start = Paddings.Content,
                            top = Paddings.Medium,
                            end = Paddings.Content,
                        ),
                ) {
                    CareerSection(
                        modifier = Modifier.weight(Weights.Medium),
                    )

                    Column(
                        modifier = Modifier.weight(ProfileDimensions.DesktopRightWeight),
                    ) {
                        SkillsSection()
                        Spacer(modifier = Modifier.weight(Weights.Medium))
                        ToolsSection()
                    }
                }
            }
        }
        WorksIcon(
            animatedSize = animatedSize,
            circleColor = MaterialTheme.colorScheme.inversePrimary,
            interactionSource = interactionSource,
        )
    }
}
