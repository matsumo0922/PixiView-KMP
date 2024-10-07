package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.logs.category.SettingsLog
import me.matsumo.fanbox.core.logs.logger.send
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
            setImageSaveDirectory(data.imageSaveDirectory)
            setFileSaveDirectory(data.fileSaveDirectory)
            setPostSaveDirectory(data.postSaveDirectory)
            setUseDynamicColor(data.isUseDynamicColor)
            setUseAppLock(data.isUseAppLock)
            setUseGridMode(data.isUseGridMode)
            setUseInfinityPostDetail(data.isUseInfinityPostDetail)
            setFollowTabDefaultHome(data.isDefaultFollowTabInHome)
            setHideAdultContents(data.isHideAdultContents)
            setOverrideAdultContents(data.isOverrideAdultContents)
            setAutoImagePreview(data.isAutoImagePreview)
            setTestUser(data.isTestUser)
            setHideRestricted(data.isHideRestricted)
            setDeveloperMode(data.isDeveloperMode)
            setPlusMode(data.isPlusMode)
        }
    }

    suspend fun setPixiViewId(id: String) = withContext(ioDispatcher) {
        if (userData.first().pixiViewId == id) return@withContext

        SettingsLog.update(
            propertyName = "pixiViewId",
            oldValue = userData.first().pixiViewId,
            newValue = id,
        ).send()

        userPreference.edit {
            it[stringPreferencesKey(UserData::pixiViewId.name)] = id
        }
    }

    suspend fun setAgreedPrivacyPolicy(isAgreed: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isAgreedPrivacyPolicy == isAgreed) return@withContext

        SettingsLog.update(
            propertyName = "isAgreedPrivacyPolicy",
            oldValue = userData.first().isAgreedPrivacyPolicy.toString(),
            newValue = isAgreed.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isAgreedPrivacyPolicy.name)] = isAgreed
        }
    }

    suspend fun setAgreedTermsOfService(isAgreed: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isAgreedTermsOfService == isAgreed) return@withContext

        SettingsLog.update(
            propertyName = "isAgreedTermsOfService",
            oldValue = userData.first().isAgreedTermsOfService.toString(),
            newValue = isAgreed.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isAgreedTermsOfService.name)] = isAgreed
        }
    }

    suspend fun setThemeConfig(themeConfig: ThemeConfig) = withContext(ioDispatcher) {
        if (userData.first().themeConfig == themeConfig) return@withContext

        SettingsLog.update(
            propertyName = "themeConfig",
            oldValue = userData.first().themeConfig.name,
            newValue = themeConfig.name,
        ).send()

        userPreference.edit {
            it[stringPreferencesKey(UserData::themeConfig.name)] = themeConfig.name
        }
    }

    suspend fun setThemeColorConfig(themeColorConfig: ThemeColorConfig) = withContext(ioDispatcher) {
        if (userData.first().themeColorConfig == themeColorConfig) return@withContext

        SettingsLog.update(
            propertyName = "themeColorConfig",
            oldValue = userData.first().themeColorConfig.name,
            newValue = themeColorConfig.name,
        ).send()

        userPreference.edit {
            it[stringPreferencesKey(UserData::themeColorConfig.name)] = themeColorConfig.name
        }
    }

    suspend fun setImageSaveDirectory(directory: String) = withContext(ioDispatcher) {
        if (userData.first().imageSaveDirectory == directory) return@withContext

        SettingsLog.update(
            propertyName = "imageSaveDirectory",
            oldValue = userData.first().imageSaveDirectory,
            newValue = directory,
        ).send()

        userPreference.edit {
            it[stringPreferencesKey(UserData::imageSaveDirectory.name)] = directory
        }
    }

    suspend fun setFileSaveDirectory(directory: String) = withContext(ioDispatcher) {
        if (userData.first().fileSaveDirectory == directory) return@withContext

        SettingsLog.update(
            propertyName = "fileSaveDirectory",
            oldValue = userData.first().fileSaveDirectory,
            newValue = directory,
        ).send()

        userPreference.edit {
            it[stringPreferencesKey(UserData::fileSaveDirectory.name)] = directory
        }
    }

    suspend fun setPostSaveDirectory(directory: String) = withContext(ioDispatcher) {
        if (userData.first().postSaveDirectory == directory) return@withContext

        SettingsLog.update(
            propertyName = "postSaveDirectory",
            oldValue = userData.first().postSaveDirectory,
            newValue = directory,
        ).send()

        userPreference.edit {
            it[stringPreferencesKey(UserData::postSaveDirectory.name)] = directory
        }
    }

    suspend fun setUseDynamicColor(useDynamicColor: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isUseDynamicColor == useDynamicColor) return@withContext

        SettingsLog.update(
            propertyName = "isUseDynamicColor",
            oldValue = userData.first().isUseDynamicColor.toString(),
            newValue = useDynamicColor.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isUseDynamicColor.name)] = useDynamicColor
        }
    }

    suspend fun setUseAppLock(isAppLock: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isUseAppLock == isAppLock) return@withContext

        SettingsLog.update(
            propertyName = "isUseAppLock",
            oldValue = userData.first().isUseAppLock.toString(),
            newValue = isAppLock.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isUseAppLock.name)] = isAppLock
        }
    }

    suspend fun setUseGridMode(isGridMode: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isUseGridMode == isGridMode) return@withContext

        SettingsLog.update(
            propertyName = "isUseGridMode",
            oldValue = userData.first().isUseGridMode.toString(),
            newValue = isGridMode.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isUseGridMode.name)] = isGridMode
        }
    }

    suspend fun setUseInfinityPostDetail(isUseInfinityPostDetail: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isUseInfinityPostDetail == isUseInfinityPostDetail) return@withContext

        SettingsLog.update(
            propertyName = "isUseInfinityPostDetail",
            oldValue = userData.first().isUseInfinityPostDetail.toString(),
            newValue = isUseInfinityPostDetail.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isUseInfinityPostDetail.name)] = isUseInfinityPostDetail
        }
    }

    suspend fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isDefaultFollowTabInHome == isFollowTabDefaultHome) return@withContext

        SettingsLog.update(
            propertyName = "isDefaultFollowTabInHome",
            oldValue = userData.first().isDefaultFollowTabInHome.toString(),
            newValue = isFollowTabDefaultHome.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isDefaultFollowTabInHome.name)] = isFollowTabDefaultHome
        }
    }

    suspend fun setHideAdultContents(isHideAdultContents: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isHideAdultContents == isHideAdultContents) return@withContext

        SettingsLog.update(
            propertyName = "isHideAdultContents",
            oldValue = userData.first().isHideAdultContents.toString(),
            newValue = isHideAdultContents.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isHideAdultContents.name)] = isHideAdultContents
        }
    }

    suspend fun setOverrideAdultContents(isOverrideAdultContents: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isOverrideAdultContents == isOverrideAdultContents) return@withContext

        SettingsLog.update(
            propertyName = "isOverrideAdultContents",
            oldValue = userData.first().isOverrideAdultContents.toString(),
            newValue = isOverrideAdultContents.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isOverrideAdultContents.name)] = isOverrideAdultContents
        }
    }

    suspend fun setAutoImagePreview(isAutoImagePreview: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isAutoImagePreview == isAutoImagePreview) return@withContext

        SettingsLog.update(
            propertyName = "isAutoImagePreview",
            oldValue = userData.first().isAutoImagePreview.toString(),
            newValue = isAutoImagePreview.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isAutoImagePreview.name)] = isAutoImagePreview
        }
    }

    suspend fun setTestUser(isTestUser: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isTestUser == isTestUser) return@withContext

        SettingsLog.update(
            propertyName = "isTestUser",
            oldValue = userData.first().isTestUser.toString(),
            newValue = isTestUser.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isTestUser.name)] = isTestUser
        }
    }

    suspend fun setHideRestricted(isHideRestricted: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isHideRestricted == isHideRestricted) return@withContext

        SettingsLog.update(
            propertyName = "isHideRestricted",
            oldValue = userData.first().isHideRestricted.toString(),
            newValue = isHideRestricted.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isHideRestricted.name)] = isHideRestricted
        }
    }

    suspend fun setDeveloperMode(isDeveloperMode: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isDeveloperMode == isDeveloperMode) return@withContext

        SettingsLog.update(
            propertyName = "isDeveloperMode",
            oldValue = userData.first().isDeveloperMode.toString(),
            newValue = isDeveloperMode.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isDeveloperMode.name)] = isDeveloperMode
        }
    }

    suspend fun setPlusMode(isPlusMode: Boolean) = withContext(ioDispatcher) {
        if (userData.first().isPlusMode == isPlusMode) return@withContext

        SettingsLog.update(
            propertyName = "isPlusMode",
            oldValue = userData.first().isPlusMode.toString(),
            newValue = isPlusMode.toString(),
        ).send()

        userPreference.edit {
            it[booleanPreferencesKey(UserData::isPlusMode.name)] = isPlusMode
        }
    }
}
