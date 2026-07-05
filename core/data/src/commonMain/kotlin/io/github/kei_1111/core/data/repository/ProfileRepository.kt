package io.github.kei_1111.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.core.model.GitHubProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ProfileRepository {
    val profile: Flow<GitHubProfile>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ProfileRepositoryImpl : ProfileRepository {
    override val profile: Flow<GitHubProfile> = flowOf(DefaultGitHubProfile)
}
