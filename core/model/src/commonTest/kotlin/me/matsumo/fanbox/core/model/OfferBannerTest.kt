package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/** ホームのオファーのバナーの表示条件を検証するテスト。 */
class OfferBannerTest {

    private val firstLaunchTime = 1_700_000_000L
    private val trialPeriod = BillingTrialPeriod(3, BillingTrialPeriod.Unit.DAY)
    private val introOffer = BillingIntroOffer(
        formattedPrice = "¥150",
        period = BillingTrialPeriod(1, BillingTrialPeriod.Unit.MONTH),
        cycleCount = 1,
    )
    private val trialPlans = listOf(
        billingPlan(BillingPlan.Type.MONTHLY, trialPeriod = trialPeriod),
        billingPlan(BillingPlan.Type.ANNUAL),
    )
    private val introPlans = listOf(
        billingPlan(BillingPlan.Type.MONTHLY, introOffer = introOffer),
        billingPlan(BillingPlan.Type.ANNUAL),
    )
    private val noOfferPlans = listOf(
        billingPlan(BillingPlan.Type.MONTHLY),
        billingPlan(BillingPlan.Type.ANNUAL),
    )

    @Test
    fun freeTrialBannerIsShownInOfferPeriod() {
        val result = resolveAt(plans = trialPlans, elapsed = 1.hours)

        assertEquals(OfferBanner.FreeTrial(trialPeriod, cycleNumber = 1), result)
    }

    @Test
    fun freeTrialBannerIsShownInRestPeriod() {
        val result = resolveAt(plans = trialPlans, elapsed = 72.hours + 1.seconds)

        assertEquals(OfferBanner.FreeTrial(trialPeriod, cycleNumber = 1), result)
    }

    @Test
    fun introOfferBannerIsShownWithRemainingTimeInOfferPeriod() {
        val result = resolveAt(plans = introPlans, elapsed = 1.hours)

        assertEquals(OfferBanner.IntroOffer(introOffer, remaining = 71.hours, cycleNumber = 1), result)
    }

    @Test
    fun introOfferBannerIsHiddenPast72Hours() {
        val result = resolveAt(plans = introPlans, elapsed = 72.hours + 1.seconds)

        assertNull(result)
    }

    @Test
    fun introOfferBannerIsShownAgainPast10Days() {
        val result = resolveAt(plans = introPlans, elapsed = 10.days + 1.seconds)

        assertEquals(OfferBanner.IntroOffer(introOffer, remaining = 72.hours - 1.seconds, cycleNumber = 2), result)
    }

    @Test
    fun bannerIsHiddenForPrivilegedUser() {
        val result = resolveAt(plans = trialPlans, elapsed = 1.hours, hasPrivilege = true)

        assertNull(result)
    }

    @Test
    fun bannerIsHiddenWithoutOffer() {
        val result = resolveAt(plans = noOfferPlans, elapsed = 1.hours)

        assertNull(result)
    }

    @Test
    fun bannerDismissedInCycleIsHiddenUntilJustBefore10Days() {
        val result = resolveAt(plans = trialPlans, elapsed = 10.days - 1.seconds, dismissedCycleNumber = 1)

        assertNull(result)
    }

    @Test
    fun freeTrialBannerDismissedInPreviousCycleIsShownPast10Days() {
        val result = resolveAt(plans = trialPlans, elapsed = 10.days + 1.seconds, dismissedCycleNumber = 1)

        assertEquals(OfferBanner.FreeTrial(trialPeriod, cycleNumber = 2), result)
    }

    @Test
    fun introOfferBannerDismissedInPreviousCycleIsShownPast10Days() {
        val result = resolveAt(plans = introPlans, elapsed = 10.days + 1.seconds, dismissedCycleNumber = 1)

        assertEquals(OfferBanner.IntroOffer(introOffer, remaining = 72.hours - 1.seconds, cycleNumber = 2), result)
    }

    @Test
    fun bannerIsHiddenWhenFirstLaunchIsUnrecorded() {
        val result = resolveOfferBanner(
            hasPrivilege = false,
            plans = trialPlans,
            offerCycle = resolveOfferCycle(firstLaunchTime = -1L, currentEpochSeconds = firstLaunchTime),
            dismissedCycleNumber = null,
        )

        assertNull(result)
    }

    private fun resolveAt(
        plans: List<BillingPlan>,
        elapsed: Duration,
        hasPrivilege: Boolean = false,
        dismissedCycleNumber: Int? = null,
    ): OfferBanner? = resolveOfferBanner(
        hasPrivilege = hasPrivilege,
        plans = plans,
        offerCycle = resolveOfferCycle(firstLaunchTime, firstLaunchTime + elapsed.inWholeSeconds),
        dismissedCycleNumber = dismissedCycleNumber,
    )

    private fun billingPlan(
        type: BillingPlan.Type,
        trialPeriod: BillingTrialPeriod? = null,
        introOffer: BillingIntroOffer? = null,
    ) = BillingPlan(
        id = type.name,
        price = 300_000_000L,
        formattedPrice = "¥300",
        type = type,
        trialPeriod = trialPeriod,
        introOffer = introOffer,
    )
}
