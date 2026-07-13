package io.github.kei_1111.server.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// TtlCache は TimeSource.Monotonic(実時計)を使うため、仮想時間ではなく短い TTL と実時間の待機で検証する。
private const val SHORT_TTL_MILLIS = 100L
private const val LONG_TTL_MILLIS = 10_000L
private const val LONG_RETRY_INTERVAL_MILLIS = 10_000L
private const val SHORT_RETRY_INTERVAL_MILLIS = 50L
private const val PAST_TTL_MILLIS = 150L

class TtlCacheTest {

    @Test
    fun returnsCachedValueWithinTtlWithoutRefetching() = runBlocking {
        val cache = TtlCache<String>(LONG_TTL_MILLIS)
        val fetchCount = AtomicInteger()

        val first = cache.get {
            fetchCount.incrementAndGet()
            "value"
        }
        val second = cache.get {
            fetchCount.incrementAndGet()
            "value"
        }

        assertEquals("value", first)
        assertEquals("value", second)
        assertEquals(1, fetchCount.get())
    }

    @Test
    fun refetchesAfterTtlExpires() = runBlocking {
        val cache = TtlCache<String>(SHORT_TTL_MILLIS)
        val fetchCount = AtomicInteger()

        val first = cache.get { "value${fetchCount.incrementAndGet()}" }
        delay(PAST_TTL_MILLIS)
        val second = cache.get { "value${fetchCount.incrementAndGet()}" }

        assertEquals("value1", first)
        assertEquals("value2", second)
        assertEquals(2, fetchCount.get())
    }

    @Test
    fun refetchesAfterTtlEvenWhenTheRetryIntervalIsLongerThanTheTtl() = runBlocking {
        // 抑止は失敗にのみ効くので、成功後の TTL 失効時は retryInterval の長さに関わらず再取得する。
        val cache = TtlCache<String>(SHORT_TTL_MILLIS, retryIntervalMillis = LONG_RETRY_INTERVAL_MILLIS)
        val fetchCount = AtomicInteger()

        val first = cache.get { "value${fetchCount.incrementAndGet()}" }
        delay(PAST_TTL_MILLIS)
        val second = cache.get { "value${fetchCount.incrementAndGet()}" }

        assertEquals("value1", first)
        assertEquals("value2", second)
        assertEquals(2, fetchCount.get())
    }

    @Test
    fun coalescesConcurrentGetsIntoASingleFetch() = runBlocking {
        val cache = TtlCache<String>(LONG_TTL_MILLIS)
        val fetchCount = AtomicInteger()

        val results = coroutineScope {
            List(16) {
                async {
                    cache.get {
                        fetchCount.incrementAndGet()
                        delay(20)
                        "value"
                    }
                }
            }.awaitAll()
        }

        assertEquals(List(16) { "value" }, results)
        assertEquals(1, fetchCount.get())
    }

    @Test
    fun servesStaleValueWhenRefetchFails() = runBlocking {
        val cache = TtlCache<String>(SHORT_TTL_MILLIS)

        val fresh = cache.get { "fresh" }
        delay(PAST_TTL_MILLIS)
        val stale = cache.get { null }

        assertEquals("fresh", fresh)
        assertEquals("fresh", stale)
    }

    @Test
    fun suppressesRetriesWithinTheRetryIntervalAfterAFailure() = runBlocking {
        val cache = TtlCache<String>(SHORT_TTL_MILLIS, retryIntervalMillis = LONG_RETRY_INTERVAL_MILLIS)
        val fetchCount = AtomicInteger()

        val first = cache.get {
            fetchCount.incrementAndGet()
            null
        }
        val second = cache.get {
            fetchCount.incrementAndGet()
            null
        }
        val third = cache.get {
            fetchCount.incrementAndGet()
            null
        }

        assertNull(first)
        assertNull(second)
        assertNull(third)
        // 失敗直後は retryInterval の間だけ再取得を抑止するため、fetch は1回しか走らない。
        assertEquals(1, fetchCount.get())
    }

    @Test
    fun suppressedRetryStillServesTheStaleValue() = runBlocking {
        val cache = TtlCache<String>(SHORT_TTL_MILLIS, retryIntervalMillis = SHORT_RETRY_INTERVAL_MILLIS)
        val fetchCount = AtomicInteger()

        val fresh = cache.get {
            fetchCount.incrementAndGet()
            "fresh"
        }
        delay(PAST_TTL_MILLIS)
        val stale = cache.get {
            fetchCount.incrementAndGet()
            null
        }
        val suppressed = cache.get {
            fetchCount.incrementAndGet()
            null
        }

        assertEquals("fresh", fresh)
        assertEquals("fresh", stale)
        assertEquals("fresh", suppressed)
        // 初回成功 + 失効後の失敗のみ。直後の3回目は retryInterval 内なので抑止され、stale が返る。
        assertEquals(2, fetchCount.get())
    }
}
