package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.local.theme.ThemeLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface ThemeRepository {
    /** 保存されたテーマ選択。未保存時は初期値のダーク（true）。 */
    val isDark: Flow<Boolean>

    suspend fun saveIsDark(isDark: Boolean)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ThemeRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val themeLocalDataSource: ThemeLocalDataSource,
) : ThemeRepository {

    override val isDark: Flow<Boolean> = themeLocalDataSource.isDark.map { isDark ->
        isDark ?: DEFAULT_IS_DARK
    }.flowOn(defaultDispatcher)

    override suspend fun saveIsDark(isDark: Boolean) {
        themeLocalDataSource.saveIsDark(isDark)
    }

    private companion object {
        const val DEFAULT_IS_DARK = true
    }
}
