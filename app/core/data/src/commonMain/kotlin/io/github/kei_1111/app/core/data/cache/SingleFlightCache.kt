package io.github.kei_1111.app.core.data.cache

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * セッション寿命の in-memory single-flight キャッシュ。
 * fetch は cache 自身の scope で走るため、待機側（ViewModel の collect）がキャンセルされても継続する。
 * 成功結果のみキャッシュし、失敗（null または例外）は次回の get() で再試行する。
 */
internal class SingleFlightCache<T : Any>(
    dispatcher: CoroutineDispatcher,
    private val fetch: suspend () -> T?,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()
    private var cached: T? = null
    private var inFlight: Deferred<T?>? = null

    suspend fun get(): T? {
        val deferred = mutex.withLock {
            cached?.let { return it }
            inFlight ?: startFetch().also { inFlight = it }
        }
        return deferred.await()
    }

    private fun startFetch(): Deferred<T?> = scope.async {
        // fetch の throw も null（失敗）と同列に扱い、次回の get() で再試行できるようにする。
        // CancellationException は本物のキャンセル（scope 停止）のみ ensureActive が伝播し、
        // fetch 内発のもの（将来 withTimeout 等が入った場合）は失敗扱いにして再試行を保証する。
        val result = try {
            fetch()
        } catch (_: CancellationException) {
            ensureActive()
            null
        } catch (_: Throwable) {
            null
        }
        mutex.withLock {
            if (result != null) cached = result
            inFlight = null
        }
        result
    }
}
