package io.github.kei_1111.ui.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kei_1111.core.designsystem.theme.dimensions.Paddings
import io.github.kei_1111.core.designsystem.theme.dimensions.Weights
import io.github.kei_1111.ui.feature.profile.component.BasicInfoSection
import io.github.kei_1111.ui.feature.profile.component.Footer
import io.github.kei_1111.ui.feature.profile.component.SNSSection
import io.github.kei_1111.ui.feature.profile.component.SkillsSection
import io.github.kei_1111.ui.feature.profile.component.WorksSection

@Composable
fun ProfileDesktopContent(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Row {
                Spacer(
                    modifier = Modifier.weight(Weights.Medium),
                )
                Column(
                    modifier = Modifier
                        .weight(Weights.Large)
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
                    SkillsSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Paddings.Content),
                    )
                    SNSSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Paddings.Content),
                    )
                }
                Spacer(
                    modifier = Modifier.weight(Weights.Medium),
                )
            }
            Footer(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
