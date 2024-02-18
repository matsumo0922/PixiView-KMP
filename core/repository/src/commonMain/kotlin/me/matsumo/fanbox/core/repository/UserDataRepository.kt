package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import me.matsumo.fanbox.core.datastore.PixiViewDataStore
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.model.UserData

interface UserDataRepository {

    val userData: Flow<UserData>
    val updatePlusMode: Flow<Boolean>

    suspend fun setDefault()
    suspend fun setPixiViewId(id: String)
    suspend fun setAgreedPrivacyPolicy(isAgreed: Boolean)
    suspend fun setAgreedTermsOfService(isAgreed: Boolean)
    suspend fun setThemeConfig(themeConfig: ThemeConfig)
    suspend fun setThemeColorConfig(themeColorConfig: ThemeColorConfig)
    suspend fun setAppLock(isAppLock: Boolean)
    suspend fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean)
    suspend fun setHideAdultContents(isHideAdultContents: Boolean)
    suspend fun setOverrideAdultContents(isOverrideAdultContents: Boolean)
    suspend fun setTestUser(isTestUser: Boolean)
    suspend fun setHideRestricted(isHideRestricted: Boolean)
    suspend fun setGridMode(isGridMode: Boolean)
    suspend fun setDeveloperMode(isDeveloperMode: Boolean)
    suspend fun setPlusMode(isPlusMode: Boolean)
    suspend fun setUseDynamicColor(useDynamicColor: Boolean)
}

class UserDataRepositoryImpl(
    private val pixiViewDataStore: PixiViewDataStore,
) : UserDataRepository {

    private val _updatePlusMode = Channel<Boolean>(Channel.BUFFERED)

    override val updatePlusMode: Flow<Boolean> = _updatePlusMode.receiveAsFlow()

    override val userData: Flow<UserData> = pixiViewDataStore.userData

    override suspend fun setDefault() {
        pixiViewDataStore.setDefault()
    }

    override suspend fun setPixiViewId(id: String) {
        pixiViewDataStore.setPixiViewId(id)
    }

    override suspend fun setAgreedPrivacyPolicy(isAgreed: Boolean) {
        pixiViewDataStore.setAgreedPrivacyPolicy(isAgreed)
    }

    override suspend fun setAgreedTermsOfService(isAgreed: Boolean) {
        pixiViewDataStore.setAgreedTermsOfService(isAgreed)
    }

    override suspend fun setThemeConfig(themeConfig: ThemeConfig) {
        pixiViewDataStore.setThemeConfig(themeConfig)
    }

    override suspend fun setThemeColorConfig(themeColorConfig: ThemeColorConfig) {
        pixiViewDataStore.setThemeColorConfig(themeColorConfig)
    }

    override suspend fun setAppLock(isAppLock: Boolean) {
        pixiViewDataStore.setAppLock(isAppLock)
    }

    override suspend fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean) {
        pixiViewDataStore.setFollowTabDefaultHome(isFollowTabDefaultHome)
    }

    override suspend fun setHideAdultContents(isHideAdultContents: Boolean) {
        pixiViewDataStore.setHideAdultContents(isHideAdultContents)
    }

    override suspend fun setOverrideAdultContents(isOverrideAdultContents: Boolean) {
        pixiViewDataStore.setOverrideAdultContents(isOverrideAdultContents)
    }

    override suspend fun setTestUser(isTestUser: Boolean) {
        pixiViewDataStore.setTestUser(isTestUser)
    }

    override suspend fun setHideRestricted(isHideRestricted: Boolean) {
        pixiViewDataStore.setHideRestricted(isHideRestricted)
    }

    override suspend fun setGridMode(isGridMode: Boolean) {
        pixiViewDataStore.setGridMode(isGridMode)
    }

    override suspend fun setDeveloperMode(isDeveloperMode: Boolean) {
        pixiViewDataStore.setDeveloperMode(isDeveloperMode)
    }

    override suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        pixiViewDataStore.setUseDynamicColor(useDynamicColor)
    }

    override suspend fun setPlusMode(isPlusMode: Boolean) {
        if (userData.first().isPlusMode != isPlusMode) {
            pixiViewDataStore.setPlusMode(isPlusMode)
            _updatePlusMode.send(isPlusMode)
        }
    }
}
