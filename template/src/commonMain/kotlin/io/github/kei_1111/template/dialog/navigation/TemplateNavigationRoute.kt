@file:Suppress("MatchingDeclarationName", "Filename")

package io.github.kei_1111.template.dialog.navigation

import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data object Golden : NavKey

// PLACEHOLDER: Result payload, when needed — internal data class GoldenResult(...)

@BindingContainer
@ContributesTo(AppScope::class)
interface TemplateNavKeyBindings {

    companion object {
        @Provides
        @IntoSet
        fun provideTemplateNavKeySerializers(): SerializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(Golden::class, Golden.serializer())
            }
        }
    }
}
