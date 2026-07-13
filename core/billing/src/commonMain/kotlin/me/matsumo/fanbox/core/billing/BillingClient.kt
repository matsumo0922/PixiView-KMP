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
import com.revenuecat.purchases.kmp.models.Package as RevenueCatPackage

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
        val availablePackages = getAvailablePackages()

        return@withContext customerInfo.toBillingPlusStatus(availablePackages)
    }

    suspend fun purchase(type: BillingPlan.Type): BillingPlusStatus = withContext(ioDispatcher) {
        val product = planCache[type] ?: return@withContext inactivePlusStatus()
        val result = Purchases.sharedInstance.awaitPurchase(product)
        val availablePackages = getAvailablePackages()

        return@withContext result.customerInfo.toBillingPlusStatus(availablePackages)
    }

    suspend fun restore(): BillingPlusStatus = withContext(ioDispatcher) {
        val customerInfo = suspendCancellableCoroutine<CustomerInfo?> { continuation ->
            Purchases.sharedInstance.restorePurchases(
                onSuccess = {
                    Napier.d { "restore success: $it" }
                    continuation.resume(it)
                },
                onError = {
                    Napier.d { "restore error: $it" }
                    continuation.resume(null)
                },
            )
        }
        val availablePackages = getAvailablePackages()

        return@withContext customerInfo?.toBillingPlusStatus(availablePackages) ?: inactivePlusStatus()
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

    private suspend fun getAvailablePackages(): List<RevenueCatPackage> {
        return runCatching {
            Purchases.sharedInstance
                .awaitOfferings()
                .current
                ?.availablePackages
                .orEmpty()
        }.getOrElse { throwable ->
            Napier.w(throwable) { "Failed to fetch offerings for plus status." }
            emptyList()
        }
    }

    private fun CustomerInfo.toBillingPlusStatus(availablePackages: List<RevenueCatPackage>): BillingPlusStatus {
        val entitlement = entitlements[PLUS_ENTITLEMENT_ID]

        Napier.d { "entitlement: $entitlement" }

        val isActive = entitlement?.isActive == true
        val willRenew = entitlement?.willRenew == true
        val isTrialPeriod = entitlement?.periodType == PeriodType.TRIAL
        // iOS でトライアル対応する場合は、この Android 限定ガードを見直す。
        val isTrial = isActive && isAndroidPlatform() && isTrialPeriod
        val planType = resolveBillingPlanType(
            entitlementProductIdentifier = entitlement?.productIdentifier,
            entitlementProductPlanIdentifier = entitlement?.productPlanIdentifier,
            availablePackages = availablePackages,
        )

        return BillingPlusStatus(
            isActive = isActive,
            isTrial = isTrial,
            willRenew = willRenew,
            unsubscribeDetectedAtMillis = entitlement?.unsubscribeDetectedAtMillis,
            planType = planType,
        )
    }

    private fun inactivePlusStatus(): BillingPlusStatus {
        return BillingPlusStatus(
            isActive = false,
            isTrial = false,
            willRenew = false,
            unsubscribeDetectedAtMillis = null,
            planType = BillingPlan.Type.UNKNOWN,
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

internal fun resolveBillingPlanType(
    entitlementProductIdentifier: String?,
    entitlementProductPlanIdentifier: String?,
    availablePackages: List<RevenueCatPackage>,
): BillingPlan.Type {
    if (entitlementProductIdentifier.isNullOrBlank()) return BillingPlan.Type.UNKNOWN

    val matchedPackages = availablePackages.filter { availablePackage ->
        availablePackage.matchesEntitlementProduct(
            entitlementProductIdentifier = entitlementProductIdentifier,
            entitlementProductPlanIdentifier = entitlementProductPlanIdentifier,
        )
    }
    val matchedPackage = matchedPackages.singleOrNull() ?: return BillingPlan.Type.UNKNOWN

    return matchedPackage.packageType.toBillingPlanType()
}

private fun RevenueCatPackage.matchesEntitlementProduct(entitlementProductIdentifier: String, entitlementProductPlanIdentifier: String?): Boolean {
    val planQualifiedProductId = entitlementProductPlanIdentifier?.let { planIdentifier ->
        "$entitlementProductIdentifier:$planIdentifier"
    }
    val exactProductIdMatches = storeProduct.id == entitlementProductIdentifier
    val planQualifiedProductIdMatches = storeProduct.id == planQualifiedProductId
    val packageIdentifierMatches = identifier == entitlementProductIdentifier
    val hasNoPlanIdentifier = entitlementProductPlanIdentifier == null
    val productIdPrefixMatches = storeProduct.id.substringBefore(":") == entitlementProductIdentifier

    return listOf(
        exactProductIdMatches,
        planQualifiedProductIdMatches,
        packageIdentifierMatches,
        hasNoPlanIdentifier && productIdPrefixMatches,
    ).any { isMatched -> isMatched }
}

private fun PackageType.toBillingPlanType(): BillingPlan.Type {
    return when (this) {
        PackageType.MONTHLY -> BillingPlan.Type.MONTHLY
        PackageType.ANNUAL -> BillingPlan.Type.ANNUAL
        else -> BillingPlan.Type.UNKNOWN
    }
}
