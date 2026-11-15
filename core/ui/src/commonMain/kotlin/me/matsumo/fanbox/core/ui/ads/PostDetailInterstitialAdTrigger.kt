package me.matsumo.fanbox.core.ui.ads

/**
 * 投稿詳細を閉じたときに、インタースティシャル広告の表示を試みるかを判定する。
 *
 * クローズ回数が閾値の倍数に達したとき、または前回の試行で表示できなかったときに試みる。
 * どちらの場合も、前回の表示からクールダウンが経過していることを条件とする。
 */
fun shouldShowPostDetailInterstitialAd(
    postCloseCount: Int,
    currentEpochSeconds: Long,
    lastShownEpochSeconds: Long,
    isRetryPending: Boolean,
): Boolean {
    val reachesTriggerCount = postCloseCount >= INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT
    val matchesTriggerInterval = postCloseCount % INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT == 0
    val isTriggeredByCount = reachesTriggerCount && matchesTriggerInterval
    val isTriggered = isTriggeredByCount || isRetryPending

    val elapsedSeconds = currentEpochSeconds - lastShownEpochSeconds
    val satisfiesCooldown = elapsedSeconds >= INTERSTITIAL_AD_COOLDOWN_SECONDS

    return isTriggered && satisfiesCooldown
}

/** インタースティシャル広告を表示する投稿詳細クローズ回数。 */
internal const val INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT = 2

/** インタースティシャル広告表示後に再表示を抑制する秒数。 */
internal const val INTERSTITIAL_AD_COOLDOWN_SECONDS = 180L
