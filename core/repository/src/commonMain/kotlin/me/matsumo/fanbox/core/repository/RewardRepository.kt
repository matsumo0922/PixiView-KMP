package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.datastore.RewardLogDataStore
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface RewardRepository {
    fun rewarded()
    suspend fun isAbleToReward(): Boolean
}

class RewardRepositoryImpl(
    private val rewardLogDataStore: RewardLogDataStore,
    private val ioDispatcher: CoroutineDispatcher,
) : RewardRepository {

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    init {
        scope.launch {
            resetIfNeeded()
        }
    }

    override fun rewarded() {
        scope.launch {
            rewardLogDataStore.rewarded()
        }
    }

    override suspend fun isAbleToReward(): Boolean {
        resetIfNeeded()
        return rewardLogDataStore.getRewardedCount() < MAX_REWARD_COUNT
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun resetIfNeeded() {
        val date = Clock.System.now().format("yyyy-MM-dd")
        val lastRewardDate = rewardLogDataStore.getRewardDate()

        if (lastRewardDate != date) {
            rewardLogDataStore.reset()
        }
    }

    companion object {
        const val MAX_REWARD_COUNT = 1
    }
}
