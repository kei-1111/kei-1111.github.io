package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.shared.model.LocalizedText

internal fun LocalizedText.forLanguage(language: KeiLanguage): String = when (language) {
    KeiLanguage.Ja -> ja
    KeiLanguage.En -> en
}
