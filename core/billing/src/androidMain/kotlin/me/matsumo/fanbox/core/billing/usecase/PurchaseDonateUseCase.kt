package me.matsumo.fanbox.core.billing.usecase

import android.app.Activity
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.ConsumeResult
import me.matsumo.fanbox.core.billing.models.ProductDetails
import me.matsumo.fanbox.core.billing.models.ProductItem
import me.matsumo.fanbox.core.billing.models.ProductType
import me.matsumo.fanbox.core.billing.purchaseSingle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PurchaseDonateUseCase(
    private val billingClient: BillingClient,
    private val mainDispatcher: CoroutineDispatcher,
) {

    suspend fun execute(activity: Activity, productType: ProductType): PurchaseConsumableResult {
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
