package org.example.project.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.example.project.model.CareerSet
import org.example.project.ui.component.BodyMediumText
import org.example.project.ui.theme.dimensions.Paddings
import org.example.project.ui.theme.dimensions.Weights

@Composable
fun CareerSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        SectionTitle(
            title = "経歴",
        )
        SectionContent(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Box(
                    modifier = Modifier.width(ProfileDimensions.CareerDividerWidth),
                    contentAlignment = Alignment.Center,
                ) {
                    GradationVerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        thickness = ProfileDimensions.CareerThickness,
                        colors = persistentListOf(
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.surface,
                        ),
                    )
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    CareerSet.years.forEach {
                        CareerByYear(
                            modifier = Modifier.fillMaxWidth(),
                            year = it.year.toString(),
                            content = {
                                it.events.forEach { event ->
                                    BodyMediumText(
                                        text = event,
                                    )
                                }
                            },
                        )
                        Spacer(modifier = Modifier.weight(Weights.Medium))
                    }
                    Spacer(modifier = Modifier.weight(Weights.Medium))
                }
            },
        )
    }
}

@Composable
fun CareerByYear(
    year: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Circle(
                size = ProfileDimensions.CareerDividerWidth,
                color = MaterialTheme.colorScheme.inversePrimary,
            )
            Spacer(modifier = Modifier.padding(Paddings.ExtraSmall))
            SectionSubTitle(
                title = year,
            )
        }
        Column(
            modifier = Modifier.padding(start = Paddings.Large),
            verticalArrangement = Arrangement.spacedBy(Paddings.ExtraSmall),
        ) {
            content()
        }
    }
}

@Composable
fun GradationVerticalDivider(
    colors: ImmutableList<Color>,
    modifier: Modifier = Modifier,
    thickness: Dp = ProfileDimensions.CareerThickness,
) {
    Box(
        modifier = modifier.width(thickness).background(
            brush = Brush.verticalGradient(
                colors = colors,
            ),
            shape = CircleShape,
        ),
    )
}
