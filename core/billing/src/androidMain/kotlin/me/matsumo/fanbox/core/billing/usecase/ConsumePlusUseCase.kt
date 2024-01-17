package me.matsumo.fanbox.core.billing.usecase

import com.android.billingclient.api.Purchase
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.ConsumeResult
import javax.inject.Inject

class ConsumePlusUseCase(
    private val billingClient: BillingClient,
) {
    suspend fun execute(purchase: Purchase): ConsumeResult {
        return billingClient.consumePurchase(purchase)
    }
}
