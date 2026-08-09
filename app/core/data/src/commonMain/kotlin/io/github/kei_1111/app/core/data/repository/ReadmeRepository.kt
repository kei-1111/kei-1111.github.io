package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.readme.ReadmeApi
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.shared.model.Readme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface ReadmeRepository {
    val readme: Flow<Readme>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ReadmeRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    readmeApi: ReadmeApi,
) : ReadmeRepository {

    // 空レスポンスは fetch 内で null に畳む — 外側で検査するとキャッシュ済みの不正値が retry で回復不能になる
    private val cache = SingleFlightCache(defaultDispatcher) {
        readmeApi.fetchReadme()?.takeIf { it.ja.isNotEmpty() && it.en.isNotEmpty() }
    }

    override val readme: Flow<Readme> = flow {
        emit(checkNotNull(cache.get()) { "readme fetch failed or was empty" })
    }.flowOn(defaultDispatcher)
}
