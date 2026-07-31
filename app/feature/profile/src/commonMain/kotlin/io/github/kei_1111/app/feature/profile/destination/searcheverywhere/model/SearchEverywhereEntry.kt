package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model

import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.SearchEverywhereEffect
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.app.feature.profile.model.testTagKey
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

internal val SearchEverywhereEntry.testTagKey: String
    get() = when (this) {
        is SearchEverywhereEntry.Page -> page.testTagKey
        // type ではなく name 由来にする — 同一 type のリンクが複数並んでも DOM id が一意に保たれる
        is SearchEverywhereEntry.Link -> service.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        SearchEverywhereEntry.SwitchTheme -> "switch-theme"
    }

internal fun SearchEverywhereEntry.toEffect(): SearchEverywhereEffect = when (this) {
    is SearchEverywhereEntry.Page -> SearchEverywhereEffect.ReturnPage(page)
    is SearchEverywhereEntry.Link -> SearchEverywhereEffect.OpenUrl(service.url)
    SearchEverywhereEntry.SwitchTheme -> SearchEverywhereEffect.ToggleTheme
}
