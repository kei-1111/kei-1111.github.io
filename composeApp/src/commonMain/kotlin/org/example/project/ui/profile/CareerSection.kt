package org.example.project.ui.profile

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
import org.example.project.model.UiConfig
import org.example.project.ui.component.BodyMediumText

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
                    modifier = Modifier.width(UiConfig.ProfileCareerDividerWidth),
                    contentAlignment = Alignment.Center,
                ) {
                    GradationVerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        thickness = UiConfig.ProfileCareerThickness,
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
                        Spacer(modifier = Modifier.weight(UiConfig.DefaultWeight))
                    }
                    Spacer(modifier = Modifier.weight(UiConfig.DefaultWeight))
                }
            },
        )
    }
}

@Composable
fun CareerByYear(
    year: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Circle(
                size = UiConfig.ProfileCareerDividerWidth,
                color = MaterialTheme.colorScheme.inversePrimary,
            )
            Spacer(modifier = Modifier.padding(UiConfig.ExtraSmallPadding))
            SectionSubTitle(
                title = year,
            )
        }
        Column(
            modifier = Modifier.padding(start = UiConfig.LargePadding),
            verticalArrangement = Arrangement.spacedBy(UiConfig.ExtraSmallPadding),
        ) {
            content()
        }
    }
}

@Composable
fun GradationVerticalDivider(
    colors: ImmutableList<Color>,
    modifier: Modifier = Modifier,
    thickness: Dp = UiConfig.ProfileCareerThickness,
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
