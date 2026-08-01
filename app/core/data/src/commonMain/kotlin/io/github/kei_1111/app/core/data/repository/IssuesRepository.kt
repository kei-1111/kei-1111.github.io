package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.issues.IssuesApi
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.shared.model.GitHubIssues
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface IssuesRepository {
    val issues: Flow<GitHubIssues>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class IssuesRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    issuesApi: IssuesApi,
) : IssuesRepository {

    private val cache = SingleFlightCache(defaultDispatcher) {
        issuesApi.fetchIssues()
    }

    override val issues: Flow<GitHubIssues> = flow {
        // 失敗は例外として流し、ViewModel 境界の asResult() が Result.Error に変換する。
        emit(checkNotNull(cache.get()) { "issues fetch failed" })
    }.flowOn(defaultDispatcher)
}
