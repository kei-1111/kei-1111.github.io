package io.github.kei_1111.app.core.mvi

interface ViewModelState<S : State, E> {
    val effect: E?
    fun toState(): S
}
