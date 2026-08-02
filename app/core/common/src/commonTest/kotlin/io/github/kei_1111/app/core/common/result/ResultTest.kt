package io.github.kei_1111.app.core.common.result

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResultTest {

    @Test
    fun successOrNullReturnsDataOnSuccess() {
        val result: Result<String> = Result.Success("data")

        assertEquals("data", result.successOrNull)
    }

    @Test
    fun successOrNullReturnsNullOnError() {
        val result: Result<String> = Result.Error(IllegalStateException("boom"))

        assertNull(result.successOrNull)
    }

    @Test
    fun successOrNullReturnsNullOnLoading() {
        val result: Result<String> = Result.Loading

        assertNull(result.successOrNull)
    }
}
