package me.matsumo.fanbox.core.billing

import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PresentedOfferingContext
import com.revenuecat.purchases.kmp.models.Price
import com.revenuecat.purchases.kmp.models.ProductCategory
import com.revenuecat.purchases.kmp.models.ProductType
import com.revenuecat.purchases.kmp.models.PurchasingData
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreProductDiscount
import com.revenuecat.purchases.kmp.models.SubscriptionOption
import com.revenuecat.purchases.kmp.models.SubscriptionOptions
import me.matsumo.fanbox.core.model.BillingPlan
import kotlin.test.Test
import kotlin.test.assertEquals

/** RevenueCat の Offering から現在の Plus プラン種別を解決する処理を検証するテスト。 */
class BillingPlanTypeResolverTest {

    @Test
    fun resolveBillingPlanTypeReturnsAnnualWhenProductIdentifierMatchesAnnualPackage() {
        val packages = listOf(
            FakePackage(
                identifier = "monthly",
                packageType = PackageType.MONTHLY,
                productId = "plus_monthly",
            ),
            FakePackage(
                identifier = "annual",
                packageType = PackageType.ANNUAL,
                productId = "plus_annual",
            ),
        )

        val result = resolveBillingPlanType(
            entitlementProductIdentifier = "plus_annual",
            entitlementProductPlanIdentifier = null,
            availablePackages = packages,
        )

        assertEquals(BillingPlan.Type.ANNUAL, result)
    }

    @Test
    fun resolveBillingPlanTypeReturnsAnnualWhenGoogleBasePlanMatchesAnnualPackage() {
        val packages = listOf(
            FakePackage(
                identifier = "monthly",
                packageType = PackageType.MONTHLY,
                productId = "plus:monthly",
            ),
            FakePackage(
                identifier = "annual",
                packageType = PackageType.ANNUAL,
                productId = "plus:annual",
            ),
        )

        val result = resolveBillingPlanType(
            entitlementProductIdentifier = "plus",
            entitlementProductPlanIdentifier = "annual",
            availablePackages = packages,
        )

        assertEquals(BillingPlan.Type.ANNUAL, result)
    }

    @Test
    fun resolveBillingPlanTypeReturnsUnknownWhenProductIdentifierMatchesMultipleBasePlans() {
        val packages = listOf(
            FakePackage(
                identifier = "monthly",
                packageType = PackageType.MONTHLY,
                productId = "plus:monthly",
            ),
            FakePackage(
                identifier = "annual",
                packageType = PackageType.ANNUAL,
                productId = "plus:annual",
            ),
        )

        val result = resolveBillingPlanType(
            entitlementProductIdentifier = "plus",
            entitlementProductPlanIdentifier = null,
            availablePackages = packages,
        )

        assertEquals(BillingPlan.Type.UNKNOWN, result)
    }

    @Test
    fun resolveBillingPlanTypeReturnsUnknownWhenOfferingDoesNotContainMatchedPackage() {
        val packages = listOf(
            FakePackage(
                identifier = "monthly",
                packageType = PackageType.MONTHLY,
                productId = "plus_monthly",
            ),
        )

        val result = resolveBillingPlanType(
            entitlementProductIdentifier = "plus_annual",
            entitlementProductPlanIdentifier = null,
            availablePackages = packages,
        )

        assertEquals(BillingPlan.Type.UNKNOWN, result)
    }

    /** テスト用の RevenueCat Package 実装。 */
    private class FakePackage(
        override val identifier: String,
        override val packageType: PackageType,
        productId: String,
    ) : Package {
        override val storeProduct: StoreProduct = FakeStoreProduct(productId)
        override val presentedOfferingContext: PresentedOfferingContext =
            PresentedOfferingContext("default", null, null)
    }

    /** テスト用の RevenueCat StoreProduct 実装。 */
    private class FakeStoreProduct(
        override val id: String,
    ) : StoreProduct {
        override val type: ProductType = ProductType.SUBS
        override val category: ProductCategory = ProductCategory.SUBSCRIPTION
        override val price: Price = Price("JPY 0", 0L, "JPY")
        override val title: String = id
        override val localizedDescription: String? = null
        override val period: Period? = null
        override val subscriptionOptions: SubscriptionOptions? = null
        override val defaultOption: SubscriptionOption? = null
        override val discounts: List<StoreProductDiscount> = emptyList()
        override val introductoryDiscount: StoreProductDiscount? = null
        override val purchasingData: PurchasingData = FakePurchasingData(id)
        override val presentedOfferingContext: PresentedOfferingContext? = null
    }

    /** テスト用の RevenueCat PurchasingData 実装。 */
    private class FakePurchasingData(
        override val productId: String,
    ) : PurchasingData {
        override val productType: ProductType = ProductType.SUBS
    }
}
