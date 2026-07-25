package io.github.kei_1111.app.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.github.kei_1111.app.core.common.dispatcher.DispatcherBindings
import io.github.kei_1111.app.core.data.repository.ThemeRepository

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [DispatcherBindings::class],
)
interface AppGraph : ViewModelGraph {
    val themeRepository: ThemeRepository
}
