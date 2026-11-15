package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

/** 課金画面に並べるプランと、開いたときに選択しておくプランの判定を検証するテスト。 */
class BillingPlanOfferTest {

    private val monthlyPlan = billingPlan(BillingPlan.Type.MONTHLY)
    private val annualPlan = billingPlan(BillingPlan.Type.ANNUAL)
    private val plans = listOf(monthlyPlan, annualPlan)

    @Test
    fun filterOfferedPlansReturnsOnlyAnnualForAnnualSubscriber() {
        val result = filterOfferedPlans(plans, BillingPlan.Type.ANNUAL)

        assertEquals(listOf(annualPlan), result)
    }

    @Test
    fun filterOfferedPlansReturnsAllPlansForMonthlySubscriber() {
        val result = filterOfferedPlans(plans, BillingPlan.Type.MONTHLY)

        assertEquals(plans, result)
    }

    @Test
    fun filterOfferedPlansReturnsAllPlansForNonSubscriber() {
        val result = filterOfferedPlans(plans, null)

        assertEquals(plans, result)
    }

    @Test
    fun initialSelectionKeepsRequestedPlanForNonSubscriber() {
        val result = resolveInitialSelectedPlanType(
            requestedPlanType = BillingPlan.Type.MONTHLY,
            offeredPlans = plans,
            subscribedPlanType = null,
        )

        assertEquals(BillingPlan.Type.MONTHLY, result)
    }

    @Test
    fun initialSelectionMovesFromSubscribedMonthlyToAnnual() {
        val result = resolveInitialSelectedPlanType(
            requestedPlanType = BillingPlan.Type.MONTHLY,
            offeredPlans = plans,
            subscribedPlanType = BillingPlan.Type.MONTHLY,
        )

        assertEquals(BillingPlan.Type.ANNUAL, result)
    }

    @Test
    fun initialSelectionFallsBackToSubscribedPlanWhenNothingIsPurchasable() {
        val result = resolveInitialSelectedPlanType(
            requestedPlanType = BillingPlan.Type.MONTHLY,
            offeredPlans = listOf(annualPlan),
            subscribedPlanType = BillingPlan.Type.ANNUAL,
        )

        assertEquals(BillingPlan.Type.ANNUAL, result)
    }

    private fun billingPlan(type: BillingPlan.Type): BillingPlan = BillingPlan(
        id = type.name,
        price = 1_000_000L,
        formattedPrice = "1",
        type = type,
        trialPeriod = null,
    )
}
