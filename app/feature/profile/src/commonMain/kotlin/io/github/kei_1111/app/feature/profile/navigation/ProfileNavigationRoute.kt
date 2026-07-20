@file:Suppress("MatchingDeclarationName", "Filename")

package io.github.kei_1111.app.feature.profile.navigation

import androidx.navigation3.runtime.NavKey
import io.github.kei_1111.app.feature.profile.model.EditorPage
import kotlinx.serialization.Serializable

@Serializable
data object Profile : NavKey

@Serializable
data object SearchEverywhere : NavKey

internal data class SearchEverywhereResult(val page: EditorPage)
