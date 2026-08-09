package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.app.core.designsystem.language.KeiLanguage
import io.github.kei_1111.shared.model.MarkdownBlock
import io.github.kei_1111.shared.model.Readme
import kotlinx.collections.immutable.ImmutableList

internal fun Readme.blocksFor(language: KeiLanguage): ImmutableList<MarkdownBlock> = when (language) {
    KeiLanguage.Ja -> ja
    KeiLanguage.En -> en
}
