package io.github.kei_1111.app.core.common.result

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AsResultTest {

    @Test
    fun prependsLoadingBeforeTheFirstEmission() = runTest {
        val results = flowOf(1, 2).asResult().toList()

        assertEquals(listOf(Result.Loading, Result.Success(1), Result.Success(2)), results)
    }

    @Test
    fun emitsLoadingEvenWhenTheFlowIsEmpty() = runTest {
        assertEquals(listOf<Result<Int>>(Result.Loading), flowOf<Int>().asResult().toList())
    }

    @Test
    fun catchesUpstreamFailuresIntoError() = runTest {
        val boom = IllegalStateException("boom")
        val results = flow<Int> {
            emit(1)
            throw boom
        }.asResult().toList()

        assertEquals(Result.Loading, results[0])
        assertEquals(Result.Success(1), results[1])
        assertEquals(boom, assertIs<Result.Error>(results[2]).exception)
    }
}
