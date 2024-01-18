package me.matsumo.fanbox.core.billing.usecase

import com.android.billingclient.api.Purchase
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.ConsumeResult

class ConsumePlusUseCase(
    private val billingClient: BillingClient,
) {
    suspend operator fun invoke(purchase: Purchase): ConsumeResult {
        return billingClient.consumePurchase(purchase)
    }
}
