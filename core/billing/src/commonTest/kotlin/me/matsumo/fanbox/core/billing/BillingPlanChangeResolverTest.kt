package me.matsumo.fanbox.core.billing

import com.revenuecat.purchases.kmp.models.Store
import me.matsumo.fanbox.core.model.BillingPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Plus の契約中に別のプランを購入するときの、変更元の商品 ID の判定を検証するテスト。 */
class BillingPlanChangeResolverTest {

    @Test
    fun returnsSubscriptionIdWhenPlayStoreMonthlyUserBuysAnnual() {
        val result = resolvePlanChangeOldProductId(
            isEntitlementActive = true,
            entitlementStore = Store.PLAY_STORE,
            entitlementProductIdentifier = "plus_subscription",
            currentPlanType = BillingPlan.Type.MONTHLY,
            targetPlanType = BillingPlan.Type.ANNUAL,
        )

        assertEquals("plus_subscription", result)
    }

    @Test
    fun returnsNullWhenEntitlementIsNotFromPlayStore() {
        val result = resolvePlanChangeOldProductId(
            isEntitlementActive = true,
            entitlementStore = Store.PROMOTIONAL,
            entitlementProductIdentifier = "rc_promo_plus_monthly",
            currentPlanType = BillingPlan.Type.MONTHLY,
            targetPlanType = BillingPlan.Type.ANNUAL,
        )

        assertNull(result)
    }

    @Test
    fun returnsNullWhenEntitlementIsInactive() {
        val result = resolvePlanChangeOldProductId(
            isEntitlementActive = false,
            entitlementStore = Store.PLAY_STORE,
            entitlementProductIdentifier = "plus_subscription",
            currentPlanType = BillingPlan.Type.MONTHLY,
            targetPlanType = BillingPlan.Type.ANNUAL,
        )

        assertNull(result)
    }

    @Test
    fun returnsNullWhenUserHasNoEntitlement() {
        val result = resolvePlanChangeOldProductId(
            isEntitlementActive = false,
            entitlementStore = null,
            entitlementProductIdentifier = null,
            currentPlanType = BillingPlan.Type.UNKNOWN,
            targetPlanType = BillingPlan.Type.MONTHLY,
        )

        assertNull(result)
    }

    @Test
    fun returnsNullWhenTargetIsCurrentPlan() {
        val result = resolvePlanChangeOldProductId(
            isEntitlementActive = true,
            entitlementStore = Store.PLAY_STORE,
            entitlementProductIdentifier = "plus_subscription",
            currentPlanType = BillingPlan.Type.ANNUAL,
            targetPlanType = BillingPlan.Type.ANNUAL,
        )

        assertNull(result)
    }

    @Test
    fun returnsNullWhenCurrentPlanIsUnknown() {
        val result = resolvePlanChangeOldProductId(
            isEntitlementActive = true,
            entitlementStore = Store.PLAY_STORE,
            entitlementProductIdentifier = "plus_subscription",
            currentPlanType = BillingPlan.Type.UNKNOWN,
            targetPlanType = BillingPlan.Type.ANNUAL,
        )

        assertNull(result)
    }
}
