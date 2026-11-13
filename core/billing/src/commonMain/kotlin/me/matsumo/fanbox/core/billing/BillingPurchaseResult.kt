package me.matsumo.fanbox.core.billing

import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import me.matsumo.fanbox.core.model.BillingPlusStatus

/** Plus の購入を試みた結果。 */
sealed interface BillingPurchaseResult {
    /** 購入が成立し、Plus が有効になった。 */
    data class Success(val status: BillingPlusStatus) : BillingPurchaseResult

    /** ユーザーがストアの購入画面で購入をキャンセルした。 */
    data object Cancelled : BillingPurchaseResult

    /**
     * キャンセル以外の理由で購入が成立しなかった。
     *
     * [errorType] は RevenueCat の PurchasesErrorCode の名前、またはアプリ側で判定した失敗の種別。
     */
    data class Failure(val errorType: String) : BillingPurchaseResult
}

/** 購入処理で発生した例外を、キャンセルと失敗の種別に分類する。 */
internal fun Throwable.toBillingPurchaseResult(): BillingPurchaseResult {
    val isUserCancelled = this is PurchasesTransactionException && userCancelled
    if (isUserCancelled) return BillingPurchaseResult.Cancelled

    val errorType = (this as? PurchasesException)?.code?.name ?: "unknown"

    return BillingPurchaseResult.Failure(errorType)
}
