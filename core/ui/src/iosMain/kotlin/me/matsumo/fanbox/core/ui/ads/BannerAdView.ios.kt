package me.matsumo.fanbox.core.ui.ads

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitViewController
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import me.matsumo.fanbox.core.ui.view.LocalNativeViewsProvider
import me.matsumo.fanbox.core.ui.view.NativeViews

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BannerAdView(modifier: Modifier) {
    val nativeView = LocalNativeViewsProvider.current.provide(NativeViews.Key.BannerAdView)

    UIKitViewController(
        modifier = modifier.height(60.dp),
        factory = nativeView
    )
}
