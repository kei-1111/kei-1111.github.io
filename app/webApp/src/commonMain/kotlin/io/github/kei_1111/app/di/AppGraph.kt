package io.github.kei_1111.app.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.github.kei_1111.app.core.common.dispatcher.DispatcherBindings

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [DispatcherBindings::class],
)
interface AppGraph : ViewModelGraph
