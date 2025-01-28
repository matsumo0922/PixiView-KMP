package me.matsumo.fanbox.core.common.util

import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (exception: Throwable) {
    recordException(exception)
    Napier.i(exception) { "Failed to evaluate a suspendRunCatchingBlock. Returning failure Result" }
    Result.failure(exception)
}

suspend fun waitFor(flagChecker: () -> Boolean) {
    while (!flagChecker()) {
        coroutineContext.ensureActive()
        delay(100)
    }
}
