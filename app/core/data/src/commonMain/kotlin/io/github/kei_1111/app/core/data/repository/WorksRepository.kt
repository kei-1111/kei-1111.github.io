package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.works.WorksApi
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.shared.model.Works
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface WorksRepository {
    val works: Flow<Works>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class WorksRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    worksApi: WorksApi,
) : WorksRepository {

    // 空レスポンスは fetch 内で null に畳む — 外側で検査するとキャッシュ済みの不正値が retry で回復不能になる
    private val cache = SingleFlightCache(defaultDispatcher) {
        worksApi.fetchWorks()?.takeIf { it.items.isNotEmpty() }
    }

    override val works: Flow<Works> = flow {
        emit(checkNotNull(cache.get()) { "works fetch failed or was empty" })
    }.flowOn(defaultDispatcher)
}
