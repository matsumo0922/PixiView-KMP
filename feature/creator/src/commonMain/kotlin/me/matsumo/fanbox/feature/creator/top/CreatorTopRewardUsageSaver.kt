package me.matsumo.fanbox.feature.creator.top

import androidx.compose.runtime.saveable.Saver
import me.matsumo.fanbox.core.model.RewardUsage

/** Creator Top の未処理リワード用途を Activity 再生成後に復元する Saver。 */
internal val CreatorTopRewardUsageSaver = Saver<RewardUsage?, String>(
    save = { usage -> saveCreatorTopRewardUsage(usage) },
    restore = { name -> restoreCreatorTopRewardUsage(name) },
)

internal fun saveCreatorTopRewardUsage(usage: RewardUsage?): String? {
    return usage?.name
}

internal fun restoreCreatorTopRewardUsage(name: String): RewardUsage? {
    return RewardUsage.entries.firstOrNull { it.name == name }
}
