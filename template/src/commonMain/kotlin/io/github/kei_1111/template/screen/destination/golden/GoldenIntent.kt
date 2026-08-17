package io.github.kei_1111.template.screen.destination.golden

import io.github.kei_1111.app.core.mvi.Intent

internal sealed interface GoldenIntent : Intent {
    // PLACEHOLDER: this destination's intents — intent-based names (Update{Target} / Toggle{Target} / Receive{Target} / Open{Target})
    data object ConsumeEffect : GoldenIntent
}
