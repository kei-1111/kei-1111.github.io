package org.example.project.ui.feature.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.data.SNSSet
import org.example.project.ui.component.ElevatedButton
import org.example.project.ui.component.TitleSmallText
import org.example.project.ui.theme.dimensions.IconSizes
import org.example.project.ui.theme.dimensions.Paddings
import org.example.project.utils.openUrl
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SNSSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Paddings.Small),
    ) {
        TitleSmallText(
            text = "SNS",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Paddings.Large),
        ) {
            SNSSet.SNSs.forEach { sns ->
                SNSButton(
                    onClick = { openUrl(sns.url) },
                    image = sns.image,
                    name = sns.name,
                )
            }
        }
    }
}

@Composable
private fun SNSButton(
    onClick: () -> Unit,
    image: DrawableResource,
    name: String,
    modifier: Modifier = Modifier,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = name,
            modifier = Modifier
                .size(IconSizes.Medium)
                .padding(Paddings.Small),
        )
    }
}
