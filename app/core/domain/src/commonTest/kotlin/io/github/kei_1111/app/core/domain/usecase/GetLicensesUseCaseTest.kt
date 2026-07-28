package io.github.kei_1111.app.core.domain.usecase

import io.github.kei_1111.app.core.data.repository.LicensesRepository
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.LicenseType
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLicensesUseCaseTest {

    @Test
    fun forwardsTheRepositoryFlow() = runTest {
        val expected = licenses(entryName = "kotlin")
        val useCase = GetLicensesUseCaseImpl(FakeLicensesRepository(flowOf(expected)))

        val actual = useCase().toList()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun collapsesConsecutiveDuplicateEmissions() = runTest {
        val first = licenses(entryName = "kotlin")
        val duplicate = licenses(entryName = "kotlin")
        val second = licenses(entryName = "compose")
        val useCase = GetLicensesUseCaseImpl(FakeLicensesRepository(flowOf(first, duplicate, second, first)))

        val actual = useCase().toList()

        assertEquals(listOf(first, second, first), actual)
    }
}

private class FakeLicensesRepository(override val licenses: Flow<ThirdPartyLicenses>) : LicensesRepository

private fun licenses(entryName: String) = ThirdPartyLicenses(
    icons = persistentListOf(),
    fonts = persistentListOf(),
    app = persistentListOf(
        LicenseEntry(
            name = entryName,
            owner = "kei-1111",
            type = LicenseType.Apache20,
            url = "https://example.com",
            copyright = "Copyright kei-1111",
        ),
    ),
    server = persistentListOf(),
    texts = persistentMapOf(),
)
