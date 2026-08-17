package io.github.kei_1111.app.core.mvi

import io.github.kei_1111.app.core.testing.ViewModelTestBase
import io.github.kei_1111.app.core.testing.dispatch
import io.github.kei_1111.app.core.testing.startCollecting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest : ViewModelTestBase() {

    @Test
    fun exposesInitialStateBeforeAnyCollection() = runTest {
        val viewModel = CounterViewModel()

        assertEquals(CounterState(), viewModel.state.value)
    }

    @Test
    fun keepsPublicStateAtInitialValueWithoutCollector() = runTest {
        val viewModel = CounterViewModel()

        dispatch(viewModel, CounterIntent.Increment)

        // 基盤特性の意図的な固定 (characterization): WhileSubscribed のためコレクタ不在では
        // toState 変換が走らない — collect-first 規則の根拠。共有戦略を意図して変える際は
        // このテストも一緒に変わる。feature テストの手本にはしないこと (mvi-testing.md)。
        assertEquals(0, viewModel.state.value.count)
    }

    @Test
    fun reflectsIntentDrivenUpdateIntoCollectedState() = runTest {
        val viewModel = CounterViewModel()
        val collected = mutableListOf<CounterState>()
        backgroundScope.launch { viewModel.state.toList(collected) }
        runCurrent()

        dispatch(viewModel, CounterIntent.Increment)

        assertEquals(1, viewModel.state.value.count)
        assertEquals(listOf(CounterState(count = 0), CounterState(count = 1)), collected)
    }

    @Test
    fun emitsEffectOnEmitEffect() = runTest {
        val viewModel = CounterViewModel()
        startCollecting(viewModel.state)

        dispatch(viewModel, CounterIntent.EmitEffect)

        assertEquals(CounterEffect.Notify, viewModel.state.value.effect)
    }

    @Test
    fun clearsEffectOnConsumeEffect() = runTest {
        val viewModel = CounterViewModel()
        startCollecting(viewModel.state)
        dispatch(viewModel, CounterIntent.EmitEffect)

        dispatch(viewModel, CounterIntent.ConsumeEffect)

        assertNull(viewModel.state.value.effect)
    }
}

private data class CounterViewModelState(
    val count: Int = 0,
    override val effect: CounterEffect? = null,
) : ViewModelState<CounterState, CounterEffect> {
    override fun toState() = CounterState(count = count)
}

private data class CounterState(
    val count: Int = 0,
    val effect: CounterEffect? = null,
) : State

private sealed interface CounterIntent : Intent {
    data object Increment : CounterIntent
    data object EmitEffect : CounterIntent
    data object ConsumeEffect : CounterIntent
}

private sealed interface CounterEffect {
    data object Notify : CounterEffect
}

private class CounterViewModel : MviViewModel<CounterViewModelState, CounterState, CounterIntent, CounterEffect>() {
    override fun createInitialViewModelState() = CounterViewModelState()
    override fun applyEffect(state: CounterState, effect: CounterEffect?) = state.copy(effect = effect)
    override fun clearEffect(viewModelState: CounterViewModelState) = viewModelState.copy(effect = null)

    override fun onIntent(intent: CounterIntent) {
        when (intent) {
            is CounterIntent.Increment -> updateViewModelState { copy(count = count + 1) }
            is CounterIntent.EmitEffect -> updateViewModelState { copy(effect = CounterEffect.Notify) }
            is CounterIntent.ConsumeEffect -> consumeEffect()
        }
    }
}
