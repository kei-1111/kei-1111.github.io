package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.api.profile.ProfileApi
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.data.cache.SingleFlightCache
import io.github.kei_1111.shared.model.Profile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface ProfileRepository {
    val profile: Flow<Profile>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ProfileRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    profileApi: ProfileApi,
) : ProfileRepository {

    private val cache = SingleFlightCache(defaultDispatcher) {
        profileApi.fetchProfile()
    }

    override val profile: Flow<Profile> = flow {
        // 失敗は例外として流し、ViewModel 境界の asResult() が Result.Error に変換する。
        emit(checkNotNull(cache.get()) { "profile fetch failed" })
    }.flowOn(defaultDispatcher)
}
