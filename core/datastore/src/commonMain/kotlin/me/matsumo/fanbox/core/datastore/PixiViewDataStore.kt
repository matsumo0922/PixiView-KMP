package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.model.UserData

class PixiViewDataStore(
    private val preferenceHelper: PreferenceHelper,
    private val formatter: Json,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val userPreference = preferenceHelper.create(PreferencesName.APP_SETTINGS)

    val userData = userPreference.data.map {
        it.deserialize(formatter, UserData.serializer())
    }

    suspend fun setPixiViewId(id: String) = withContext(ioDispatcher) {
        userPreference.edit {
            it[stringPreferencesKey(UserData::pixiViewId.name)] = id
        }
    }

    suspend fun setAgreedPrivacyPolicy(isAgreed: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isAgreedPrivacyPolicy.name)] = isAgreed
        }
    }

    suspend fun setAgreedTermsOfService(isAgreed: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isAgreedTermsOfService.name)] = isAgreed
        }
    }

    suspend fun setThemeConfig(themeConfig: ThemeConfig) = withContext(ioDispatcher) {
        userPreference.edit {
            it[stringPreferencesKey(UserData::themeConfig.name)] = themeConfig.name
        }
    }

    suspend fun setThemeColorConfig(themeColorConfig: ThemeColorConfig) = withContext(ioDispatcher) {
        userPreference.edit {
            it[stringPreferencesKey(UserData::themeColorConfig.name)] = themeColorConfig.name
        }
    }

    suspend fun setUseDynamicColor(useDynamicColor: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isDynamicColor.name)] = useDynamicColor
        }
    }

    suspend fun setAppLock(isAppLock: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isAppLock.name)] = isAppLock
        }
    }

    suspend fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isFollowTabDefaultHome.name)] = isFollowTabDefaultHome
        }
    }

    suspend fun setHideAdultContents(isHideAdultContents: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isHideAdultContents.name)] = isHideAdultContents
        }
    }

    suspend fun setOverrideAdultContents(isOverrideAdultContents: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isOverrideAdultContents.name)] = isOverrideAdultContents
        }
    }

    suspend fun setTestUser(isTestUser: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isTestUser.name)] = isTestUser
        }
    }

    suspend fun setHideRestricted(isHideRestricted: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isHideRestricted.name)] = isHideRestricted
        }
    }

    suspend fun setGridMode(isGridMode: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isGridMode.name)] = isGridMode
        }
    }

    suspend fun setDeveloperMode(isDeveloperMode: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isDeveloperMode.name)] = isDeveloperMode
        }
    }

    suspend fun setPlusMode(isPlusMode: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isPlusMode.name)] = isPlusMode
        }
    }
}
