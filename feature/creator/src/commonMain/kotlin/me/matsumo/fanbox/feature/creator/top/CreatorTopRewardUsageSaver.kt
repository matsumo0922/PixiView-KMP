package me.matsumo.fanbox.feature.creator.top

import androidx.compose.runtime.saveable.Saver
import me.matsumo.fanbox.core.model.RewardUsage

/** Creator Top の未処理リワード用途を Activity 再生成後に復元する Saver。 */
internal val CreatorTopRewardUsageSaver = Saver<RewardUsage?, String>(
    save = { usage -> saveCreatorTopRewardUsage(usage) },
    restore = { storageKey -> restoreCreatorTopRewardUsage(storageKey) },
)

internal fun saveCreatorTopRewardUsage(usage: RewardUsage?): String? {
    return usage?.storageKey
}

internal fun restoreCreatorTopRewardUsage(storageKey: String): RewardUsage? {
    return RewardUsage.entries.firstOrNull { it.storageKey == storageKey }
}
