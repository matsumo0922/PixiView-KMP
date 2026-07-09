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

class RewardRepositoryImpl(
    private val rewardLogDataStore: RewardLogDataStore,
    private val ioDispatcher: CoroutineDispatcher,
) : RewardRepository {

    override suspend fun rewarded(usage: RewardUsage) {
        withContext(ioDispatcher) {
            resetIfNeeded()
            rewardLogDataStore.rewarded(usage)
        }
    }

    override suspend fun isAbleToReward(usage: RewardUsage): Boolean {
        return withContext(ioDispatcher) {
            resetIfNeeded()
            rewardLogDataStore.getRewardedCount(usage) < usage.dailyLimit
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun resetIfNeeded() {
        val date = Clock.System.now().format("yyyy-MM-dd")
        val lastRewardDate = rewardLogDataStore.getRewardDate()

        if (lastRewardDate != date) {
            rewardLogDataStore.reset()
        }
    }
}
