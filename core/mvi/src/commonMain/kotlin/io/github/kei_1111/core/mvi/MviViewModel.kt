package io.github.kei_1111.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * A base ViewModel that provides state management based on the MVI pattern.
 *
 * By separating the ViewModel's internal state ([ViewModelState]) from the state exposed to the UI ([State]),
 * it hides the ViewModel's implementation details from the UI and achieves a cleaner architecture.
 *
 * ### Architecture characteristics
 * - **Separation of internal and exposed state**: ViewModelState is for internal implementation, State is for UI rendering
 * - **Unidirectional data flow**: Intent → ViewModel → State → UI
 * - **Side effect management**: controls navigation, toast display, etc. through the effect property in State
 *
 * ### Usage
 * ```kotlin
 * // ViewModelState (internal state)
 * data class ClockSettingsViewModelState(
 *     val clockSettings: ClockSettings = ClockSettings(),
 *     val initialClockSettings: ClockSettings = ClockSettings(),
 *     val effect: ClockSettingsEffect? = null,
 * ) : ViewModelState<ClockSettingsState> {
 *     override fun toState() = ClockSettingsState(
 *         clockSettings = clockSettings,
 *         isSaveButtonEnabled = clockSettings != initialClockSettings,
 *         effect = effect,
 *     )
 * }
 *
 * // State (UI state)
 * data class ClockSettingsState(
 *     val clockSettings: ClockSettings = ClockSettings(),
 *     val isSaveButtonEnabled: Boolean = false,
 *     val effect: ClockSettingsEffect? = null,
 * ) : State
 *
 * // ViewModel implementation
 * @Inject
 * @ViewModelKey
 * @ContributesIntoMap(AppScope::class, binding<ViewModel>())
 * class ClockSettingsViewModel(
 *     private val getClockSettingsUseCase: GetClockSettingsUseCase,
 *     private val saveClockSettingsUseCase: SaveClockSettingsUseCase,
 * ) : MviViewModel<ClockSettingsViewModelState, ClockSettingsState, ClockSettingsIntent>() {
 *
 *     override fun createInitialViewModelState() = ClockSettingsViewModelState()
 *     override fun createInitialState() = ClockSettingsState()
 *
 *     override fun onIntent(intent: ClockSettingsIntent) {
 *         when (intent) {
 *             is ClockSettingsIntent.SaveSettings -> saveSettings()
 *             is ClockSettingsIntent.ConsumeEffect -> updateViewModelState { copy(effect = null) }
 *         }
 *     }
 * }
 * ```
 *
 * @param VS the ViewModelState type. Must implement the [ViewModelState] interface
 * @param S the State type. Must implement the [State] interface
 * @param I the Intent type. Must implement the [Intent] interface
 */
@Suppress("VariableNaming")
abstract class MviViewModel<VS : ViewModelState<S>, S : State, I : Intent> : ViewModel() {
    /**
     * A MutableStateFlow that holds the ViewModel's internal state.
     *
     * This state contains the ViewModel's implementation details and is not exposed directly to the UI.
     * Update it using the [updateViewModelState] method.
     */
    protected val _viewModelState = MutableStateFlow<VS>(createInitialViewModelState())

    /**
     * A StateFlow of the state observed by the UI.
     *
     * Exposes the result of converting [_viewModelState] with the ViewModelState's [toState] method.
     * The UI observes this state to render the screen.
     */
    val state: StateFlow<S> = _viewModelState
        .map(ViewModelState<S>::toState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = createInitialState(),
        )

    /**
     * An abstract method that creates the initial ViewModelState value.
     *
     * Called only once when the ViewModel is initialized.
     *
     * @return the initial ViewModelState instance
     */
    protected abstract fun createInitialViewModelState(): VS

    /**
     * An abstract method that creates the initial State value.
     *
     * Used as the initial value of the [state] StateFlow.
     *
     * @return the initial State instance
     */
    protected abstract fun createInitialState(): S

    /**
     * An abstract method that handles Intents from the UI.
     *
     * Receives user interactions that occur in the UI (button clicks, text input, etc.)
     * and performs the appropriate processing.
     *
     * @param intent the Intent to handle
     */
    abstract fun onIntent(intent: I)

    /**
     * A utility function for updating the current state.
     *
     * This function passes the current state held in the `StateFlow` to the lambda received as the `update`
     * argument, generates a new state from the lambda's return value, and reflects it in the [MutableStateFlow].
     * It is suited for updating immutable data class state with `copy()`, allowing state changes to be made
     * clearly and safely.
     *
     * ### Usage
     * ```kotlin
     * updateViewModelState {
     *     copy(isModelChangeWarningDialogShown = false)
     * }
     * ```
     * The example above creates a new state with the `isModelChangeWarningDialogShown` flag changed to false,
     * and reflects it as the current state.
     *
     * ### Notes
     * - This function uses `_viewModelState.update {}` internally to update the state in a thread-safe manner.
     * - The state must be defined as a `data class` (since it uses `copy()`).
     *
     * @param update a lambda function that receives the current state [VS] and returns the new state
     */
    protected fun updateViewModelState(update: VS.() -> VS) {
        _viewModelState.update { update(it) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
