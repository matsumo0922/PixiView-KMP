package me.matsumo.fanbox.core.billing

class BillingInitializeImpl(
    private val billingClient: BillingClient,
): BillingInitialize {

    override fun init() {
        billingClient.initialize()
    }

    override fun finish() {
        // do nothing
    }
}
