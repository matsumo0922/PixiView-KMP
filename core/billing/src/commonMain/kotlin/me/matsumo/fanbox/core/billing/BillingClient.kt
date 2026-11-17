package me.matsumo.fanbox.core.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.SuccessfulPurchase
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.GoogleReplacementMode
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodType
import com.revenuecat.purchases.kmp.models.PeriodUnit
import com.revenuecat.purchases.kmp.models.Store
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.SubscriptionOption
import com.revenuecat.purchases.kmp.models.freePhase
import com.revenuecat.purchases.kmp.models.introPhase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.BillingIntroOffer
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.BillingPlans
import me.matsumo.fanbox.core.model.BillingPlusStatus
import me.matsumo.fanbox.core.model.BillingTrialPeriod
import me.matsumo.fanbox.core.model.sortBillingPlans
import kotlin.coroutines.resume
import com.revenuecat.purchases.kmp.models.Package as RevenueCatPackage

/** RevenueCat を利用して Plus 課金状態を扱うクライアント。 */
class BillingClient(val pixiViewConfig: PixiViewConfig, val ioDispatcher: CoroutineDispatcher) {
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

    /**
     * [type] のプランを購入する。
     *
     * 導入価格のオファーが付いたプランは、[isIntroOfferPeriod] が true ならそのオファーを、false なら基本プランだけを購入する。
     * それ以外のプランは、RevenueCat の SDK が選ぶ既定のオプション（無料トライアルなど）で購入する。
     */
    suspend fun purchase(type: BillingPlan.Type, isIntroOfferPeriod: Boolean): BillingPurchaseResult = withContext(ioDispatcher) {
        val product = planCache[type] ?: return@withContext BillingPurchaseResult.Failure("product_not_found")
        val oldProductId = getPlanChangeOldProductId(type)
        val purchase = suspendRunCatching { awaitPurchase(product, isIntroOfferPeriod, oldProductId) }.getOrElse { throwable ->
            Napier.w(throwable) { "Failed to purchase $type." }
            return@withContext throwable.toBillingPurchaseResult()
        }
        val status = purchase.customerInfo.toBillingPlusStatus(getAvailablePackages())

        return@withContext if (status.isActive) {
            BillingPurchaseResult.Success(status)
        } else {
            BillingPurchaseResult.Failure("entitlement_inactive")
        }
    }

    private suspend fun awaitPurchase(
        product: StoreProduct,
        isIntroOfferPeriod: Boolean,
        oldProductId: String?,
    ): SuccessfulPurchase {
        val introOfferPurchaseOption = product.resolveIntroOfferPurchaseOption(isIntroOfferPeriod)

        if (introOfferPurchaseOption != null) return awaitPurchase(introOfferPurchaseOption, oldProductId)
        if (oldProductId == null) return Purchases.sharedInstance.awaitPurchase(product)

        // 同じ定期購入の中で自動更新のプランへ変更するときに有効なモードは、CHARGE_FULL_PRICE と WITHOUT_PRORATION だけ。
        // 変更の時点で新しいプランの料金を請求し、変更元の残りの金額分は新しいプランの期間に加算させる
        return Purchases.sharedInstance.awaitPurchase(
            storeProduct = product,
            oldProductId = oldProductId,
            replacementMode = GoogleReplacementMode.CHARGE_FULL_PRICE,
        )
    }

    private suspend fun awaitPurchase(subscriptionOption: SubscriptionOption, oldProductId: String?): SuccessfulPurchase {
        if (oldProductId == null) return Purchases.sharedInstance.awaitPurchase(subscriptionOption)

        // 変更のモードは、StoreProduct で購入する場合と同じ理由で CHARGE_FULL_PRICE にする
        return Purchases.sharedInstance.awaitPurchase(
            subscriptionOption = subscriptionOption,
            oldProductId = oldProductId,
            replacementMode = GoogleReplacementMode.CHARGE_FULL_PRICE,
        )
    }

    private suspend fun getPlanChangeOldProductId(targetPlanType: BillingPlan.Type): String? {
        val customerInfo = suspendRunCatching { Purchases.sharedInstance.awaitCustomerInfo() }.getOrElse { throwable ->
            Napier.w(throwable) { "Failed to fetch customer info for plan change." }
            return null
        }
        val entitlement = customerInfo.entitlements[PLUS_ENTITLEMENT_ID]
        val currentPlanType = customerInfo.toBillingPlusStatus(getAvailablePackages()).planType

        return resolvePlanChangeOldProductId(
            isEntitlementActive = entitlement?.isActive == true,
            entitlementStore = entitlement?.store,
            entitlementProductIdentifier = entitlement?.productIdentifier,
            currentPlanType = currentPlanType,
            targetPlanType = targetPlanType,
        )
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

    /** 現在の Offering のプランを返す。Offering がないか、パッケージがない場合は null を返す。 */
    suspend fun getPlans(): BillingPlans? = withContext(ioDispatcher) {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        val current = offerings.current

        if (current == null || current.availablePackages.isEmpty()) return@withContext null

        val plans = current.availablePackages.map { pkg ->
            val planType = pkg.packageType.toBillingPlanType()
            planCache[planType] = pkg.storeProduct

            BillingPlan(
                id = pkg.identifier,
                price = pkg.storeProduct.price.amountMicros,
                formattedPrice = pkg.storeProduct.price.formatted,
                type = planType,
                trialPeriod = pkg.storeProduct.getTrialPeriod(),
                introOffer = pkg.storeProduct.getIntroOffer(),
            )
        }

        return@withContext BillingPlans(
            offeringId = current.identifier,
            plans = sortBillingPlans(plans),
        )
    }

    /**
     * 契約中のプラン種別の判定に使うパッケージを返す。
     *
     * 現在の Offering だけでなく、すべての Offering のパッケージを返す。現在の Offering にない基本プラン
     * （A/B テストの別の群の基本プランなど）の契約者も、プラン種別を判定できるようにするため。
     */
    private suspend fun getAvailablePackages(): List<RevenueCatPackage> = runCatching {
        val offerings = Purchases.sharedInstance.awaitOfferings()

        offerings.all.values.flatMap { offering -> offering.availablePackages }
    }.getOrElse { throwable ->
        Napier.w(throwable) { "Failed to fetch offerings for plus status." }
        emptyList()
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

    private fun inactivePlusStatus(): BillingPlusStatus = BillingPlusStatus(
        isActive = false,
        isTrial = false,
        willRenew = false,
        unsubscribeDetectedAtMillis = null,
        planType = BillingPlan.Type.UNKNOWN,
    )

    private fun StoreProduct.getTrialPeriod(): BillingTrialPeriod? {
        val freePhase = subscriptionOptions?.freeTrial?.freePhase ?: return null

        return freePhase.billingPeriod.toBillingTrialPeriod()
    }

    /**
     * 有料の導入価格のオファーを返す。
     *
     * 無料トライアルのオファーもある場合は null を返す。RevenueCat の SDK の既定の選択は無料トライアルを優先するため、
     * 導入価格のオファーとして扱わない。
     */
    private fun StoreProduct.getIntroOffer(): BillingIntroOffer? {
        val subscriptionOptions = subscriptionOptions ?: return null

        if (subscriptionOptions.freeTrial != null) return null

        val introPhase = subscriptionOptions.introOffer?.introPhase ?: return null

        return BillingIntroOffer(
            formattedPrice = introPhase.price.formatted,
            period = introPhase.billingPeriod.toBillingTrialPeriod(),
            cycleCount = introPhase.billingCycleCount ?: 1,
        )
    }

    /**
     * 導入価格のオファーが付いた製品で購入するオプションを返す。提示の期間内は導入価格のオファーを、期間外は基本プランを返す。
     *
     * 導入価格のオファーが付いていない製品は null を返し、RevenueCat の SDK の既定のオプションで購入させる。
     * 期間外に製品をそのまま購入すると、SDK の既定の選択が導入価格のオファーを自動で適用するため、基本プランを明示する。
     */
    private fun StoreProduct.resolveIntroOfferPurchaseOption(isIntroOfferPeriod: Boolean): SubscriptionOption? {
        if (getIntroOffer() == null) return null

        val subscriptionOptions = subscriptionOptions ?: return null

        return if (isIntroOfferPeriod) subscriptionOptions.introOffer else subscriptionOptions.basePlan
    }

    private fun Period.toBillingTrialPeriod(): BillingTrialPeriod = BillingTrialPeriod(
        value = value,
        unit = unit.toBillingTrialPeriodUnit(),
    )

    private fun PeriodUnit.toBillingTrialPeriodUnit(): BillingTrialPeriod.Unit = when (this) {
        PeriodUnit.DAY -> BillingTrialPeriod.Unit.DAY
        PeriodUnit.WEEK -> BillingTrialPeriod.Unit.WEEK
        PeriodUnit.MONTH -> BillingTrialPeriod.Unit.MONTH
        PeriodUnit.YEAR -> BillingTrialPeriod.Unit.YEAR
        PeriodUnit.UNKNOWN -> BillingTrialPeriod.Unit.UNKNOWN
    }

    private fun isAndroidPlatform(): Boolean = pixiViewConfig.platform.equals(ANDROID_PLATFORM, ignoreCase = true)

    /** BillingClient で利用する固定値。 */
    companion object {
        /** Plus 権限の Entitlement ID。 */
        private const val PLUS_ENTITLEMENT_ID = "Plus"

        /** Android プラットフォーム名。 */
        private const val ANDROID_PLATFORM = "Android"
    }
}

/**
 * Plus の契約中に別のプランを購入するときに、置き換える変更元の商品 ID を返す。
 *
 * Play ストアで購入した Plus が有効で、購入するプランが現在のプランと異なる場合だけ返す。
 * Play ストアの契約がない場合（RevenueCat から付与した権限など）、未契約の場合、どちらかのプランが分からない場合は null を返し、通常の購入にする。
 * Play ストアの [entitlementProductIdentifier] は、基本プランを含まない定期購入の ID である。
 */
internal fun resolvePlanChangeOldProductId(
    isEntitlementActive: Boolean,
    entitlementStore: Store?,
    entitlementProductIdentifier: String?,
    currentPlanType: BillingPlan.Type,
    targetPlanType: BillingPlan.Type,
): String? {
    val hasActivePlayStoreSubscription = isEntitlementActive && entitlementStore == Store.PLAY_STORE
    val arePlanTypesKnown = currentPlanType != BillingPlan.Type.UNKNOWN && targetPlanType != BillingPlan.Type.UNKNOWN
    val isDifferentPlan = arePlanTypesKnown && currentPlanType != targetPlanType

    if (!hasActivePlayStoreSubscription || !isDifferentPlan) return null

    return entitlementProductIdentifier?.takeIf { productIdentifier -> productIdentifier.isNotBlank() }
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
    // 同じ製品が複数の Offering に入っている場合も、プラン種別が 1 つに決まれば、その種別にする
    val matchedPackageType = matchedPackages
        .map { matchedPackage -> matchedPackage.packageType }
        .distinct()
        .singleOrNull() ?: return BillingPlan.Type.UNKNOWN

    return matchedPackageType.toBillingPlanType()
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

private fun PackageType.toBillingPlanType(): BillingPlan.Type = when (this) {
    PackageType.MONTHLY -> BillingPlan.Type.MONTHLY
    PackageType.ANNUAL -> BillingPlan.Type.ANNUAL
    else -> BillingPlan.Type.UNKNOWN
}
