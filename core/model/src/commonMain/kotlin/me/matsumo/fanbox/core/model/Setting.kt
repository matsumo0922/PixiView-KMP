package me.matsumo.fanbox.core.model

import androidx.compose.ui.text.intl.Locale
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class Setting(
    val pixiViewId: String,
    val themeConfig: ThemeConfig,
    val themeColorConfig: ThemeColorConfig,
    val translateLanguage: String,
    val downloadFileType: DownloadFileType,
    val imageSaveDirectory: String,
    val fileSaveDirectory: String,
    val postSaveDirectory: String,
    val firstLaunchTime: Long,
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

    @OptIn(ExperimentalTime::class)
    val shouldShowInterstitialAd get() = (Clock.System.now().epochSeconds - firstLaunchTime) > 300L // 1日(=24*60*60秒)

    companion object Companion {
        @OptIn(ExperimentalTime::class)
        fun default(): Setting {
            return Setting(
                pixiViewId = "",
                themeConfig = ThemeConfig.System,
                themeColorConfig = ThemeColorConfig.Blue,
                translateLanguage = Locale.current.language,
                downloadFileType = DownloadFileType.ORIGINAL,
                imageSaveDirectory = "",
                fileSaveDirectory = "",
                postSaveDirectory = "",
                firstLaunchTime = -1L,
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
