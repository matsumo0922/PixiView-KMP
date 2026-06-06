package me.matsumo.fanbox.core.ui.ads

import androidx.compose.runtime.Composable

interface InterstitialAdState {
    fun load()
    suspend fun show(): Boolean
}

@Composable
expect fun rememberInterstitialAdState(
    adUnitId: String,
    enable: Boolean,
): InterstitialAdState
