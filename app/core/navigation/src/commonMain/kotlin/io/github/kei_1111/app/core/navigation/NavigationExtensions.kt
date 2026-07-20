@file:Suppress("MagicNumber")

package io.github.kei_1111.app.core.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.scene.DialogSceneStrategy
import io.github.kei_1111.app.core.designsystem.theme.animations.Durations

private const val FORWARD_TRANSITION_DURATION = 700
private const val NAVIGATION_INITIAL_ALPHA = 0.1f

// navigation-compose (nav2) has no built-in-default equivalent on nav3; this mirrors its
// StandardDefaultNavTransitions (fadeIn/fadeOut with tween(700)), which NavGraph.kt relied on
// implicitly for the forward Splash -> Profile transition.
fun crossFadeIn(): ContentTransform =
    fadeIn(animationSpec = tween(FORWARD_TRANSITION_DURATION)) togetherWith
        fadeOut(animationSpec = tween(FORWARD_TRANSITION_DURATION))

// Mirrors NavGraph.kt's popEnterTransition(Splash)/popExitTransition(Profile) for the
// base scene's Profile -> Splash pop path. Dialog destinations are rendered separately.
fun crossFadeOut(): ContentTransform =
    fadeIn(
        animationSpec = tween(Durations.Long),
        initialAlpha = NAVIGATION_INITIAL_ALPHA,
    ) togetherWith fadeOut(animationSpec = tween(Durations.Long))

/** Dialog transition metadata consumed by [DialogSceneStrategy]. */
fun dialogTransition(
    dialogProperties: DialogProperties = DialogProperties(),
): Map<String, Any> = DialogSceneStrategy.dialog(dialogProperties)
