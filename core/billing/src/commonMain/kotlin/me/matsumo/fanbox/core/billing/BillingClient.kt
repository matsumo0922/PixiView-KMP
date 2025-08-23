package me.matsumo.fanbox.core.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.BillingPlan

class BillingClient(
    val pixiViewConfig: PixiViewConfig,
    val ioDispatcher: CoroutineDispatcher,
) {
    private val planCache = mutableMapOf<BillingPlan.Type, StoreProduct>()

    init {
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(pixiViewConfig.purchaseApiKey.orEmpty())
    }

    suspend fun hasPlus(): Boolean = withContext(ioDispatcher) {
        val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
        val entitlement = customerInfo.entitlements[PLUS_ENTITLEMENT_ID]

        return@withContext entitlement?.isActive == true
    }

    suspend fun purchase(type: BillingPlan.Type) = withContext(ioDispatcher) {
        val product = planCache[type] ?: return@withContext false
        val result = Purchases.sharedInstance.awaitPurchase(product)
        val entitlement = result.customerInfo.entitlements[PLUS_ENTITLEMENT_ID]

        return@withContext entitlement?.isActive == true
    }

    suspend fun getPlans() = withContext(ioDispatcher) {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        val current = offerings.current

        return@withContext if (current != null && current.availablePackages.isNotEmpty()) {
            current.availablePackages.map { pkg ->
                planCache[pkg.packageType.toBillingPlanType()] = pkg.storeProduct

                BillingPlan(
                    id = pkg.identifier,
                    price = pkg.storeProduct.price.amountMicros,
                    formattedPrice = pkg.storeProduct.price.formatted,
                    type = pkg.packageType.toBillingPlanType(),
                )
            }
        } else {
            null
        }
    }

    private fun PackageType.toBillingPlanType(): BillingPlan.Type = when (this) {
        PackageType.MONTHLY -> BillingPlan.Type.MONTHLY
        PackageType.ANNUAL -> BillingPlan.Type.ANNUAL
        else -> BillingPlan.Type.UNKNOWN
    }

    companion object {
        private const val PLUS_ENTITLEMENT_ID = "Plus"
    }
}
