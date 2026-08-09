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

    private val cache = SingleFlightCache(defaultDispatcher) {
        worksApi.fetchWorks()
    }

    override val works: Flow<Works> = flow {
        // 失敗は例外として流し、ViewModel 境界の asResult() が Result.Error に変換する。
        // 空一覧はポートフォリオの静的契約上ありえないため、UI に空カードを渡さず失敗として扱う。
        val works = checkNotNull(cache.get()) { "works fetch failed" }
        check(works.items.isNotEmpty()) { "works response was empty" }
        emit(works)
    }.flowOn(defaultDispatcher)
}
