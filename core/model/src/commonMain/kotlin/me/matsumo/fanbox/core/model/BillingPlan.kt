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
    val introOffer: BillingIntroOffer? = null,
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
 * RevenueCat の Offering から取得したプランの一覧。
 *
 * @property offeringId 取得した Offering の識別子。課金のログに送る
 */
@Immutable
data class BillingPlans(
    val offeringId: String,
    val plans: List<BillingPlan>,
)

/**
 * 有料の導入価格（初月割引など）の内容。
 *
 * @property formattedPrice 導入価格の 1 期間あたりの表示用の価格
 * @property period 導入価格の 1 期間の長さ
 * @property cycleCount 導入価格を適用する期間の数
 */
@Immutable
data class BillingIntroOffer(
    val formattedPrice: String,
    val period: BillingTrialPeriod,
    val cycleCount: Int,
) {
    /** 導入価格を適用する期間が、ちょうど最初の 1 か月かどうか。 */
    val isFirstMonthOnly get() = cycleCount == 1 && period == BillingTrialPeriod(1, BillingTrialPeriod.Unit.MONTH)
}

/** 導入価格のオファーが付いたプランがあるかどうか。初月割引の Offering の判定に使う。 */
fun hasIntroOffer(plans: List<BillingPlan>): Boolean = plans.any { plan -> plan.introOffer != null }

/**
 * 提示の期間に合わせて、課金画面に表示するプランを返す。
 *
 * 提示の期間内（[isOfferPeriod] が true）なら [plans] をそのまま返す。期間外なら導入価格を取り除き、通常価格のプランとして返す。
 */
fun applyIntroOfferPeriod(plans: List<BillingPlan>, isOfferPeriod: Boolean): List<BillingPlan> {
    if (isOfferPeriod) return plans

    return plans.map { plan -> plan.copy(introOffer = null) }
}

/**
 * 課金画面に並べる順にプランを並べ替える。
 *
 * 月額、年額の順にする。RevenueCat のパッケージの並び順（position）が同じ値の Offering でも、表示の順を揃えるため。
 */
fun sortBillingPlans(plans: List<BillingPlan>): List<BillingPlan> = plans.sortedBy { plan -> plan.type.ordinal }

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

/**
 * 課金画面に並べるプランを返す。
 *
 * 年額を契約中のユーザーには、月額への変更を提供しないため年額だけを返す。それ以外は [plans] をそのまま返す。
 */
fun filterOfferedPlans(plans: List<BillingPlan>, subscribedPlanType: BillingPlan.Type?): List<BillingPlan> {
    if (subscribedPlanType != BillingPlan.Type.ANNUAL) return plans

    return plans.filter { plan -> plan.type == BillingPlan.Type.ANNUAL }
}

/**
 * 課金画面を開いたときに選択しておくプランを返す。
 *
 * 契約中のプランがなければ [requestedPlanType] をそのまま返す。契約中のプランがあれば、購入できる（契約中ではない）プランを優先し、
 * [requestedPlanType] が購入できなければ最初の購入できるプランを返す。購入できるプランがなければ契約中のプランを返す。
 */
fun resolveInitialSelectedPlanType(
    requestedPlanType: BillingPlan.Type,
    offeredPlans: List<BillingPlan>,
    subscribedPlanType: BillingPlan.Type?,
): BillingPlan.Type {
    if (subscribedPlanType == null) return requestedPlanType

    val purchasablePlanTypes = offeredPlans
        .map { plan -> plan.type }
        .filter { planType -> planType != subscribedPlanType }

    if (requestedPlanType in purchasablePlanTypes) return requestedPlanType

    return purchasablePlanTypes.firstOrNull() ?: subscribedPlanType
}

/** Plus の無料トライアル期間や導入価格の期間など、課金の期間の長さを表すモデル。 */
@Immutable
data class BillingTrialPeriod(val value: Int, val unit: Unit) {
    /** 期間の単位を表す列挙型。 */
    enum class Unit {
        DAY,
        WEEK,
        MONTH,
        YEAR,
        UNKNOWN,
    }
}
