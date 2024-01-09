package me.matsumo.fanbox.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val pixiViewId: String,
    val themeConfig: ThemeConfig,
    val themeColorConfig: ThemeColorConfig,
    val isAgreedPrivacyPolicy: Boolean,
    val isAgreedTermsOfService: Boolean,
    val isAppLock: Boolean,
    val isFollowTabDefaultHome: Boolean,
    val isHideAdultContents: Boolean,
    val isOverrideAdultContents: Boolean,
    val isDynamicColor: Boolean,
    val isHideRestricted: Boolean,
    val isGridMode: Boolean,
    val isTestUser: Boolean,
    val isDeveloperMode: Boolean,
    val isPlusMode: Boolean,
) {
    val hasPrivilege get() = isPlusMode || isDeveloperMode

    val isAllowedShowAdultContents get() = !isTestUser && isOverrideAdultContents

    companion object {
        fun default(): UserData {
            return UserData(
                pixiViewId = "",
                themeConfig = ThemeConfig.System,
                themeColorConfig = ThemeColorConfig.Blue,
                isAgreedPrivacyPolicy = false,
                isAgreedTermsOfService = false,
                isAppLock = false,
                isFollowTabDefaultHome = false,
                isHideAdultContents = true,
                isOverrideAdultContents = false,
                isDynamicColor = false,
                isHideRestricted = false,
                isGridMode = false,
                isTestUser = false,
                isDeveloperMode = true,
                isPlusMode = false,
            )
        }
    }
}
