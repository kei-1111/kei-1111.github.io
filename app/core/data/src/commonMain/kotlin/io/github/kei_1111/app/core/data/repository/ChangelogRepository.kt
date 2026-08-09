package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.changelog.ChangelogApi
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.shared.model.GitHubChangelog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface ChangelogRepository {
    val changelog: Flow<GitHubChangelog>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ChangelogRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    changelogApi: ChangelogApi,
) : ChangelogRepository {

    // 空一覧は失敗に畳まない — サーバ側 TtlCache が空応答も成功として保持するため、
    // クライアントだけ失敗扱いにすると retry が TTL の間まったく効かないエラーに固定される。
    private val cache = SingleFlightCache(defaultDispatcher) {
        changelogApi.fetchChangelog()
    }

    override val changelog: Flow<GitHubChangelog> = flow {
        emit(checkNotNull(cache.get()) { "changelog fetch failed" })
    }.flowOn(defaultDispatcher)
}
