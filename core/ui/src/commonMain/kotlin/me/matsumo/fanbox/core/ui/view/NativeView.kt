package me.matsumo.fanbox.core.ui.view

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

expect class NativeView

@Immutable
data class NativeViews(
    private val provider: Map<String, () -> NativeView?> = emptyMap(),
) {
    fun provide(key: Key): () -> NativeView {
        return provider[key.id]!! as () -> NativeView
    }

    enum class Key(val id: String) {
        NativeAdView("NativeAdView"),
        BannerAdView("BannerAdView"),
    }
}

val LocalNativeViewsProvider = staticCompositionLocalOf { NativeViews() }
