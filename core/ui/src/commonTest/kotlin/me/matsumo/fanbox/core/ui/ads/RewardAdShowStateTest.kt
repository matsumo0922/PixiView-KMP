package me.matsumo.fanbox.core.ui.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** RewardAdShowState の表示完了順序を検証するテスト。 */
class RewardAdShowStateTest {

    @Test
    fun completeByDismissalReturnsFalseWithoutReward() {
        val showState = RewardAdShowState()

        assertEquals(false, showState.completeByDismissal())
    }

    @Test
    fun rewardIsReturnedOnlyAfterDismissal() {
        val showState = RewardAdShowState()

        showState.recordRewardEarned()

        assertEquals(true, showState.completeByDismissal())
    }

    @Test
    fun duplicateRewardAndDismissalCompleteOnce() {
        val showState = RewardAdShowState()

        showState.recordRewardEarned()
        showState.recordRewardEarned()

        assertEquals(true, showState.completeByDismissal())
        assertNull(showState.completeByDismissal())
        assertNull(showState.completeWithoutReward())
    }

    @Test
    fun failedToShowCompletesWithoutReward() {
        val showState = RewardAdShowState()

        showState.recordRewardEarned()

        assertEquals(false, showState.completeWithoutReward())
        assertNull(showState.completeByDismissal())
    }

    @Test
    fun rewardAfterCancellationIsIgnored() {
        val showState = RewardAdShowState()

        assertEquals(false, showState.completeWithoutReward())

        showState.recordRewardEarned()

        assertNull(showState.completeByDismissal())
    }
}
