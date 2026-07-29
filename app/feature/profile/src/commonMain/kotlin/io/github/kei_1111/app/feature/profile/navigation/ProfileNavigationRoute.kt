@file:Suppress("MatchingDeclarationName", "Filename")

package io.github.kei_1111.app.feature.profile.navigation

import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import io.github.kei_1111.app.feature.profile.model.EditorPage
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data object Profile : NavKey

@Serializable
data object SearchEverywhere : NavKey

internal data class SearchEverywhereResult(val page: EditorPage)

// wasmJs はリフレクション非対応のため、この file の NavKey をバックスタック直列化へ登録する断片を
// Metro が Set として集約する（AppGraph.navKeySerializers）。NavKey を増やしたらここにも追加する。
@BindingContainer
@ContributesTo(AppScope::class)
interface ProfileNavKeyBindings {

    companion object {
        @Provides
        @IntoSet
        fun provideProfileNavKeySerializers(): SerializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(Profile::class, Profile.serializer())
                subclass(SearchEverywhere::class, SearchEverywhere.serializer())
            }
        }
    }
}
