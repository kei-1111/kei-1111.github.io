package org.example.project.ui.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.ui.feature.profile.component.BasicInfoSection
import org.example.project.ui.feature.profile.component.SkillsSection
import org.example.project.ui.feature.profile.component.WorksSection
import org.example.project.ui.theme.dimensions.Paddings

@Composable
fun ProfileMobileContent(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Paddings.Content),
            verticalArrangement = Arrangement.spacedBy(Paddings.Large),
        ) {
            BasicInfoSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Paddings.Content),
            )
            WorksSection(
                modifier = Modifier.fillMaxWidth(),
            )
            SkillsSection()
        }
    }
}
