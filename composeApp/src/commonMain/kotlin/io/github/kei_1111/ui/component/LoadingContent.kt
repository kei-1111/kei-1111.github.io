package io.github.kei_1111.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.kei_1111.ui.theme.dimensions.IconSizes
import kei_1111.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val LoadingAnimationRotation = 25f

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoadingContent(
    modifier: Modifier = Modifier,
) {
    val loadingAnimation by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/loading_animation.json").decodeToString(),
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd,
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = loadingAnimation,
                iterations = Compottie.IterateForever,
            ),
            contentDescription = "Loading animation",
            modifier = Modifier
                .size(IconSizes.ExtraLarge)
                .rotate(LoadingAnimationRotation),
        )
    }
}
