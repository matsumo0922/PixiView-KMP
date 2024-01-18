package me.matsumo.fanbox.core.billing.usecase

import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.models.ProductItem
import me.matsumo.fanbox.core.billing.models.ProductType
import com.android.billingclient.api.Purchase

class VerifyPlusUseCase(
    private val billingClient: BillingClient,
) {
    suspend operator fun invoke(): Purchase? {
        return verifyInAppPurchase() ?: verifySubscriptionPurchase()
    }

    private suspend fun verifyInAppPurchase(): Purchase? {
        billingClient.queryPurchaseHistory(ProductType.INAPP)

        val productDetails = billingClient.queryProductDetails(ProductItem.plus, ProductType.INAPP)
        val purchases = billingClient.queryPurchases(ProductType.INAPP)

        return purchases.find { it.products.contains(productDetails.productId.toString()) }
    }

    private suspend fun verifySubscriptionPurchase(): Purchase? {
        billingClient.queryPurchaseHistory(ProductType.SUBS)

        val productDetails = billingClient.queryProductDetails(ProductItem.plusSubscription, ProductType.SUBS)
        val purchases = billingClient.queryPurchases(ProductType.SUBS)

        return purchases.find { it.products.contains(productDetails.productId.toString()) }
    }
}
