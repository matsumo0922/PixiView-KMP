package me.matsumo.fanbox.core.billing.usecase

import android.app.Activity
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.billing.AcknowledgeResult
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.models.ProductDetails
import me.matsumo.fanbox.core.billing.models.ProductItem
import me.matsumo.fanbox.core.billing.models.ProductType
import me.matsumo.fanbox.core.billing.purchaseSingle

class PurchasePlusSubscriptionUseCase(
    private val billingClient: BillingClient,
    private val mainDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(activity: Activity): PurchaseConsumableResult {
        val productDetails = billingClient.queryProductDetails(ProductItem.plusSubscription, ProductType.SUBS)
        val purchaseResult = purchase(activity, productDetails)

        acknowledge(purchaseResult.purchase)

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

    private suspend fun acknowledge(purchase: Purchase): AcknowledgeResult {
        return billingClient.acknowledgePurchase(purchase)
    }
}
