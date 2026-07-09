package me.matsumo.fanbox.core.ui.ads

import androidx.compose.runtime.Immutable
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/** リワード広告の表示完了結果。 */
@Immutable
data class RewardAdShowResult(
    val requestId: Long,
    val isRewardEarned: Boolean,
)

/** リワード広告の表示完了結果を表示要求 ID に基づいて消費する。 */
fun handleRewardAdShowResult(
    showResult: RewardAdShowResult?,
    activeRequestId: Long?,
    consumeShowResult: (RewardAdShowResult) -> Unit,
    onActiveResultConsumed: (Boolean) -> Unit,
) {
    if (showResult == null) return

    val isActiveResult = showResult.requestId == activeRequestId

    consumeShowResult(showResult)

    if (isActiveResult) {
        onActiveResultConsumed(showResult.isRewardEarned)
    }
}

/** リワード広告の表示完了結果を一度だけ確定する状態管理クラス。 */
@OptIn(ExperimentalAtomicApi::class)
internal class RewardAdShowState {
    private val isCompleted = AtomicBoolean(false)
    private val isRewardEarned = AtomicBoolean(false)

    fun recordRewardEarned() {
        if (!isCompleted.load()) {
            isRewardEarned.store(true)
        }
    }

    fun completeByDismissal(): Boolean? {
        return complete(isRewardEarned.load())
    }

    fun completeWithoutReward(): Boolean? {
        return complete(isRewardEarned = false)
    }

    private fun complete(isRewardEarned: Boolean): Boolean? {
        return if (isCompleted.compareAndSet(false, true)) {
            isRewardEarned
        } else {
            null
        }
    }
}
