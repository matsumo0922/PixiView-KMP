package me.matsumo.fanbox.core.ui.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** RewardAdShowResult の表示要求 ID による消費を検証するテスト。 */
class RewardAdShowResultHandlerTest {

    @Test
    fun matchingRequestIdConsumesResultAndInvokesCallback() {
        val showResult = RewardAdShowResult(
            requestId = 100L,
            isRewardEarned = true,
        )
        var consumedResult: RewardAdShowResult? = null
        var consumedReward: Boolean? = null

        handleRewardAdShowResult(
            showResult = showResult,
            activeRequestId = 100L,
            consumeShowResult = { result ->
                consumedResult = result
            },
            onActiveResultConsumed = { isRewardEarned ->
                consumedReward = isRewardEarned
            },
        )

        assertEquals(showResult, consumedResult)
        assertEquals(true, consumedReward)
    }

    @Test
    fun mismatchedRequestIdConsumesResultWithoutCallback() {
        val showResult = RewardAdShowResult(
            requestId = 100L,
            isRewardEarned = true,
        )
        var consumedResult: RewardAdShowResult? = null
        var consumedReward: Boolean? = null

        handleRewardAdShowResult(
            showResult = showResult,
            activeRequestId = 200L,
            consumeShowResult = { result ->
                consumedResult = result
            },
            onActiveResultConsumed = { isRewardEarned ->
                consumedReward = isRewardEarned
            },
        )

        assertEquals(showResult, consumedResult)
        assertNull(consumedReward)
    }

    @Test
    fun nullResultDoesNothing() {
        var consumeCount = 0
        var callbackCount = 0

        handleRewardAdShowResult(
            showResult = null,
            activeRequestId = 100L,
            consumeShowResult = {
                consumeCount += 1
            },
            onActiveResultConsumed = {
                callbackCount += 1
            },
        )

        assertEquals(0, consumeCount)
        assertEquals(0, callbackCount)
    }
}
