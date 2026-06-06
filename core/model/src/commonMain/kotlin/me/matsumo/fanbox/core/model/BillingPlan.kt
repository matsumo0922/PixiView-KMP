package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable

/** Plus の購入プランを表すモデル。 */
@Immutable
data class BillingPlan(
    val id: String,
    val price: Long,
    val formattedPrice: String,
    val type: Type,
    val trialPeriod: BillingTrialPeriod?,
) {
    /** Plus の購入プラン種別を表す列挙型。 */
    enum class Type {
        MONTHLY,
        ANNUAL,
        UNKNOWN,
    }
}

/** Plus の無料トライアル期間を表すモデル。 */
@Immutable
data class BillingTrialPeriod(
    val value: Int,
    val unit: Unit,
) {
    /** Plus の無料トライアル期間の単位を表す列挙型。 */
    enum class Unit {
        DAY,
        WEEK,
        MONTH,
        YEAR,
        UNKNOWN,
    }
}
