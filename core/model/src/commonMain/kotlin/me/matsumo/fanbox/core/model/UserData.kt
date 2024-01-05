package me.matsumo.fanbox.core.model

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
        fun dummy(): UserData {
            return UserData(
                pixiViewId = "",
                themeConfig = ThemeConfig.System,
                themeColorConfig = ThemeColorConfig.Red,
                isAgreedPrivacyPolicy = false,
                isAgreedTermsOfService = false,
                isAppLock = false,
                isFollowTabDefaultHome = false,
                isHideAdultContents = false,
                isOverrideAdultContents = false,
                isDynamicColor = true,
                isHideRestricted = false,
                isGridMode = false,
                isTestUser = false,
                isDeveloperMode = true,
                isPlusMode = false,
            )
        }
    }
}
