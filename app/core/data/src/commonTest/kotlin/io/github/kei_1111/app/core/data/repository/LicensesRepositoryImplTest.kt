package io.github.kei_1111.app.core.data.repository

import io.github.kei_1111.app.core.data.license.LicenseContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LicensesRepositoryImplTest {

    @Test
    fun emitsTheBundledLicenses() = runTest {
        val repository = LicensesRepositoryImpl()

        val actual = repository.licenses.first()

        assertEquals(LicenseContent.licenses, actual)
    }
}
