package me.matsumo.fanbox.core.billing.usecase

import android.app.Activity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.ConsumeResult
import me.matsumo.fanbox.core.billing.models.ProductDetails
import me.matsumo.fanbox.core.billing.models.ProductItem
import me.matsumo.fanbox.core.billing.models.ProductType
import me.matsumo.fanbox.core.billing.purchaseSingle

class PurchaseDonateUseCase(
    private val billingClient: BillingClient,
    private val mainDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(activity: Activity, productType: ProductType): PurchaseConsumableResult {
        val productDetails = billingClient.queryProductDetails(ProductItem.plus, productType)
        val purchaseResult = purchase(activity, productDetails)

        // TODO: verification purchaseResult

        consume(purchaseResult)

        return purchaseResult
    }

    private suspend fun purchase(
        activity: Activity,
        productDetails: ProductDetails,
    ): PurchaseConsumableResult = withContext(mainDispatcher) {
        val command = purchaseSingle(productDetails, null)
        val result = billingClient.launchBillingFlow(activity, command)

        PurchaseConsumableResult(command, productDetails, result.billingPurchase)
    }

    private suspend fun consume(
        purchaseConsumableResult: PurchaseConsumableResult,
    ): ConsumeResult {
        return billingClient.consumePurchase(purchaseConsumableResult.purchase)
    }
}
