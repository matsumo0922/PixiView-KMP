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
        it.deserialize(formatter, UserData.serializer(), UserData.default())
    }

    suspend fun setDefault() = withContext(ioDispatcher) {
        UserData.default().also { data ->
            setPixiViewId(data.pixiViewId)
            setAgreedPrivacyPolicy(data.isAgreedPrivacyPolicy)
            setAgreedTermsOfService(data.isAgreedTermsOfService)
            setThemeConfig(data.themeConfig)
            setThemeColorConfig(data.themeColorConfig)
            setUseDynamicColor(data.isUseDynamicColor)
            setUseAppLock(data.isUseAppLock)
            setUseGridMode(data.isUseGridMode)
            setUseInfinityPostDetail(data.isUseInfinityPostDetail)
            setFollowTabDefaultHome(data.isDefaultFollowTabInHome)
            setHideAdultContents(data.isHideAdultContents)
            setOverrideAdultContents(data.isOverrideAdultContents)
            setTestUser(data.isTestUser)
            setHideRestricted(data.isHideRestricted)
            setDeveloperMode(data.isDeveloperMode)
            setPlusMode(data.isPlusMode)
        }
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
            it[booleanPreferencesKey(UserData::isUseDynamicColor.name)] = useDynamicColor
        }
    }

    suspend fun setUseAppLock(isAppLock: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isUseAppLock.name)] = isAppLock
        }
    }

    suspend fun setUseGridMode(isGridMode: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isUseGridMode.name)] = isGridMode
        }
    }

    suspend fun setUseInfinityPostDetail(isUseInfinityPostDetail: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isUseInfinityPostDetail.name)] = isUseInfinityPostDetail
        }
    }

    suspend fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean) = withContext(ioDispatcher) {
        userPreference.edit {
            it[booleanPreferencesKey(UserData::isDefaultFollowTabInHome.name)] = isFollowTabDefaultHome
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
