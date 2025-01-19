package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
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
import me.matsumo.fanbox.core.model.Flag

class FlagDataStore(
    private val preferenceHelper: PreferenceHelper,
) {
    private val preference = preferenceHelper.create(PreferencesName.FLAG)

    suspend fun setFlag(key: Flag, value: Boolean) {
        preference.edit {
            it[booleanPreferencesKey(key.name)] = value
        }
    }

    suspend fun getFlag(key: Flag, default: Boolean): Boolean {
        return preference.data.map { it[booleanPreferencesKey(key.name)] ?: default }.first()
    }
}
