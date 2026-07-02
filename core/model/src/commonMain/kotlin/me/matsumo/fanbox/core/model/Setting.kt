package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.intl.Locale
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/** アプリ全体のユーザー設定を表すモデル。 */
@Immutable
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
    val isPlusTrial: Boolean,
    val isPlusSubscriptionSetToCancel: Boolean,
    val plusUnsubscribeDetectedAtMillis: Long?,
    val plusPlanType: BillingPlan.Type,
    val plusRetentionPromptLastShownAtMillis: Long,
    val plusRetentionPromptLastShownUnsubscribeDetectedAtMillis: Long?,
) {
    /** Plus として扱う機能を利用できるかどうか。 */
    val hasPrivilege get() = isPlusMode || isDeveloperMode

    /** クリエイター全投稿の一括ダウンロードを利用できるかどうか。 */
    val canBulkDownload get() = isDeveloperMode || (isPlusMode && !isPlusTrial)

    /** 成人向けコンテンツを表示できるかどうか。 */
    val isAllowedShowAdultContents get() = !isTestUser && isOverrideAdultContents

    /** インタースティシャル広告を表示する期間に入っているかどうか。 */
    @OptIn(ExperimentalTime::class)
    val shouldShowInterstitialAd get() = (Clock.System.now().epochSeconds - firstLaunchTime) > 2.days.inWholeSeconds

    /** 解約予約中の Plus ユーザーとして扱うかどうか。 */
    val isSubscriptionSetToCancel get() = isPlusMode && isPlusSubscriptionSetToCancel

    /** リテンション UI で年額プランへの訴求を表示するかどうか。 */
    val shouldShowBillingRetentionAnnualOffer get() = plusPlanType != BillingPlan.Type.ANNUAL

    /** 指定時刻でリテンション UI を表示できるかどうか。 */
    fun canShowBillingRetentionPrompt(currentTimeMillis: Long): Boolean {
        if (!isSubscriptionSetToCancel) return false

        val hasNeverShown = plusRetentionPromptLastShownAtMillis < 0L
        if (hasNeverShown) return true

        val isNewEpisode = plusRetentionPromptLastShownUnsubscribeDetectedAtMillis != plusUnsubscribeDetectedAtMillis
        if (isNewEpisode) return true

        val elapsedTimeMillis = currentTimeMillis - plusRetentionPromptLastShownAtMillis

        return elapsedTimeMillis >= BILLING_RETENTION_PROMPT_INTERVAL_MILLIS
    }

    /** アプリ設定の初期値を生成するオブジェクト。 */
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
                isPlusTrial = false,
                isPlusSubscriptionSetToCancel = false,
                plusUnsubscribeDetectedAtMillis = null,
                plusPlanType = BillingPlan.Type.UNKNOWN,
                plusRetentionPromptLastShownAtMillis = -1L,
                plusRetentionPromptLastShownUnsubscribeDetectedAtMillis = null,
            )
        }
    }
}

/** リテンション UI の再表示を抑制するミリ秒数。 */
private const val BILLING_RETENTION_PROMPT_INTERVAL_MILLIS = 86_400_000L
