package io.github.kei_1111.app.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.github.kei_1111.app.core.common.dispatcher.DispatcherBindings
import io.github.kei_1111.app.core.common.logging.InteractionLog
import io.github.kei_1111.app.core.data.repository.ThemeRepository
import kotlinx.serialization.modules.SerializersModule

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [DispatcherBindings::class],
)
interface AppGraph : ViewModelGraph {
    val themeRepository: ThemeRepository
    val interactionLog: InteractionLog

    /** 各 feature が @IntoSet で提供する NavKey 直列化断片（AppNavDisplay が統合する）。 */
    val navKeySerializers: Set<SerializersModule>
}
