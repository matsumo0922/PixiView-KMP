package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CreatorTopRewardAdDialog(
    title: String,
    message: String,
    isAbleToReward: Boolean,
    onReward: () -> Unit,
    onClickShowPlus: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    onClickShowPlus.invoke()
}
