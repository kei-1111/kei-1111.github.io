package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.app.core.data.issues.parseIssues
import io.github.kei_1111.app.core.data.network.API_BASE_URL
import io.github.kei_1111.app.core.data.network.fetchText
import io.github.kei_1111.shared.model.GitHubIssues
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface IssuesRepository {
    fun getIssues(): Flow<GitHubIssues>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class IssuesRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : IssuesRepository {

    private val cache = SingleFlightCache(defaultDispatcher) {
        fetchText("$API_BASE_URL/api/issues")?.let(::parseIssues)
    }

    override fun getIssues(): Flow<GitHubIssues> = flow {
        // 失敗は例外として流し、ViewModel 境界の asResult() が Result.Error に変換する。
        emit(checkNotNull(cache.get()) { "issues fetch failed" })
    }.flowOn(defaultDispatcher)
}
