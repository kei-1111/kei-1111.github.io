package io.github.kei_1111.app.feature.splash.destination.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kei_1111.app.core.designsystem.component.KeiAsyncImagePrefetch
import io.github.kei_1111.app.core.designsystem.theme.rememberJetBrainsMonoFontsLoaded
import io.github.kei_1111.app.core.designsystem.theme.rememberNotoSansJpFontsLoaded
import io.github.kei_1111.app.core.designsystem.theme.rememberZenKakuGothicNewFontsLoaded
import io.github.kei_1111.app.core.mvi.MviEffect
import io.github.kei_1111.app.core.utils.rememberIsPageVisible
import io.github.kei_1111.app.feature.splash.destination.splash.model.SplashFont

@Composable
internal fun SplashScreenRoot(
    viewModel: SplashViewModel,
    navigateProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MviEffect(
        effect = state.effect,
        onConsume = { viewModel.onIntent(SplashIntent.ConsumeEffect) },
    ) { effect ->
        when (effect) {
            is SplashEffect.NavigateProfile -> navigateProfile()
        }
    }

    KeiAsyncImagePrefetch(state.imagePrefetchUrls)

    val jetBrainsMonoLoaded by rememberUpdatedState(rememberJetBrainsMonoFontsLoaded())
    val notoSansJpLoaded by rememberUpdatedState(rememberNotoSansJpFontsLoaded())
    val zenKakuGothicNewLoaded by rememberUpdatedState(rememberZenKakuGothicNewFontsLoaded())

    LaunchedEffect(jetBrainsMonoLoaded) {
        if (jetBrainsMonoLoaded) viewModel.onIntent(SplashIntent.ReceiveFontLoaded(SplashFont.JetBrainsMono))
    }
    LaunchedEffect(notoSansJpLoaded) {
        if (notoSansJpLoaded) viewModel.onIntent(SplashIntent.ReceiveFontLoaded(SplashFont.NotoSansJp))
    }
    LaunchedEffect(zenKakuGothicNewLoaded) {
        if (zenKakuGothicNewLoaded) viewModel.onIntent(SplashIntent.ReceiveFontLoaded(SplashFont.ZenKakuGothicNew))
    }

    // 非表示タブでは Chrome が requestAnimationFrame を停止しリコンポジションも止まるため、
    // イベントリスナーが直接書き込む State を snapshotFlow で監視し、可視状態の変化を確実に伝える
    val isPageVisible = rememberIsPageVisible()
    LaunchedEffect(Unit) {
        snapshotFlow { isPageVisible.value }.collect { visible ->
            viewModel.onIntent(SplashIntent.UpdatePageVisibility(visible))
        }
    }

    SplashScreen(
        state = state,
        modifier = modifier,
    )
}
