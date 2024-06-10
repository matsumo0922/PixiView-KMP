package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.runtime.Composable

@Composable
expect fun CreatorTopRewardAdDialog(
    onRewarded: () -> Unit,
    onClickShowPlus: () -> Unit,
    onDismissRequest: () -> Unit,
)
