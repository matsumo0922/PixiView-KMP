package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull

class FanboxCookieDataStore(
    private val preferenceHelper: PreferenceHelper,
) {
    private val cookiePreference = preferenceHelper.create(PreferencesName.FANBOX_COOKIE)

    private val _data = MutableSharedFlow<String>(replay = 1)

    val data: SharedFlow<String> = _data.asSharedFlow()

    suspend fun save(cookie: String) {
        _data.tryEmit(cookie)

        cookiePreference.edit {
            it[stringPreferencesKey(KEY_COOKIE)] = cookie
        }
    }

    suspend fun get(): String? {
        return cookiePreference.data.firstOrNull()?.get(stringPreferencesKey(KEY_COOKIE))
    }

    companion object {
        private const val KEY_COOKIE = "cookie"
    }
}
