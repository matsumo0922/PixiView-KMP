package me.matsumo.fanbox.core.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodType
import com.revenuecat.purchases.kmp.models.PeriodUnit
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.freePhase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.BillingPlusStatus
import me.matsumo.fanbox.core.model.BillingTrialPeriod
import kotlin.coroutines.resume

/** RevenueCat を利用して Plus 課金状態を扱うクライアント。 */
class BillingClient(
    val pixiViewConfig: PixiViewConfig,
    val ioDispatcher: CoroutineDispatcher,
) {
    private val planCache = mutableMapOf<BillingPlan.Type, StoreProduct>()

    init {
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(pixiViewConfig.purchaseApiKey.orEmpty())
    }

    suspend fun getPlusStatus(): BillingPlusStatus = withContext(ioDispatcher) {
        val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()

        return@withContext customerInfo.toBillingPlusStatus()
    }

    suspend fun purchase(type: BillingPlan.Type): BillingPlusStatus = withContext(ioDispatcher) {
        val product = planCache[type] ?: return@withContext BillingPlusStatus(
            isActive = false,
            isTrial = false,
        )
        val result = Purchases.sharedInstance.awaitPurchase(product)

        return@withContext result.customerInfo.toBillingPlusStatus()
    }

    suspend fun restore(): BillingPlusStatus = suspendCancellableCoroutine { continuation ->
        Purchases.sharedInstance.restorePurchases(
            onSuccess = {
                Napier.d { "restore success: $it" }
                continuation.resume(it.toBillingPlusStatus())
            },
            onError = {
                Napier.d { "restore error: $it" }
                continuation.resume(
                    BillingPlusStatus(
                        isActive = false,
                        isTrial = false,
                    ),
                )
            },
        )
    }

    suspend fun getPlans() = withContext(ioDispatcher) {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        val current = offerings.current

        return@withContext if (current != null && current.availablePackages.isNotEmpty()) {
            current.availablePackages.map { pkg ->
                val planType = pkg.packageType.toBillingPlanType()
                planCache[planType] = pkg.storeProduct

                BillingPlan(
                    id = pkg.identifier,
                    price = pkg.storeProduct.price.amountMicros,
                    formattedPrice = pkg.storeProduct.price.formatted,
                    type = planType,
                    trialPeriod = pkg.storeProduct.getTrialPeriod(),
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

    private fun CustomerInfo.toBillingPlusStatus(): BillingPlusStatus {
        val entitlement = entitlements[PLUS_ENTITLEMENT_ID]

        Napier.d { "entitlement: $entitlement" }

        val isActive = entitlement?.isActive == true
        val isTrialPeriod = entitlement?.periodType == PeriodType.TRIAL
        val isTrial = isActive && isAndroidPlatform() && isTrialPeriod

        return BillingPlusStatus(
            isActive = isActive,
            isTrial = isTrial,
        )
    }

    private fun StoreProduct.getTrialPeriod(): BillingTrialPeriod? {
        val freePhase = subscriptionOptions?.freeTrial?.freePhase ?: return null

        return freePhase.billingPeriod.toBillingTrialPeriod()
    }

    private fun Period.toBillingTrialPeriod(): BillingTrialPeriod {
        return BillingTrialPeriod(
            value = value,
            unit = unit.toBillingTrialPeriodUnit(),
        )
    }

    private fun PeriodUnit.toBillingTrialPeriodUnit(): BillingTrialPeriod.Unit {
        return when (this) {
            PeriodUnit.DAY -> BillingTrialPeriod.Unit.DAY
            PeriodUnit.WEEK -> BillingTrialPeriod.Unit.WEEK
            PeriodUnit.MONTH -> BillingTrialPeriod.Unit.MONTH
            PeriodUnit.YEAR -> BillingTrialPeriod.Unit.YEAR
            PeriodUnit.UNKNOWN -> BillingTrialPeriod.Unit.UNKNOWN
        }
    }

    private fun isAndroidPlatform(): Boolean {
        return pixiViewConfig.platform.equals(ANDROID_PLATFORM, ignoreCase = true)
    }

    /** BillingClient で利用する固定値。 */
    companion object {
        /** Plus 権限の Entitlement ID。 */
        private const val PLUS_ENTITLEMENT_ID = "Plus"

        /** Android プラットフォーム名。 */
        private const val ANDROID_PLATFORM = "Android"
    }
}
