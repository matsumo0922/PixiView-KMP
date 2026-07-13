package me.matsumo.fanbox.core.ui.ads

import androidx.compose.runtime.Immutable

/** リワード広告の表示完了結果。 */
@Immutable
data class RewardAdShowResult(
    val requestId: Long,
    val isRewardEarned: Boolean,
)
