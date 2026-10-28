package me.matsumo.fanbox.core.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class InterstitialAdStateImpl : InterstitialAdState {
    override fun load() {
        // Implement iOS-specific ad loading logic
    }

    override suspend fun show(): Boolean = true
}

@Composable
actual fun rememberInterstitialAdState(adUnitId: String, enable: Boolean): InterstitialAdState = remember {
    InterstitialAdStateImpl()
}
