package org.example.project.ui.feature.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.example.project.model.AnimationConfig
import org.example.project.ui.component.LoadingContent
import org.example.project.ui.theme.NotoSansJpFamily

@Suppress("ModifierMissing")
@Composable
fun SplashScreen(
    toProfile: () -> Unit,
) {
    val text = "Hello!!"
    var s by remember { mutableStateOf("") }

    val profileIconAlphaAnimation =
        remember { Animatable(AnimationConfig.SplashInitialProfileIconAlpha) }
    val profileIconXOffsetAnimation =
        remember { Animatable(AnimationConfig.SplashInitialProfileIconXOffset) }

    val isFontLoaded = MaterialTheme.typography.headlineLarge.fontFamily == NotoSansJpFamily()

    val currentToProfile by rememberUpdatedState(toProfile)

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(isFontLoaded) {
        if (isFontLoaded) {
            profileIconAlphaAnimation.animateTo(
                targetValue = AnimationConfig.SplashFinalProfileIconAlpha,
                animationSpec = tween(durationMillis = AnimationConfig.LongDuration),
            )

            isLoading = false

            profileIconXOffsetAnimation.animateTo(
                targetValue = AnimationConfig.SplashFinalProfileIconXOffset,
                animationSpec = tween(
                    durationMillis = AnimationConfig.MediumDuration,
                    easing = LinearOutSlowInEasing,
                ),
            )

            delay(AnimationConfig.ShortDuration.toLong())

            text.indices.forEach { index ->
                delay(AnimationConfig.SplashCharacterDisplayDelay)
                s = text.substring(0..index)
            }

            delay(AnimationConfig.LongDuration.toLong())

            currentToProfile()
        }
    }

    AnimatedVisibility(isLoading || !isFontLoaded) {
        LoadingContent(
            modifier = Modifier.fillMaxSize(),
        )
    }

    SplashContent(
        s = s,
        profileIconAlphaAnimation = profileIconAlphaAnimation,
        profileIconXOffsetAnimation = profileIconXOffsetAnimation,
        modifier = Modifier.fillMaxSize(),
    )
}
