package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import me.matsumo.fanbox.core.datastore.SettingDataStore
import me.matsumo.fanbox.core.model.DownloadFileType
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig

interface SettingRepository {

    val setting: Flow<Setting>
    val updatePlusMode: Flow<Boolean>

    suspend fun setDefault()
    suspend fun setPixiViewId(id: String)
    suspend fun setAgreedPrivacyPolicy(isAgreed: Boolean)
    suspend fun setAgreedTermsOfService(isAgreed: Boolean)
    suspend fun setThemeConfig(themeConfig: ThemeConfig)
    suspend fun setThemeColorConfig(themeColorConfig: ThemeColorConfig)
    suspend fun setTranslateLanguage(code: String)
    suspend fun setDownloadFileType(downloadFileType: DownloadFileType)
    suspend fun setImageSaveDirectory(directory: String)
    suspend fun setFileSaveDirectory(directory: String)
    suspend fun setPostSaveDirectory(directory: String)
    suspend fun setUseDynamicColor(useDynamicColor: Boolean)
    suspend fun setUseAppLock(isAppLock: Boolean)
    suspend fun setUseGridMode(isGridMode: Boolean)
    suspend fun setUseInfinityPostDetail(isInfinityPostDetail: Boolean)
    suspend fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean)
    suspend fun setHideAdultContents(isHideAdultContents: Boolean)
    suspend fun setOverrideAdultContents(isOverrideAdultContents: Boolean)
    suspend fun setAutoImagePreview(isAutoImagePreview: Boolean)
    suspend fun setTestUser(isTestUser: Boolean)
    suspend fun setHideRestricted(isHideRestricted: Boolean)
    suspend fun setDeveloperMode(isDeveloperMode: Boolean)
    suspend fun setPlusMode(isPlusMode: Boolean)
}

class SettingRepositoryImpl(
    private val settingDataStore: SettingDataStore,
) : SettingRepository {

    private val _updatePlusMode = Channel<Boolean>(Channel.BUFFERED)

    override val updatePlusMode: Flow<Boolean> = _updatePlusMode.receiveAsFlow()

    override val setting: Flow<Setting> = settingDataStore.setting

    override suspend fun setDefault() {
        settingDataStore.setDefault()
    }

    override suspend fun setPixiViewId(id: String) {
        settingDataStore.setPixiViewId(id)
    }

    override suspend fun setAgreedPrivacyPolicy(isAgreed: Boolean) {
        settingDataStore.setAgreedPrivacyPolicy(isAgreed)
    }

    override suspend fun setAgreedTermsOfService(isAgreed: Boolean) {
        settingDataStore.setAgreedTermsOfService(isAgreed)
    }

    override suspend fun setThemeConfig(themeConfig: ThemeConfig) {
        settingDataStore.setThemeConfig(themeConfig)
    }

    override suspend fun setThemeColorConfig(themeColorConfig: ThemeColorConfig) {
        settingDataStore.setThemeColorConfig(themeColorConfig)
    }

    override suspend fun setTranslateLanguage(code: String) {
        settingDataStore.setTranslateLanguage(code)
    }

    override suspend fun setDownloadFileType(downloadFileType: DownloadFileType) {
        settingDataStore.setDownloadFileType(downloadFileType)
    }

    override suspend fun setImageSaveDirectory(directory: String) {
        settingDataStore.setImageSaveDirectory(directory)
    }

    override suspend fun setFileSaveDirectory(directory: String) {
        settingDataStore.setFileSaveDirectory(directory)
    }

    override suspend fun setPostSaveDirectory(directory: String) {
        settingDataStore.setPostSaveDirectory(directory)
    }

    override suspend fun setUseAppLock(isAppLock: Boolean) {
        settingDataStore.setUseAppLock(isAppLock)
    }

    override suspend fun setUseInfinityPostDetail(isInfinityPostDetail: Boolean) {
        settingDataStore.setUseInfinityPostDetail(isInfinityPostDetail)
    }

    override suspend fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean) {
        settingDataStore.setFollowTabDefaultHome(isFollowTabDefaultHome)
    }

    override suspend fun setHideAdultContents(isHideAdultContents: Boolean) {
        settingDataStore.setHideAdultContents(isHideAdultContents)
    }

    override suspend fun setOverrideAdultContents(isOverrideAdultContents: Boolean) {
        settingDataStore.setOverrideAdultContents(isOverrideAdultContents)
    }

    override suspend fun setAutoImagePreview(isAutoImagePreview: Boolean) {
        settingDataStore.setAutoImagePreview(isAutoImagePreview)
    }

    override suspend fun setTestUser(isTestUser: Boolean) {
        settingDataStore.setTestUser(isTestUser)
    }

    override suspend fun setHideRestricted(isHideRestricted: Boolean) {
        settingDataStore.setHideRestricted(isHideRestricted)
    }

    override suspend fun setUseGridMode(isGridMode: Boolean) {
        settingDataStore.setUseGridMode(isGridMode)
    }

    override suspend fun setDeveloperMode(isDeveloperMode: Boolean) {
        settingDataStore.setDeveloperMode(isDeveloperMode)
    }

    override suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        settingDataStore.setUseDynamicColor(useDynamicColor)
    }

    override suspend fun setPlusMode(isPlusMode: Boolean) {
        if (setting.first().isPlusMode != isPlusMode) {
            settingDataStore.setPlusMode(isPlusMode)
            _updatePlusMode.send(isPlusMode)

            if (!isPlusMode) {
                setUseDynamicColor(false)
                setUseAppLock(false)
                setHideRestricted(false)
                setUseGridMode(false)
                setAutoImagePreview(false)
            }
        }
    }
}
