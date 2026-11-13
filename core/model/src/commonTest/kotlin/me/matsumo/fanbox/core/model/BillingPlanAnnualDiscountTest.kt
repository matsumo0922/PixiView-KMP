package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 年額プランの月額比の割引率の算出を検証するテスト。 */
class BillingPlanAnnualDiscountTest {

    @Test
    fun calculateAnnualDiscountPercentTruncatesFractionForJapanesePrices() {
        // 1 - 3,000 / (300 × 12) = 16.66...%
        val plans = listOf(
            monthlyPlan(priceMicros = 300_000_000L),
            annualPlan(priceMicros = 3_000_000_000L),
        )

        assertEquals(16, calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentTruncatesFractionCloseToNextInteger() {
        // 1 - 9,612 / (1,000 × 12) = 19.9%
        val plans = listOf(
            monthlyPlan(priceMicros = 1_000_000_000L),
            annualPlan(priceMicros = 9_612_000_000L),
        )

        assertEquals(19, calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentReturnsOneWhenDiscountIsExactlyOnePercent() {
        val plans = listOf(
            monthlyPlan(priceMicros = 100_000_000L),
            annualPlan(priceMicros = 1_188_000_000L),
        )

        assertEquals(1, calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentReturnsNullWhenDiscountIsBelowOnePercent() {
        // 1 - 1,190 / (100 × 12) = 0.83...%
        val plans = listOf(
            monthlyPlan(priceMicros = 100_000_000L),
            annualPlan(priceMicros = 1_190_000_000L),
        )

        assertNull(calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentReturnsNullWhenAnnualIsEqualToTwelveMonths() {
        val plans = listOf(
            monthlyPlan(priceMicros = 300_000_000L),
            annualPlan(priceMicros = 3_600_000_000L),
        )

        assertNull(calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentReturnsNullWhenAnnualIsMoreExpensiveThanTwelveMonths() {
        val plans = listOf(
            monthlyPlan(priceMicros = 300_000_000L),
            annualPlan(priceMicros = 4_000_000_000L),
        )

        assertNull(calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentReturnsNullWhenMonthlyPlanIsMissing() {
        val plans = listOf(annualPlan(priceMicros = 3_000_000_000L))

        assertNull(calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentReturnsNullWhenAnnualPlanIsMissing() {
        val plans = listOf(monthlyPlan(priceMicros = 300_000_000L))

        assertNull(calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentReturnsNullWhenMonthlyPriceIsZero() {
        val plans = listOf(
            monthlyPlan(priceMicros = 0L),
            annualPlan(priceMicros = 3_000_000_000L),
        )

        assertNull(calculateAnnualDiscountPercent(plans))
    }

    @Test
    fun calculateAnnualDiscountPercentReturnsNullWhenAnnualPriceIsZero() {
        val plans = listOf(
            monthlyPlan(priceMicros = 300_000_000L),
            annualPlan(priceMicros = 0L),
        )

        assertNull(calculateAnnualDiscountPercent(plans))
    }

    private fun monthlyPlan(priceMicros: Long): BillingPlan = billingPlan(
        type = BillingPlan.Type.MONTHLY,
        priceMicros = priceMicros,
    )

    private fun annualPlan(priceMicros: Long): BillingPlan = billingPlan(
        type = BillingPlan.Type.ANNUAL,
        priceMicros = priceMicros,
    )

    private fun billingPlan(type: BillingPlan.Type, priceMicros: Long): BillingPlan = BillingPlan(
        id = type.name,
        price = priceMicros,
        formattedPrice = priceMicros.toString(),
        type = type,
        trialPeriod = null,
    )
}
