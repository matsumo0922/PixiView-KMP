package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LaunchLogDataStore(
    private val preferenceHelper: PreferenceHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val preference = preferenceHelper.create(PreferencesName.LAUNCH_LOG)
    private val scope = CoroutineScope(ioDispatcher)

    private val data: Flow<Int> = preference.data.map { it[intPreferencesKey(KEY_LAUNCH_COUNT)] ?: 0 }

    fun launch() {
        scope.launch {
            preference.edit {
                it[intPreferencesKey(KEY_LAUNCH_COUNT)] = getLaunchCount() + 1
            }
        }
    }

    suspend fun getLaunchCount(): Int {
        return data.first()
    }

    companion object {
        const val KEY_LAUNCH_COUNT = "launch_count"
    }
}
