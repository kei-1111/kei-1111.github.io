package io.github.kei_1111.ui.feature.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kei_1111.core.data.SNSSet
import io.github.kei_1111.core.designsystem.component.ElevatedButton
import io.github.kei_1111.core.designsystem.component.TitleSmallText
import io.github.kei_1111.core.designsystem.theme.dimensions.IconSizes
import io.github.kei_1111.core.designsystem.theme.dimensions.Paddings
import io.github.kei_1111.utils.openUrl
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
