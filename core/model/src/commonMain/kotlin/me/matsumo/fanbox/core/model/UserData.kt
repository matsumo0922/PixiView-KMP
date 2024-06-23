package me.matsumo.fanbox.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val pixiViewId: String,
    val themeConfig: ThemeConfig,
    val themeColorConfig: ThemeColorConfig,
    val isAgreedPrivacyPolicy: Boolean,
    val isAgreedTermsOfService: Boolean,
    val isUseAppLock: Boolean,
    val isUseDynamicColor: Boolean,
    val isUseGridMode: Boolean,
    val isUseInfinityPostDetail: Boolean,
    val isDefaultFollowTabInHome: Boolean,
    val isHideAdultContents: Boolean,
    val isOverrideAdultContents: Boolean,
    val isHideRestricted: Boolean,
    val isAutoImagePreview: Boolean,
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
                isUseAppLock = false,
                isUseDynamicColor = false,
                isUseGridMode = false,
                isUseInfinityPostDetail = false,
                isDefaultFollowTabInHome = false,
                isHideAdultContents = true,
                isOverrideAdultContents = true,
                isHideRestricted = false,
                isAutoImagePreview = false,
                isTestUser = false,
                isDeveloperMode = false,
                isPlusMode = false,
            )
        }
    }
}
