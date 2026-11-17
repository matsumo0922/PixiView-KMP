package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 導入価格のオファーが付いたプランの表示の判定と、プランの並び順を検証するテスト。 */
class BillingPlanIntroOfferTest {

    private val firstMonthOffer = BillingIntroOffer(
        formattedPrice = "¥240",
        period = BillingTrialPeriod(1, BillingTrialPeriod.Unit.MONTH),
        cycleCount = 1,
    )
    private val introMonthlyPlan = billingPlan(BillingPlan.Type.MONTHLY, firstMonthOffer)
    private val annualPlan = billingPlan(BillingPlan.Type.ANNUAL, null)

    @Test
    fun offerPeriodKeepsIntroOffer() {
        val result = applyIntroOfferPeriod(listOf(introMonthlyPlan, annualPlan), isOfferPeriod = true)

        assertEquals(listOf(introMonthlyPlan, annualPlan), result)
    }

    @Test
    fun outsideOfferPeriodRemovesIntroOffer() {
        val result = applyIntroOfferPeriod(listOf(introMonthlyPlan, annualPlan), isOfferPeriod = false)

        assertEquals(listOf(introMonthlyPlan.copy(introOffer = null), annualPlan), result)
    }

    @Test
    fun hasIntroOfferDetectsPlanWithIntroOffer() {
        assertTrue(hasIntroOffer(listOf(introMonthlyPlan, annualPlan)))
        assertFalse(hasIntroOffer(listOf(annualPlan)))
    }

    @Test
    fun firstMonthOnlyIsTrueForOneMonthOnce() {
        assertTrue(firstMonthOffer.isFirstMonthOnly)
        assertFalse(firstMonthOffer.copy(cycleCount = 2).isFirstMonthOnly)
        assertFalse(firstMonthOffer.copy(period = BillingTrialPeriod(1, BillingTrialPeriod.Unit.WEEK)).isFirstMonthOnly)
    }

    @Test
    fun sortBillingPlansPutsMonthlyBeforeAnnual() {
        val result = sortBillingPlans(listOf(annualPlan, introMonthlyPlan))

        assertEquals(listOf(introMonthlyPlan, annualPlan), result)
    }

    private fun billingPlan(type: BillingPlan.Type, introOffer: BillingIntroOffer?) = BillingPlan(
        id = type.name,
        price = 480_000_000L,
        formattedPrice = "¥480",
        type = type,
        trialPeriod = null,
        introOffer = introOffer,
    )
}
