package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

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
    @Serializable
    enum class Type {
        MONTHLY,
        ANNUAL,
        UNKNOWN,
    }
}

/**
 * 年額プランの価格が、月額プラン 12 か月分と比べて何 % 安いかを返す。
 *
 * 実際より大きい割引を表示しないよう、端数は切り捨てる。月額と年額の両方の価格があり、
 * 割引率が 1% 以上のときだけ値を返し、それ以外は null を返す。
 */
fun calculateAnnualDiscountPercent(plans: List<BillingPlan>): Int? {
    val monthlyPrice = plans.find { plan -> plan.type == BillingPlan.Type.MONTHLY }?.price ?: return null
    val annualPrice = plans.find { plan -> plan.type == BillingPlan.Type.ANNUAL }?.price ?: return null

    if (monthlyPrice <= 0 || annualPrice <= 0) return null

    val twelveMonthsPrice = monthlyPrice * 12
    // 整数の割り算で切り捨てる。年額が 12 か月分以上のときは 0 以下になる
    val discountPercent = (twelveMonthsPrice - annualPrice) * 100 / twelveMonthsPrice

    return discountPercent.toInt().takeIf { percent -> percent >= 1 }
}

/** Plus の無料トライアル期間を表すモデル。 */
@Immutable
data class BillingTrialPeriod(val value: Int, val unit: Unit) {
    /** Plus の無料トライアル期間の単位を表す列挙型。 */
    enum class Unit {
        DAY,
        WEEK,
        MONTH,
        YEAR,
        UNKNOWN,
    }
}
