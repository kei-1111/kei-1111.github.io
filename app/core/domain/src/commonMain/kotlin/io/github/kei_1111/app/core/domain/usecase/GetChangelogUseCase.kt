package io.github.kei_1111.app.core.domain.usecase

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.data.repository.ChangelogRepository
import io.github.kei_1111.shared.model.GitHubChangelog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface GetChangelogUseCase {
    operator fun invoke(): Flow<GitHubChangelog>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetChangelogUseCaseImpl(
    private val changelogRepository: ChangelogRepository,
) : GetChangelogUseCase {
    override fun invoke(): Flow<GitHubChangelog> =
        changelogRepository.changelog
            .distinctUntilChanged()
}
