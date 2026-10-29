package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class OldCookieDataStore(
    preferenceHelper: PreferenceHelper,
) {
    private val cookiePreference = preferenceHelper.create(PreferencesName.FANBOX_COOKIE)

    val data: Flow<String> = cookiePreference.data.map { it[stringPreferencesKey(KEY_COOKIE)] ?: "" }

    suspend fun save(cookie: String) {
        cookiePreference.edit {
            it[stringPreferencesKey(KEY_COOKIE)] = cookie
        }
    }

    suspend fun get(): String? = cookiePreference.data.firstOrNull()?.get(stringPreferencesKey(KEY_COOKIE))

    /**
     * 保存されている Cookie ヘッダから FANBOXSESSID の値を返す。無ければ null を返す。
     *
     * 値そのものは認証情報なので、ログや telemetry に出さないこと。
     */
    suspend fun getSessionId(): String? = parseOldSessionId(get())

    companion object {
        private const val KEY_COOKIE = "cookie"
    }
}
