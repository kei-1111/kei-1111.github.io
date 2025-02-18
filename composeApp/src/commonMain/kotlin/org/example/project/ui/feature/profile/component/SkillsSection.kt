package org.example.project.ui.feature.profile.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.ui.component.TitleSmallText

@Composable
fun SkillsSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        TitleSmallText(
            text = "Skills",
        )
    }
}
