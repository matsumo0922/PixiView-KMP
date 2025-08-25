package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.logs.category.SettingsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.DownloadFileType
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig

class SettingDataStore(
    private val preferenceHelper: PreferenceHelper,
    private val formatter: Json,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val settingPreference = preferenceHelper.create(PreferencesName.APP_SETTINGS)

    val setting = settingPreference.data.map {
        it.deserialize(formatter, Setting.serializer(), Setting.default())
    }.stateIn(
        scope = CoroutineScope(ioDispatcher),
        started = SharingStarted.WhileSubscribed(1000),
        initialValue = Setting.default(),
    )

    suspend fun setPixiViewId(id: String) = withContext(ioDispatcher) {
        if (setting.first().pixiViewId == id) return@withContext

        SettingsLog.update(
            propertyName = "pixiViewId",
            oldValue = setting.first().pixiViewId,
            newValue = id,
        ).send()

        settingPreference.edit {
            it[stringPreferencesKey(Setting::pixiViewId.name)] = id
        }
    }

    suspend fun setAgreedPrivacyPolicy(isAgreed: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isAgreedPrivacyPolicy == isAgreed) return@withContext

        SettingsLog.update(
            propertyName = "isAgreedPrivacyPolicy",
            oldValue = setting.first().isAgreedPrivacyPolicy.toString(),
            newValue = isAgreed.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isAgreedPrivacyPolicy.name)] = isAgreed
        }
    }

    suspend fun setAgreedTermsOfService(isAgreed: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isAgreedTermsOfService == isAgreed) return@withContext

        SettingsLog.update(
            propertyName = "isAgreedTermsOfService",
            oldValue = setting.first().isAgreedTermsOfService.toString(),
            newValue = isAgreed.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isAgreedTermsOfService.name)] = isAgreed
        }
    }

    suspend fun setThemeConfig(themeConfig: ThemeConfig) = withContext(ioDispatcher) {
        if (setting.first().themeConfig == themeConfig) return@withContext

        SettingsLog.update(
            propertyName = "themeConfig",
            oldValue = setting.first().themeConfig.name,
            newValue = themeConfig.name,
        ).send()

        settingPreference.edit {
            it[stringPreferencesKey(Setting::themeConfig.name)] = themeConfig.name
        }
    }

    suspend fun setThemeColorConfig(themeColorConfig: ThemeColorConfig) = withContext(ioDispatcher) {
        if (setting.first().themeColorConfig == themeColorConfig) return@withContext

        SettingsLog.update(
            propertyName = "themeColorConfig",
            oldValue = setting.first().themeColorConfig.name,
            newValue = themeColorConfig.name,
        ).send()

        settingPreference.edit {
            it[stringPreferencesKey(Setting::themeColorConfig.name)] = themeColorConfig.name
        }
    }

    suspend fun setTranslateLanguage(code: String) = withContext(ioDispatcher) {
        if (setting.first().translateLanguage == code) return@withContext

        SettingsLog.update(
            propertyName = "translateLanguage",
            oldValue = setting.first().translateLanguage,
            newValue = code,
        ).send()

        settingPreference.edit {
            it[stringPreferencesKey(Setting::translateLanguage.name)] = code
        }
    }

    suspend fun setDownloadFileType(downloadFileType: DownloadFileType) = withContext(ioDispatcher) {
        if (setting.first().downloadFileType == downloadFileType) return@withContext

        SettingsLog.update(
            propertyName = "downloadFileType",
            oldValue = setting.first().downloadFileType.name,
            newValue = downloadFileType.name,
        ).send()

        settingPreference.edit {
            it[stringPreferencesKey(Setting::downloadFileType.name)] = downloadFileType.name
        }
    }

    suspend fun setImageSaveDirectory(directory: String) = withContext(ioDispatcher) {
        if (setting.first().imageSaveDirectory == directory) return@withContext

        SettingsLog.update(
            propertyName = "imageSaveDirectory",
            oldValue = setting.first().imageSaveDirectory,
            newValue = directory,
        ).send()

        settingPreference.edit {
            it[stringPreferencesKey(Setting::imageSaveDirectory.name)] = directory
        }
    }

    suspend fun setFileSaveDirectory(directory: String) = withContext(ioDispatcher) {
        if (setting.first().fileSaveDirectory == directory) return@withContext

        SettingsLog.update(
            propertyName = "fileSaveDirectory",
            oldValue = setting.first().fileSaveDirectory,
            newValue = directory,
        ).send()

        settingPreference.edit {
            it[stringPreferencesKey(Setting::fileSaveDirectory.name)] = directory
        }
    }

    suspend fun setPostSaveDirectory(directory: String) = withContext(ioDispatcher) {
        if (setting.first().postSaveDirectory == directory) return@withContext

        SettingsLog.update(
            propertyName = "postSaveDirectory",
            oldValue = setting.first().postSaveDirectory,
            newValue = directory,
        ).send()

        settingPreference.edit {
            it[stringPreferencesKey(Setting::postSaveDirectory.name)] = directory
        }
    }

    suspend fun setFirstLaunchTime(time: Long) = withContext(ioDispatcher) {
        if (setting.first().firstLaunchTime == time) return@withContext

        SettingsLog.update(
            propertyName = "firstLaunchTime",
            oldValue = setting.first().firstLaunchTime.toString(),
            newValue = time.toString(),
        ).send()

        settingPreference.edit {
            it[longPreferencesKey(Setting::firstLaunchTime.name)] = time
        }
    }

    suspend fun setUseDynamicColor(useDynamicColor: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isUseDynamicColor == useDynamicColor) return@withContext

        SettingsLog.update(
            propertyName = "isUseDynamicColor",
            oldValue = setting.first().isUseDynamicColor.toString(),
            newValue = useDynamicColor.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isUseDynamicColor.name)] = useDynamicColor
        }
    }

    suspend fun setUseAppLock(isAppLock: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isUseAppLock == isAppLock) return@withContext

        SettingsLog.update(
            propertyName = "isUseAppLock",
            oldValue = setting.first().isUseAppLock.toString(),
            newValue = isAppLock.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isUseAppLock.name)] = isAppLock
        }
    }

    suspend fun setUseGridMode(isGridMode: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isUseGridMode == isGridMode) return@withContext

        SettingsLog.update(
            propertyName = "isUseGridMode",
            oldValue = setting.first().isUseGridMode.toString(),
            newValue = isGridMode.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isUseGridMode.name)] = isGridMode
        }
    }

    suspend fun setUseInfinityPostDetail(isUseInfinityPostDetail: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isUseInfinityPostDetail == isUseInfinityPostDetail) return@withContext

        SettingsLog.update(
            propertyName = "isUseInfinityPostDetail",
            oldValue = setting.first().isUseInfinityPostDetail.toString(),
            newValue = isUseInfinityPostDetail.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isUseInfinityPostDetail.name)] = isUseInfinityPostDetail
        }
    }

    suspend fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isDefaultFollowTabInHome == isFollowTabDefaultHome) return@withContext

        SettingsLog.update(
            propertyName = "isDefaultFollowTabInHome",
            oldValue = setting.first().isDefaultFollowTabInHome.toString(),
            newValue = isFollowTabDefaultHome.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isDefaultFollowTabInHome.name)] = isFollowTabDefaultHome
        }
    }

    suspend fun setHideAdultContents(isHideAdultContents: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isHideAdultContents == isHideAdultContents) return@withContext

        SettingsLog.update(
            propertyName = "isHideAdultContents",
            oldValue = setting.first().isHideAdultContents.toString(),
            newValue = isHideAdultContents.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isHideAdultContents.name)] = isHideAdultContents
        }
    }

    suspend fun setOverrideAdultContents(isOverrideAdultContents: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isOverrideAdultContents == isOverrideAdultContents) return@withContext

        SettingsLog.update(
            propertyName = "isOverrideAdultContents",
            oldValue = setting.first().isOverrideAdultContents.toString(),
            newValue = isOverrideAdultContents.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isOverrideAdultContents.name)] = isOverrideAdultContents
        }
    }

    suspend fun setAutoImagePreview(isAutoImagePreview: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isAutoImagePreview == isAutoImagePreview) return@withContext

        SettingsLog.update(
            propertyName = "isAutoImagePreview",
            oldValue = setting.first().isAutoImagePreview.toString(),
            newValue = isAutoImagePreview.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isAutoImagePreview.name)] = isAutoImagePreview
        }
    }

    suspend fun setTestUser(isTestUser: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isTestUser == isTestUser) return@withContext

        SettingsLog.update(
            propertyName = "isTestUser",
            oldValue = setting.first().isTestUser.toString(),
            newValue = isTestUser.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isTestUser.name)] = isTestUser
        }
    }

    suspend fun setHideRestricted(isHideRestricted: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isHideRestricted == isHideRestricted) return@withContext

        SettingsLog.update(
            propertyName = "isHideRestricted",
            oldValue = setting.first().isHideRestricted.toString(),
            newValue = isHideRestricted.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isHideRestricted.name)] = isHideRestricted
        }
    }

    suspend fun setDeveloperMode(isDeveloperMode: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isDeveloperMode == isDeveloperMode) return@withContext

        SettingsLog.update(
            propertyName = "isDeveloperMode",
            oldValue = setting.first().isDeveloperMode.toString(),
            newValue = isDeveloperMode.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isDeveloperMode.name)] = isDeveloperMode
        }
    }

    suspend fun setPlusMode(isPlusMode: Boolean) = withContext(ioDispatcher) {
        if (setting.first().isPlusMode == isPlusMode) return@withContext

        SettingsLog.update(
            propertyName = "isPlusMode",
            oldValue = setting.first().isPlusMode.toString(),
            newValue = isPlusMode.toString(),
        ).send()

        settingPreference.edit {
            it[booleanPreferencesKey(Setting::isPlusMode.name)] = isPlusMode
        }
    }
}
