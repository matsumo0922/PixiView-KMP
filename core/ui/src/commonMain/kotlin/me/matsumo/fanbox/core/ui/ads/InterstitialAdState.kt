package me.matsumo.fanbox.core.ui.ads

import androidx.compose.runtime.Composable

interface InterstitialAdState {
    fun load()
    suspend fun show(): InterstitialAdShowResult
}

@Composable
expect fun rememberInterstitialAdState(adUnitId: String, enable: Boolean): InterstitialAdState
