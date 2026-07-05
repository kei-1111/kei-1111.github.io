package io.github.kei_1111.core.mvi

interface ViewModelState<S : State> {
    fun toState(): S
}
