package io.github.kei_1111.app.core.common.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RetryTest {

    @Test
    fun returnsFirstSuccessWithoutDelay() = runTest {
        var calls = 0

        val result = retryWithBackoff {
            calls++
            "value"
        }

        assertEquals("value", result)
        assertEquals(1, calls)
    }

    @Test
    fun retriesWithGrowingBackoffUntilSuccess() = runTest {
        var calls = 0

        val result = retryWithBackoff {
            calls++
            if (calls < 3) error("boom")
            "value"
        }

        assertEquals("value", result)
        assertEquals(3, calls)
        assertEquals(3_000L, currentTime)
    }

    @Test
    fun capsBackoffDelayAtMaxDelay() = runTest {
        var calls = 0
        val job = launch {
            retryWithBackoff {
                calls++
                error("boom")
            }
        }
        runCurrent()

        assertEquals(1, calls)

        listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 16_000L).forEachIndexed { index, delayMillis ->
            advanceTimeBy(delayMillis)
            runCurrent()

            assertEquals(index + 2, calls)
        }

        job.cancelAndJoin()
    }

    @Test
    fun treatsNullResultAsSuccess() = runTest {
        var calls = 0

        val result = retryWithBackoff<String?> {
            calls++
            null
        }

        assertNull(result)
        assertEquals(1, calls)
    }

    @Test
    fun propagatesCancellationWhileWaiting() = runTest {
        var calls = 0
        val job = launch {
            retryWithBackoff {
                calls++
                error("boom")
            }
        }
        runCurrent()
        advanceTimeBy(500)

        job.cancelAndJoin()
        advanceTimeBy(1_000)
        runCurrent()

        assertTrue(job.isCancelled)
        assertEquals(1, calls)
    }
}
