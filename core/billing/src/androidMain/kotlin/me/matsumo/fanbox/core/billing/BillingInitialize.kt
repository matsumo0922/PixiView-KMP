package me.matsumo.fanbox.core.billing

import me.matsumo.fanbox.core.billing.di.BillingInitialize

class BillingInitializeImpl(
    private val billingClient: BillingClient,
): BillingInitialize {

    override fun init() {
        billingClient.initialize()
    }
}
