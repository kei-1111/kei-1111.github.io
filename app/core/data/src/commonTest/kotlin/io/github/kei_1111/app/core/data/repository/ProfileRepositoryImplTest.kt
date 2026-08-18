package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.api.profile.ProfileApi
import io.github.kei_1111.shared.model.LocalizedText
import io.github.kei_1111.shared.model.Profile
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryImplTest {

    @Test
    fun emitsTheFetchedValue() = runTest {
        val expected = profile()
        val repository = ProfileRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            profileApi = FakeProfileApi(expected),
        )

        val actual = repository.profile.first()

        assertEquals(expected, actual)
    }

    @Test
    fun throwsWhenTheFetchFails() = runTest {
        val repository = ProfileRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            profileApi = FakeProfileApi(null),
        )

        assertFailsWith<IllegalStateException> {
            repository.profile.first()
        }
    }

    @Test
    fun sharesOneFetchAcrossCollections() = runTest {
        val api = FakeProfileApi(profile())
        val repository = ProfileRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            profileApi = api,
        )

        repository.profile.first()
        repository.profile.first()

        assertEquals(1, api.callCount)
    }
}

private class FakeProfileApi(
    private val result: Profile?,
) : ProfileApi {
    var callCount = 0

    override suspend fun fetchProfile(): Profile? {
        callCount += 1
        return result
    }
}

private fun profile() = Profile(
    name = LocalizedText(ja = "テスト", en = "Test"),
    handle = "kei-1111",
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
