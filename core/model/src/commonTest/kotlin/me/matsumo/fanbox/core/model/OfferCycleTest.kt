package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/** 初回起動からの経過時間による、オファーの提示の周期の判定を検証するテスト。 */
class OfferCycleTest {

    private val firstLaunchTime = 1_700_000_000L

    @Test
    fun firstLaunchIsInFirstOfferPeriod() {
        val result = resolveAt(Duration.ZERO)

        assertEquals(OfferCycle(number = 1, offerPeriodRemaining = 72.hours), result)
    }

    @Test
    fun justBefore72HoursIsStillInFirstOfferPeriod() {
        val result = resolveAt(72.hours - 1.seconds)

        assertEquals(OfferCycle(number = 1, offerPeriodRemaining = 1.seconds), result)
    }

    @Test
    fun past72HoursIsInFirstRestPeriod() {
        val result = resolveAt(72.hours + 1.seconds)

        assertEquals(OfferCycle(number = 1, offerPeriodRemaining = null), result)
    }

    @Test
    fun justBefore10DaysIsStillInFirstRestPeriod() {
        val result = resolveAt(10.days - 1.seconds)

        assertEquals(OfferCycle(number = 1, offerPeriodRemaining = null), result)
    }

    @Test
    fun past10DaysIsInSecondOfferPeriod() {
        val result = resolveAt(10.days + 1.seconds)

        assertEquals(OfferCycle(number = 2, offerPeriodRemaining = 72.hours - 1.seconds), result)
    }

    @Test
    fun past13DaysIsInSecondRestPeriod() {
        val result = resolveAt(13.days + 1.seconds)

        assertEquals(OfferCycle(number = 2, offerPeriodRemaining = null), result)
    }

    @Test
    fun past20DaysIsInThirdOfferPeriod() {
        val result = resolveAt(20.days + 1.seconds)

        assertEquals(OfferCycle(number = 3, offerPeriodRemaining = 72.hours - 1.seconds), result)
    }

    @Test
    fun offerPeriodRepeatsWithoutLimit() {
        val result = resolveAt(990.days + 1.hours)

        assertEquals(OfferCycle(number = 100, offerPeriodRemaining = 71.hours), result)
    }

    @Test
    fun clockBeforeFirstLaunchIsTreatedAsFirstLaunch() {
        val result = resolveAt((-1).hours)

        assertEquals(OfferCycle(number = 1, offerPeriodRemaining = 72.hours), result)
    }

    @Test
    fun unrecordedFirstLaunchReturnsNull() {
        val result = resolveOfferCycle(firstLaunchTime = -1L, currentEpochSeconds = firstLaunchTime)

        assertNull(result)
    }

    private fun resolveAt(elapsed: Duration): OfferCycle? = resolveOfferCycle(
        firstLaunchTime = firstLaunchTime,
        currentEpochSeconds = firstLaunchTime + elapsed.inWholeSeconds,
    )
}
