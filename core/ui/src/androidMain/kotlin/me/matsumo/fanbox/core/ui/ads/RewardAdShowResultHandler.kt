package me.matsumo.fanbox.core.ui.ads

/** リワード広告の表示完了結果を表示要求 ID に基づいて消費する。 */
fun handleRewardAdShowResult(
    showResult: RewardAdShowResult?,
    activeRequestId: Long?,
    consumeShowResult: (RewardAdShowResult) -> Unit,
    onActiveResultConsumed: (Boolean) -> Unit,
) {
    if (showResult == null) return

    val isActiveResult = showResult.requestId == activeRequestId

    consumeShowResult(showResult)

    if (isActiveResult) {
        onActiveResultConsumed(showResult.isRewardEarned)
    }
}
