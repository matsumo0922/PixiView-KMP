package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.runtime.Composable

@Composable
actual fun CreatorTopRewardAdDialog(
    title: String,
    message: String,
    isAbleToReward: Boolean,
    onRewarded: () -> Unit,
    onClickShowPlus: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    onClickShowPlus.invoke()
}
