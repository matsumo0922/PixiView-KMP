package me.matsumo.fanbox.core.ui.ads

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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

    fun completeByDismissal(): Boolean? = complete(isRewardEarned.load())

    fun completeWithoutReward(): Boolean? = complete(isRewardEarned = false)

    private fun complete(isRewardEarned: Boolean): Boolean? = if (isCompleted.compareAndSet(false, true)) {
        isRewardEarned
    } else {
        null
    }
}
