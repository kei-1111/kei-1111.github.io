package io.github.kei_1111.app.feature.splash.destination.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.kei_1111.app.core.mvi.MviViewModel
import io.github.kei_1111.app.feature.splash.theme.SplashAnimations
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/**
 * スプラッシュの表示時間・フォントロード監視・Profile への遷移を一元管理する ViewModel。
 *
 * 実際に使うフォントリソースのロード完了は UI（Composable）側からしか観測できないため、
 * [SplashIntent.ReceiveFontLoaded] として通知を受け、各ログ行の完了表示と連動させる。
 *
 * 非表示タブでは rAF 停止によりリコンポジションが止まりロード完了が伝播しないため、
 * フォントロード待ちのタイムアウトはページ表示中のみ進める（[SplashIntent.UpdatePageVisibility] のハンドラ参照）。
 * 表示中に満了した場合はビルド失敗としてスプラッシュに留まる。
 *
 * 全フォントのロードが完了した後（最低表示時間の待機・成功表示から遷移までの待機）は、
 * このタイムアウト監視の対象外であり、ページの表示・非表示に関わらず影響を受けない。
 */
@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class SplashViewModel : MviViewModel<SplashViewModelState, SplashState, SplashIntent>() {

    // metroViewModel() はエントリの初回コンポジションと同じフレームで ViewModel を生成するため、
    // 旧実装の LaunchedEffect(Unit) 開始時刻とほぼ一致する
    private val shownAt = TimeSource.Monotonic.markNow()

    /** フォントロード待ちタイムアウトの監視ジョブ。ページ非表示になるたびキャンセルする。 */
    private var timeoutJob: Job? = null

    private var isPageVisible = false

    /** ロード完了済みのフォント集合。3種すべて揃うと [allFontsDone] を true にする。 */
    private val doneFonts = mutableSetOf<SplashFont>()

    /**
     * 全フォントのロードが完了した後は true。
     * true になった後はタイムアウト監視を二度と再開しない（フォント待ちフェーズのみを
     * 監視する旧 awaitWithVisibleTimeout の仕様を再現するためのガード）。
     */
    private var allFontsDone = false

    /** [SplashState.buildStatus] のミラー。Intent ハンドラ内で state を経由せず現在値を参照するために持つ。 */
    private var buildStatus = BuildStatus.Running

    override fun createInitialViewModelState() = SplashViewModelState()
    override fun createInitialState() = SplashState()

    @Suppress("CyclomaticComplexMethod", "ReturnCount", "NestedBlockDepth")
    override fun onIntent(intent: SplashIntent) {
        when (intent) {
            is SplashIntent.ReceiveFontLoaded -> {
                // ビルドが Running でなくなった後（タイムアウト失敗後）に届いた通知は無視する
                // （旧実装で LaunchedEffect が既に return し、遅れて完了しても表示が更新されないのと同じ）。
                if (buildStatus != BuildStatus.Running) return

                updateViewModelState {
                    when (intent.font) {
                        SplashFont.JetBrainsMono -> copy(jetBrainsMonoStep = SplashStep.Done)
                        SplashFont.NotoSansJp -> copy(notoSansJpStep = SplashStep.Done)
                        SplashFont.ZenKakuGothicNew -> copy(zenKakuGothicNewStep = SplashStep.Done)
                    }
                }
                doneFonts += intent.font
                if (allFontsDone || doneFonts.size < SplashFont.entries.size) return

                // 3種すべて揃った瞬間だけ成功シーケンスへ進む。以後のタイムアウト監視は永久に止める
                allFontsDone = true
                timeoutJob?.cancel()
                viewModelScope.launch {
                    val remainingMillis =
                        SplashAnimations.MinDisplayMillis - shownAt.elapsedNow().inWholeMilliseconds
                    if (remainingMillis > 0) delay(remainingMillis)

                    buildStatus = BuildStatus.Success
                    updateViewModelState {
                        copy(
                            renderStep = SplashStep.Done,
                            buildStatus = BuildStatus.Success,
                        )
                    }

                    delay(SplashAnimations.SuccessToExitMillis)
                    updateViewModelState { copy(effect = SplashEffect.NavigateProfile) }
                }
            }

            is SplashIntent.UpdatePageVisibility -> {
                // 非表示になるたび保留中のタイムアウトをキャンセルし、再表示のたびに
                // [SplashAnimations.FontLoadTimeoutMillis] を 0 から計り直す。フォントロード完了が
                // タイムアウトより先に届けばそちらが常に勝つ。ビルドが Running でなくなった後は
                // 表示状態を記録するだけで監視には影響させない。
                val shouldReschedule =
                    buildStatus == BuildStatus.Running && intent.isVisible != isPageVisible
                isPageVisible = intent.isVisible
                if (!shouldReschedule) return

                if (!intent.isVisible) {
                    timeoutJob?.cancel()
                    return
                }
                // 全フォント読み込み済みなら、表示に戻ってもタイムアウト監視は再開しない
                if (allFontsDone) return

                timeoutJob?.cancel()
                timeoutJob = viewModelScope.launch {
                    delay(SplashAnimations.FontLoadTimeoutMillis)
                    // タイムアウト時はビルド失敗としてスプラッシュに留まり、Profile へは遷移しない。
                    // フォント読み込み完了と競合した場合は成功シーケンス側を常に優先する。
                    if (allFontsDone) return@launch

                    buildStatus = BuildStatus.Failed
                    updateViewModelState {
                        copy(
                            jetBrainsMonoStep =
                            if (SplashFont.JetBrainsMono in doneFonts) jetBrainsMonoStep else SplashStep.Failed,
                            notoSansJpStep =
                            if (SplashFont.NotoSansJp in doneFonts) notoSansJpStep else SplashStep.Failed,
                            zenKakuGothicNewStep =
                            if (SplashFont.ZenKakuGothicNew in doneFonts) zenKakuGothicNewStep else SplashStep.Failed,
                            renderStep = SplashStep.Failed,
                            buildStatus = BuildStatus.Failed,
                        )
                    }
                }
            }

            is SplashIntent.ConsumeEffect -> updateViewModelState { copy(effect = null) }
        }
    }
}
