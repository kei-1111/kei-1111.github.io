package io.github.kei_1111.app.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kei_1111.app.core.common.result.Result
import io.github.kei_1111.app.core.common.result.asResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<VS : ViewModelState<S, E>, S : State, I : Intent, E> : ViewModel() {
    private val _viewModelState = MutableStateFlow<VS>(createInitialViewModelState())
    protected val viewModelState: StateFlow<VS> = _viewModelState.asStateFlow()

    val state: StateFlow<S> = _viewModelState
        .map { applyEffect(it.toState(), it.effect) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = _viewModelState.value.let { applyEffect(it.toState(), it.effect) },
        )

    protected abstract fun createInitialViewModelState(): VS

    protected abstract fun applyEffect(state: S, effect: E?): S

    protected abstract fun clearEffect(viewModelState: VS): VS

    abstract fun onIntent(intent: I)

    protected fun consumeEffect() {
        updateViewModelState { clearEffect(this) }
    }

    protected fun updateViewModelState(update: VS.() -> VS) {
        _viewModelState.update { update(it) }
    }

    protected fun <T> Flow<T>.collectAsResult(reduce: VS.(Result<T>) -> VS): Job =
        viewModelScope.launch {
            asResult().collect { result -> updateViewModelState { reduce(result) } }
        }

    protected fun Flow<*>.prefetchAsResult(): Job = asResult().launchIn(viewModelScope)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
