package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.preview

import io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model.SearchEverywhereEntry
import io.github.kei_1111.app.feature.profile.model.EditorPage
import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal val PreviewSearchEntries: ImmutableList<SearchEverywhereEntry> = persistentListOf(
    SearchEverywhereEntry.Page(EditorPage.Readme),
    SearchEverywhereEntry.Page(EditorPage.Profile),
    SearchEverywhereEntry.Page(EditorPage.Licenses),
    SearchEverywhereEntry.Link(
        LinkService(type = LinkServiceType.GitHub, name = "GitHub", url = "https://github.com/kei-1111"),
    ),
    SearchEverywhereEntry.Link(
        LinkService(type = LinkServiceType.X, name = "X", url = "https://x.com/kei_1111"),
    ),
    SearchEverywhereEntry.Link(
        LinkService(type = LinkServiceType.Qiita, name = "Qiita", url = "https://qiita.com/kei-1111"),
    ),
    SearchEverywhereEntry.SwitchTheme,
)
