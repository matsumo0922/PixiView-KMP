package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

/**
 * ホームの投稿一覧の最上部に表示する、オファーを案内するバナーの内容。
 *
 * [cycleNumber] は、表示した時点の提示の周期の番号。閉じた周期の記録と、ログに使う。
 */
@Immutable
sealed interface OfferBanner {
    val cycleNumber: Int

    /** ログに送るオファーの種類。 */
    val logValue: String

    /** 無料トライアルを案内するバナー。 */
    @Immutable
    data class FreeTrial(
        val trialPeriod: BillingTrialPeriod,
        override val cycleNumber: Int,
    ) : OfferBanner {
        override val logValue get() = "free_trial"
    }

    /** 初月割引などの導入価格のオファーを案内するバナー。[remaining] は提示の期間の残り時間。 */
    @Immutable
    data class IntroOffer(
        val introOffer: BillingIntroOffer,
        val remaining: Duration,
        override val cycleNumber: Int,
    ) : OfferBanner {
        override val logValue get() = "intro_offer"
    }
}

/**
 * ホームに表示するオファーのバナーを返す。表示しない場合は null を返す。
 *
 * 次のすべてを満たすときに表示する。
 * - Plus の特典が有効でない（[hasPrivilege] が false）
 * - [plans] に、利用できるオファーがある。無料トライアルは常に、導入価格のオファーは提示の期間内だけ利用できる
 * - 現在の周期で、バナーを閉じていない（[dismissedCycleNumber] が現在の周期の番号と異なる）
 *
 * 周期は [offerCycle] で判定する。初回起動の時刻が未記録で周期が分からない（[offerCycle] が null の）場合は、閉じた記録を扱えないため表示しない。
 */
fun resolveOfferBanner(
    hasPrivilege: Boolean,
    plans: List<BillingPlan>,
    offerCycle: OfferCycle?,
    dismissedCycleNumber: Int?,
): OfferBanner? {
    if (hasPrivilege || offerCycle == null) return null
    if (dismissedCycleNumber == offerCycle.number) return null

    val trialPeriod = plans.firstNotNullOfOrNull { plan -> plan.trialPeriod }
    if (trialPeriod != null) return OfferBanner.FreeTrial(trialPeriod, offerCycle.number)

    val introOffer = plans.firstNotNullOfOrNull { plan -> plan.introOffer }
    val remaining = offerCycle.offerPeriodRemaining

    if (introOffer == null || remaining == null) return null

    return OfferBanner.IntroOffer(
        introOffer = introOffer,
        remaining = remaining,
        cycleNumber = offerCycle.number,
    )
}
