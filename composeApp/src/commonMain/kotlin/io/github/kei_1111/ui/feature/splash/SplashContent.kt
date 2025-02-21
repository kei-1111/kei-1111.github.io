package io.github.kei_1111.ui.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.component.HeadlineLargeText
import io.github.kei_1111.core.designsystem.theme.dimensions.IconSizes
import io.github.kei_1111.core.designsystem.theme.dimensions.Paddings
import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_profile_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashContent(
    s: String,
    profileIconAlphaAnimation: Animatable<Float, AnimationVector1D>,
    profileIconXOffsetAnimation: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.alpha(profileIconAlphaAnimation.value),
        ) {
            Image(
                painter = painterResource(Res.drawable.img_profile_icon),
                contentDescription = "Profile icon",
                modifier = Modifier
                    .offset(x = profileIconXOffsetAnimation.value.dp) // スライドインの位置指定
                    .size(IconSizes.Medium)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
            )
            if (s != "") {
                Spacer(
                    modifier = Modifier
                        .padding(Paddings.Small),
                )
            }
            HeadlineLargeText(
                text = s,
            )
        }
    }
}
