package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** ホームのオファーのバナーを閉じた周期を保存する DataStore。 */
class OfferBannerDataStore(preferenceHelper: PreferenceHelper, private val ioDispatcher: CoroutineDispatcher) {
    private val preference = preferenceHelper.create(PreferencesName.OFFER_BANNER)
    private val dismissedCycleNumberKey = intPreferencesKey("dismissed_cycle_number")

    /** バナーを最後に閉じた周期の番号。閉じたことがなければ null。 */
    val dismissedCycleNumber: Flow<Int?> = preference.data.map { preferences -> preferences[dismissedCycleNumberKey] }

    suspend fun setDismissedCycleNumber(cycleNumber: Int) = withContext(ioDispatcher) {
        preference.edit { preferences ->
            preferences[dismissedCycleNumberKey] = cycleNumber
        }
    }
}
