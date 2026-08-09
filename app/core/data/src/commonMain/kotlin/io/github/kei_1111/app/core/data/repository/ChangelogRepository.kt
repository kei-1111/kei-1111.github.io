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

    // main 直 push 禁止の本リポジトリではマージ済み PR ゼロはあり得ないため、空一覧は fetch 失敗として扱う。
    // 空レスポンスは fetch 内で null に畳む — 外側で検査するとキャッシュ済みの不正値が retry で回復不能になる
    private val cache = SingleFlightCache(defaultDispatcher) {
        changelogApi.fetchChangelog()?.takeIf { it.pullRequests.isNotEmpty() }
    }

    override val changelog: Flow<GitHubChangelog> = flow {
        emit(checkNotNull(cache.get()) { "changelog fetch failed or was empty" })
    }.flowOn(defaultDispatcher)
}
