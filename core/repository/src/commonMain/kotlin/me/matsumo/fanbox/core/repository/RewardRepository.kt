package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.datastore.RewardLogDataStore
import me.matsumo.fanbox.core.model.RewardUsage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** リワード広告による一時機能解放の利用状況を扱う Repository。 */
interface RewardRepository {
    suspend fun rewarded(usage: RewardUsage)
    suspend fun isAbleToReward(usage: RewardUsage): Boolean
}

@OptIn(ExperimentalTime::class)
class RewardRepositoryImpl(
    private val rewardLogDataStore: RewardLogDataStore,
    private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock = Clock.System,
) : RewardRepository {

    override suspend fun rewarded(usage: RewardUsage) {
        withContext(ioDispatcher) {
            val date = currentDate()

            resetIfNeeded(date)
            rewardLogDataStore.rewarded(usage)
        }
    }

    override suspend fun isAbleToReward(usage: RewardUsage): Boolean {
        return withContext(ioDispatcher) {
            resetIfNeeded(currentDate())
            rewardLogDataStore.getRewardedCount(usage) < usage.dailyLimit
        }
    }

    private suspend fun resetIfNeeded(date: String) {
        val lastRewardDate = rewardLogDataStore.getRewardDate()

        if (lastRewardDate != date) {
            rewardLogDataStore.reset()
            rewardLogDataStore.setRewardDate(date)
        }
    }

    private fun currentDate(): String {
        return clock.now().format("yyyy-MM-dd")
    }
}
