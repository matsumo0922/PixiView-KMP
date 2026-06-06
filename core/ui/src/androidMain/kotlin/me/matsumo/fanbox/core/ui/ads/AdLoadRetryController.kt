package me.matsumo.fanbox.core.ui.ads

import android.os.Handler
import android.os.Looper
import io.github.aakira.napier.Napier

/** 広告ロード失敗時の指数バックオフ再試行を管理するクラス。 */
internal class AdLoadRetryController(
    private val adFormatName: String,
) {
    private val handler = Handler(Looper.getMainLooper())
    private val retryRunnable = Runnable { executePendingRetry() }
    private var retryAttemptCount = 0
    private var pendingRetryAction: (() -> Unit)? = null

    fun reset() {
        retryAttemptCount = 0
        pendingRetryAction = null
        handler.removeCallbacks(retryRunnable)
    }

    fun scheduleRetry(
        failureMessage: String,
        retryAction: () -> Unit,
    ) {
        if (retryAttemptCount >= AD_LOAD_RETRY_MAX_ATTEMPTS) {
            Napier.w { "$adFormatName: ad load retry limit reached. $failureMessage" }
            pendingRetryAction = null
            handler.removeCallbacks(retryRunnable)
            return
        }

        retryAttemptCount += 1
        pendingRetryAction = retryAction
        handler.removeCallbacks(retryRunnable)

        val retryDelayMillis = calculateRetryDelayMillis(retryAttemptCount = retryAttemptCount)
        handler.postDelayed(retryRunnable, retryDelayMillis)

        Napier.w {
            "$adFormatName: ad load failed. " +
                "retry=$retryAttemptCount/$AD_LOAD_RETRY_MAX_ATTEMPTS, " +
                "delayMillis=$retryDelayMillis, " +
                failureMessage
        }
    }

    private fun executePendingRetry() {
        val retryAction = pendingRetryAction ?: return
        pendingRetryAction = null
        retryAction.invoke()
    }

    private fun calculateRetryDelayMillis(retryAttemptCount: Int): Long {
        val retryDelayMultiplier = 1L shl (retryAttemptCount - 1)
        return (AD_LOAD_RETRY_INITIAL_DELAY_MILLIS * retryDelayMultiplier)
            .coerceAtMost(AD_LOAD_RETRY_MAX_DELAY_MILLIS)
    }
}

/** 広告ロード再試行の最大回数。 */
private const val AD_LOAD_RETRY_MAX_ATTEMPTS = 5

/** 広告ロード再試行の初期待機時間。 */
private const val AD_LOAD_RETRY_INITIAL_DELAY_MILLIS = 1_000L

/** 広告ロード再試行の最大待機時間。 */
private const val AD_LOAD_RETRY_MAX_DELAY_MILLIS = 32_000L
