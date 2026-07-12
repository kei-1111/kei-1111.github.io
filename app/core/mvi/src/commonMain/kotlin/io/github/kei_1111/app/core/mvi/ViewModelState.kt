package io.github.kei_1111.app.core.mvi

interface ViewModelState<S : State> {
    fun toState(): S
}
