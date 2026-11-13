package me.matsumo.fanbox.core.billing

import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import kotlin.test.Test
import kotlin.test.assertEquals

/** 購入処理の例外を購入結果に分類する処理を検証するテスト。 */
class BillingPurchaseResultTest {

    @Test
    fun toBillingPurchaseResultReturnsCancelledWhenUserCancelled() {
        val exception = PurchasesTransactionException(
            purchasesError = PurchasesError(PurchasesErrorCode.PurchaseCancelledError),
            userCancelled = true,
        )

        assertEquals(BillingPurchaseResult.Cancelled, exception.toBillingPurchaseResult())
    }

    @Test
    fun toBillingPurchaseResultReturnsErrorCodeWhenTransactionFailedWithoutCancel() {
        val exception = PurchasesTransactionException(
            purchasesError = PurchasesError(PurchasesErrorCode.StoreProblemError),
            userCancelled = false,
        )

        assertEquals(BillingPurchaseResult.Failure("StoreProblemError"), exception.toBillingPurchaseResult())
    }

    @Test
    fun toBillingPurchaseResultReturnsErrorCodeForPurchasesException() {
        val exception = PurchasesException(PurchasesError(PurchasesErrorCode.NetworkError))

        assertEquals(BillingPurchaseResult.Failure("NetworkError"), exception.toBillingPurchaseResult())
    }

    @Test
    fun toBillingPurchaseResultReturnsUnknownForOtherException() {
        val exception = IllegalStateException("unexpected")

        assertEquals(BillingPurchaseResult.Failure("unknown"), exception.toBillingPurchaseResult())
    }
}
