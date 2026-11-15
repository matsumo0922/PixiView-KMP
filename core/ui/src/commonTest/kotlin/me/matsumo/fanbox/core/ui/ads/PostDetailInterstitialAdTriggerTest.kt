package me.matsumo.fanbox.core.ui.ads

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 投稿詳細クローズ時のインタースティシャル広告の表示判定を検証するテスト。 */
class PostDetailInterstitialAdTriggerTest {

    private val lastShownEpochSeconds = 10_000L
    private val afterCooldown = lastShownEpochSeconds + INTERSTITIAL_AD_COOLDOWN_SECONDS + 1
    private val beforeCooldown = lastShownEpochSeconds + INTERSTITIAL_AD_COOLDOWN_SECONDS - 1

    @Test
    fun showsWhenCloseCountReachesTrigger() {
        val result = shouldShowPostDetailInterstitialAd(
            postCloseCount = INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT,
            currentEpochSeconds = afterCooldown,
            lastShownEpochSeconds = lastShownEpochSeconds,
            isRetryPending = false,
        )

        assertTrue(result)
    }

    @Test
    fun doesNotShowBetweenTriggerMultiplesWithoutRetry() {
        val result = shouldShowPostDetailInterstitialAd(
            postCloseCount = INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT + 1,
            currentEpochSeconds = afterCooldown,
            lastShownEpochSeconds = lastShownEpochSeconds,
            isRetryPending = false,
        )

        assertFalse(result)
    }

    @Test
    fun retriesOnNextCloseAfterFailedAttempt() {
        val result = shouldShowPostDetailInterstitialAd(
            postCloseCount = INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT + 1,
            currentEpochSeconds = afterCooldown,
            lastShownEpochSeconds = lastShownEpochSeconds,
            isRetryPending = true,
        )

        assertTrue(result)
    }

    @Test
    fun doesNotShowBeforeCooldownEvenAtTrigger() {
        val result = shouldShowPostDetailInterstitialAd(
            postCloseCount = INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT,
            currentEpochSeconds = beforeCooldown,
            lastShownEpochSeconds = lastShownEpochSeconds,
            isRetryPending = false,
        )

        assertFalse(result)
    }

    @Test
    fun doesNotRetryBeforeCooldown() {
        val result = shouldShowPostDetailInterstitialAd(
            postCloseCount = INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT + 1,
            currentEpochSeconds = beforeCooldown,
            lastShownEpochSeconds = lastShownEpochSeconds,
            isRetryPending = true,
        )

        assertFalse(result)
    }
}
