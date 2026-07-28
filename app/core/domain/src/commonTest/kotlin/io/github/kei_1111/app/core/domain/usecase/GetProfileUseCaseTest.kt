package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.ProfileRepository
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.LocalizedText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetProfileUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val expected = profile(handle = "kei-1111")
        val useCase = GetProfileUseCaseImpl(FakeProfileRepository(flowOf(expected)))

        val actual = useCase().toList()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val first = profile(handle = "first")
        val duplicate = profile(handle = "first")
        val second = profile(handle = "second")
        val useCase = GetProfileUseCaseImpl(FakeProfileRepository(flowOf(first, duplicate, second, first)))

        val actual = useCase().toList()

        assertEquals(listOf(first, second, first), actual)
    }
}

private class FakeProfileRepository(override val profile: Flow<GitHubProfile>) : ProfileRepository

private fun profile(handle: String) = GitHubProfile(
    name = LocalizedText(ja = "テスト", en = "Test"),
    handle = handle,
    location = "Tokyo",
    role = "Student",
    followers = 0,
    following = 0,
    repos = 0,
    totalStars = 0,
    pinnedRepos = persistentListOf(),
    languages = persistentListOf(),
    links = persistentListOf(),
)
