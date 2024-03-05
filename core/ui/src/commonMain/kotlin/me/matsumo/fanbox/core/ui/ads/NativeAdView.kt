package me.matsumo.fanbox.core.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NativeAdView(
    key: String,
    modifier: Modifier = Modifier,
)
