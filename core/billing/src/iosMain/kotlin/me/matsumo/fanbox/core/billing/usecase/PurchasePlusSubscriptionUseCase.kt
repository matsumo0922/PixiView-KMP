package me.matsumo.fanbox.core.billing.usecase

import me.matsumo.fanbox.core.billing.BillingClient
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKProduct

class PurchasePlusSubscriptionUseCase(
    private val billingClient: BillingClient,
) {
    suspend operator fun invoke() {
        val product = billingClient.queryProductDetails(setOf("plus")).first()
        val payment = SKPayment.paymentWithProduct(product)

        SKPaymentQueue.defaultQueue().addPayment(payment)
    }
}
