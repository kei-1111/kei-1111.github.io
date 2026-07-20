package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model

import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.LinkService

internal enum class SearchEverywhereTab(val label: String) {
    All("All"),
    Files("Files"),
    Links("Links"),
    Actions("Actions"),
}

internal sealed interface SearchEverywhereEntry {
    val name: String
    val detail: String
    val categoryLabel: String

    data class Page(val page: EditorPage) : SearchEverywhereEntry {
        override val name = page.fileName
        override val detail = page.breadcrumb
        override val categoryLabel = "File"
    }

    data class Link(val service: LinkService) : SearchEverywhereEntry {
        override val name = service.name
        override val detail = service.url
        override val categoryLabel = "Link"
    }

    data object SwitchTheme : SearchEverywhereEntry {
        override val name = "Switch Theme"
        override val detail = ""
        override val categoryLabel = "Action"
    }
}
