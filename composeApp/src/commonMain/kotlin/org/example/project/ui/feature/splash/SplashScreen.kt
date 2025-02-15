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
import org.example.project.ui.component.LoadingContent
import org.example.project.ui.feature.splash.theme.SplashAnimations
import org.example.project.ui.theme.NotoSansJpFamily
import org.example.project.ui.theme.animations.Durations

@Suppress("ModifierMissing")
@Composable
fun SplashScreen(
    toProfile: () -> Unit,
) {
    val text = "Hello!!"
    var s by remember { mutableStateOf("") }

    val profileIconAlphaAnimation =
        remember { Animatable(SplashAnimations.InitialProfileIconAlpha) }
    val profileIconXOffsetAnimation =
        remember { Animatable(SplashAnimations.InitialProfileIconXOffset) }

    val isFontLoaded = MaterialTheme.typography.headlineLarge.fontFamily == NotoSansJpFamily()

    val currentToProfile by rememberUpdatedState(toProfile)

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(isFontLoaded) {
        if (isFontLoaded) {
            profileIconAlphaAnimation.animateTo(
                targetValue = SplashAnimations.FinalProfileIconAlpha,
                animationSpec = tween(durationMillis = Durations.Long),
            )

            isLoading = false

            profileIconXOffsetAnimation.animateTo(
                targetValue = SplashAnimations.FinalProfileIconXOffset,
                animationSpec = tween(
                    durationMillis = Durations.Medium,
                    easing = LinearOutSlowInEasing,
                ),
            )

            delay(Durations.Short.toLong())

            text.indices.forEach { index ->
                delay(SplashAnimations.CharacterDisplayDelay)
                s = text.substring(0..index)
            }

            delay(Durations.Long.toLong())

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
