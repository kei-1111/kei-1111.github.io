package io.github.kei_1111.ui.feature.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import io.github.kei_1111.core.data.BasicInfoSet
import io.github.kei_1111.core.designsystem.component.BodyMediumText
import io.github.kei_1111.core.designsystem.component.IconText
import io.github.kei_1111.core.designsystem.component.TitleMediumText
import io.github.kei_1111.core.designsystem.theme.dimensions.Alpha
import io.github.kei_1111.core.designsystem.theme.dimensions.IconSizes
import io.github.kei_1111.core.designsystem.theme.dimensions.Paddings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun BasicInfoSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Paddings.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicInfoDetails(
            profileIcon = BasicInfoSet.profileIcon,
            name = BasicInfoSet.name,
            birthday = BasicInfoSet.birthday,
            university = BasicInfoSet.university,
        )
        SelfIntroduction(
            introduction = BasicInfoSet.introduction,
        )
    }
}

@Composable
private fun BasicInfoDetails(
    profileIcon: DrawableResource,
    name: String,
    birthday: String,
    university: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .size(IconSizes.Large)
                .clip(MaterialTheme.shapes.medium),
            painter = painterResource(profileIcon),
            contentDescription = "Profile Icon",
            contentScale = ContentScale.Crop,
        )
        TitleMediumText(
            text = name,
        )
        Column {
            IconText(
                icon = Icons.Rounded.Cake,
                text = birthday,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.Medium),
            )
            IconText(
                icon = Icons.Rounded.School,
                text = university,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.Medium),
            )
        }
    }
}

@Composable
private fun SelfIntroduction(
    introduction: String,
    modifier: Modifier = Modifier,
) {
    BodyMediumText(
        text = introduction,
        modifier = modifier,
    )
}
