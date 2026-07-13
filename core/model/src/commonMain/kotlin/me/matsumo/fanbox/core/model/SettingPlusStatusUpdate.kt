package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable

/** Plus 課金状態の設定更新内容を表すモデル。 */
@Immutable
data class SettingPlusStatusUpdate(
    val isPlusMode: Boolean,
    val isPlusTrial: Boolean,
    val isPlusSubscriptionSetToCancel: Boolean,
    val plusUnsubscribeDetectedAtMillis: Long?,
    val plusPlanType: BillingPlan.Type,
    val isPlusModeChanged: Boolean,
    val isPlusTrialChanged: Boolean,
    val isPlusSubscriptionSetToCancelChanged: Boolean,
    val plusUnsubscribeDetectedAtMillisChanged: Boolean,
    val plusPlanTypeChanged: Boolean,
) {
    /** Plus 課金状態の設定に変更があるかどうか。 */
    val hasChanged
        get() = listOf(
            isPlusModeChanged,
            isPlusTrialChanged,
            isPlusSubscriptionSetToCancelChanged,
            plusUnsubscribeDetectedAtMillisChanged,
            plusPlanTypeChanged,
        ).any { hasPropertyChanged -> hasPropertyChanged }

    /** Plus 課金状態の設定更新内容を生成するオブジェクト。 */
    companion object {
        fun from(
            currentSetting: Setting,
            plusStatus: BillingPlusStatus,
        ): SettingPlusStatusUpdate {
            val isPlusMode = plusStatus.isActive
            val normalizedIsPlusTrial = isPlusMode && plusStatus.isTrial
            val normalizedIsSetToCancel = isPlusMode && plusStatus.isSetToCancel
            val normalizedUnsubscribeDetectedAtMillis = if (normalizedIsSetToCancel) {
                plusStatus.unsubscribeDetectedAtMillis
            } else {
                null
            }
            val normalizedPlanType = plusStatus.planType.takeIf { isPlusMode } ?: BillingPlan.Type.UNKNOWN

            return SettingPlusStatusUpdate(
                isPlusMode = isPlusMode,
                isPlusTrial = normalizedIsPlusTrial,
                isPlusSubscriptionSetToCancel = normalizedIsSetToCancel,
                plusUnsubscribeDetectedAtMillis = normalizedUnsubscribeDetectedAtMillis,
                plusPlanType = normalizedPlanType,
                isPlusModeChanged = currentSetting.isPlusMode != isPlusMode,
                isPlusTrialChanged = currentSetting.isPlusTrial != normalizedIsPlusTrial,
                isPlusSubscriptionSetToCancelChanged = currentSetting.isPlusSubscriptionSetToCancel != normalizedIsSetToCancel,
                plusUnsubscribeDetectedAtMillisChanged = currentSetting.plusUnsubscribeDetectedAtMillis != normalizedUnsubscribeDetectedAtMillis,
                plusPlanTypeChanged = currentSetting.plusPlanType != normalizedPlanType,
            )
        }
    }
}
