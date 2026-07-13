package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.matsumo.fanbox.core.ui.ads.RewardAdLoader
import me.matsumo.fanbox.core.ui.theme.LocalAdsSdkInitialized
import org.koin.compose.koinInject

@Composable
actual fun CreatorTopRewardAdPreloader(shouldPreload: Boolean) {
    val isAdsSdkInitialized = LocalAdsSdkInitialized.current
    val rewardAdLoader = koinInject<RewardAdLoader>()

    LaunchedEffect(
        key1 = shouldPreload,
        key2 = isAdsSdkInitialized,
    ) {
        if (shouldPreload) {
            rewardAdLoader.loadRewardAdIfNeeded(isAdsSdkInitialized)
        }
    }
}
