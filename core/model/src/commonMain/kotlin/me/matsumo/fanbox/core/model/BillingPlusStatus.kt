package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable

/** Plus の利用状態を表すモデル。 */
@Immutable
data class BillingPlusStatus(
    val isActive: Boolean,
    val isTrial: Boolean,
    val willRenew: Boolean,
    val unsubscribeDetectedAtMillis: Long?,
    val planType: BillingPlan.Type,
) {
    /** Plus の有効期間内で、RevenueCat が解約を検知している状態かどうか。 */
    val isSetToCancel get() = isActive && !willRenew && unsubscribeDetectedAtMillis != null
}
