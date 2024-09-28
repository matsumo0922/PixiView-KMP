package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.matsumo.fanbox.core.common.util.format

class RewardLogDataStore(
    private val preferenceHelper: PreferenceHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val preference = preferenceHelper.create(PreferencesName.REWARD_LOG)
    private val scope = CoroutineScope(ioDispatcher)

    fun rewarded() {
        scope.launch {
            preference.edit {
                it[intPreferencesKey(KEY_REWARDED_COUNT)] = getRewardedCount() + 1
                it[stringPreferencesKey(KEY_REWARD_DATE)] = Clock.System.now().format("yyyy-MM-dd")
            }
        }
    }

    fun reset() {
        scope.launch {
            preference.edit {
                it[intPreferencesKey(KEY_REWARDED_COUNT)] = 0
            }
        }
    }

    suspend fun getRewardedCount(): Int {
        return preference.data.map { it[intPreferencesKey(KEY_REWARDED_COUNT)] ?: 0 }.first()
    }

    suspend fun getRewardDate(): String? {
        return preference.data.map { it[stringPreferencesKey(KEY_REWARD_DATE)] }.first()
    }

    companion object {
        private const val KEY_REWARDED_COUNT = "rewarded_count"
        private const val KEY_REWARD_DATE = "reward_date"
    }
}
