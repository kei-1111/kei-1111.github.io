package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.local.language.LanguageLocalDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageRepositoryImplTest {

    @Test
    fun passesThroughSavedValueFromTheDataSource() = runTest {
        val repository = LanguageRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            languageLocalDataSource = FakeLanguageLocalDataSource(flowOf("en")),
        )

        val languageTag = repository.languageTag.first()

        assertEquals("en", languageTag)
    }

    @Test
    fun keepsNothingStoredAsNullInsteadOfFallingBackToADefault() = runTest {
        val repository = LanguageRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            languageLocalDataSource = FakeLanguageLocalDataSource(flowOf(null)),
        )

        val languageTag = repository.languageTag.first()

        assertNull(languageTag)
    }

    @Test
    fun delegatesSavingToTheDataSource() = runTest {
        val dataSource = FakeLanguageLocalDataSource(flowOf(null))
        val repository = LanguageRepositoryImpl(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            languageLocalDataSource = dataSource,
        )

        repository.saveLanguageTag("en")

        assertEquals(listOf("en"), dataSource.savedValues)
    }
}

private class FakeLanguageLocalDataSource(
    override val languageTag: Flow<String?>,
) : LanguageLocalDataSource {
    val savedValues = mutableListOf<String>()

    override suspend fun saveLanguageTag(languageTag: String) {
        savedValues += languageTag
    }
}
