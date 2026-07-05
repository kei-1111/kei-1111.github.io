package io.github.kei_1111.feature.splash

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.kei_1111.core.designsystem.theme.rememberJetBrainsMonoFontsLoaded
import io.github.kei_1111.core.designsystem.theme.rememberNotoSansJpFontsLoaded
import io.github.kei_1111.core.designsystem.theme.rememberZenKakuGothicNewFontsLoaded
import io.github.kei_1111.core.utils.rememberIsPageVisible
import io.github.kei_1111.feature.splash.theme.SplashAnimations
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/** フルブリード型(モバイル)スプラッシュへ切り替えるブレークポイント。ProfileScreen と揃える。 */
private val CompactWidth = 900.dp

@Suppress("ModifierMissing")
@Composable
fun SplashScreen(
    toProfile: () -> Unit,
) {
    // 各フォントをフォントキャッシュへ実ロードし、完了を snapshotFlow で監視できるよう橋渡しする
    val currentJetBrainsMonoLoaded by rememberUpdatedState(rememberJetBrainsMonoFontsLoaded())
    val currentNotoSansJpLoaded by rememberUpdatedState(rememberNotoSansJpFontsLoaded())
    val currentZenKakuGothicNewLoaded by rememberUpdatedState(rememberZenKakuGothicNewFontsLoaded())
    val currentToProfile by rememberUpdatedState(toProfile)

    // State オブジェクト自体は不変参照なので rememberUpdatedState のブリッジは不要
    val isPageVisible = rememberIsPageVisible()

    var jetBrainsMonoStep by remember { mutableStateOf(SplashStep.Running) }
    var notoSansJpStep by remember { mutableStateOf(SplashStep.Running) }
    var zenKakuGothicNewStep by remember { mutableStateOf(SplashStep.Running) }
    var renderStep by remember { mutableStateOf(SplashStep.Running) }
    var buildStatus by remember { mutableStateOf(BuildStatus.Running) }

    LaunchedEffect(Unit) {
        val shownAt = TimeSource.Monotonic.markNow()

        // 実際に使うフォントリソースを読み込み、各ログ行の完了表示と連動させる。
        // 非表示タブでは rAF 停止によりリコンポジションが止まりロード完了が伝播しないため、
        // タイムアウトはページ表示中のみ進める（表示中に満了した場合の失敗挙動は従来どおり）。
        val isLoaded = awaitWithVisibleTimeout(
            isPageVisible = { isPageVisible.value },
            timeoutMillis = SplashAnimations.FontLoadTimeoutMillis,
        ) {
            joinAll(
                launch {
                    snapshotFlow { currentJetBrainsMonoLoaded }.first { it }
                    jetBrainsMonoStep = SplashStep.Done
                },
                launch {
                    snapshotFlow { currentNotoSansJpLoaded }.first { it }
                    notoSansJpStep = SplashStep.Done
                },
                launch {
                    snapshotFlow { currentZenKakuGothicNewLoaded }.first { it }
                    zenKakuGothicNewStep = SplashStep.Done
                },
            )
        }

        if (!isLoaded) {
            // タイムアウト時はビルド失敗としてスプラッシュに留まり、Profile へは遷移しない
            if (jetBrainsMonoStep != SplashStep.Done) jetBrainsMonoStep = SplashStep.Failed
            if (notoSansJpStep != SplashStep.Done) notoSansJpStep = SplashStep.Failed
            if (zenKakuGothicNewStep != SplashStep.Done) zenKakuGothicNewStep = SplashStep.Failed
            renderStep = SplashStep.Failed
            buildStatus = BuildStatus.Failed
            return@LaunchedEffect
        }

        val remainingMillis =
            SplashAnimations.MinDisplayMillis - shownAt.elapsedNow().inWholeMilliseconds
        if (remainingMillis > 0) {
            delay(remainingMillis)
        }

        renderStep = SplashStep.Done
        buildStatus = BuildStatus.Success
        delay(SplashAnimations.SuccessToExitMillis)
        currentToProfile()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        if (screenWidth < CompactWidth) {
            SplashMobileContent(
                jetBrainsMonoStep = jetBrainsMonoStep,
                notoSansJpStep = notoSansJpStep,
                zenKakuGothicNewStep = zenKakuGothicNewStep,
                renderStep = renderStep,
                buildStatus = buildStatus,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            SplashDesktopContent(
                jetBrainsMonoStep = jetBrainsMonoStep,
                notoSansJpStep = notoSansJpStep,
                zenKakuGothicNewStep = zenKakuGothicNewStep,
                renderStep = renderStep,
                buildStatus = buildStatus,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * [block] の完了を待ち、完了すれば true を返す。
 * タイムアウトは [isPageVisible] が true の間のみ進み、非表示中は停止する。
 * 再表示のたびに 0 から計り直し、表示されたまま [timeoutMillis] 経過したら
 * [block] をキャンセルして false を返す。
 */
private suspend fun awaitWithVisibleTimeout(
    isPageVisible: () -> Boolean,
    timeoutMillis: Long,
    block: suspend CoroutineScope.() -> Unit,
): Boolean = coroutineScope {
    val loadResult = CompletableDeferred<Boolean>()
    val watchdog = launch {
        snapshotFlow { isPageVisible() }.collectLatest { visible ->
            if (!visible) return@collectLatest
            delay(timeoutMillis)
            loadResult.complete(false)
        }
    }
    val work = launch {
        block()
        loadResult.complete(true)
    }
    val result = loadResult.await()
    watchdog.cancel()
    work.cancel()
    result
}
