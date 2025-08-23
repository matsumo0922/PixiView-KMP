package me.matsumo.fanbox.core.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class InterstitialAdStateImpl : InterstitialAdState {
    override fun load() {

    }

    override suspend fun show(): Boolean {
        return true
    }
}

@Composable
actual fun rememberInterstitialAdState(adUnitId: String, enable: Boolean): InterstitialAdState {
    return remember { InterstitialAdStateImpl() }
}
