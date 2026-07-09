package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.model.RewardUsage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** リワード広告の用途別視聴履歴を保存する DataStore。 */
@OptIn(ExperimentalTime::class)
class RewardLogDataStore(
    private val preferenceHelper: PreferenceHelper,
    private val clock: Clock = Clock.System,
) {
    private val preference = preferenceHelper.create(PreferencesName.REWARD_LOG)

    suspend fun rewarded(usage: RewardUsage) {
        preference.edit {
            val countKey = rewardedCountKey(usage)

            it[countKey] = (it[countKey] ?: 0) + 1
            it[stringPreferencesKey(KEY_REWARD_DATE)] = currentDate()
        }
    }

    suspend fun reset() {
        preference.edit {
            it[intPreferencesKey(KEY_REWARDED_COUNT)] = 0
            it[stringPreferencesKey(KEY_REWARD_DATE)] = currentDate()

            RewardUsage.entries.forEach { usage ->
                it[rewardedCountKey(usage)] = 0
            }
        }
    }

    suspend fun getRewardedCount(usage: RewardUsage): Int {
        return preference.data.map { it[rewardedCountKey(usage)] ?: 0 }.first()
    }

    suspend fun getRewardDate(): String? {
        return preference.data.map { it[stringPreferencesKey(KEY_REWARD_DATE)] }.first()
    }

    private fun rewardedCountKey(usage: RewardUsage) = intPreferencesKey("${KEY_REWARDED_COUNT}_${usage.storageKey}")

    private fun currentDate(): String {
        return clock.now().format("yyyy-MM-dd")
    }

    companion object {
        private const val KEY_REWARDED_COUNT = "rewarded_count"
        private const val KEY_REWARD_DATE = "reward_date"
    }
}
