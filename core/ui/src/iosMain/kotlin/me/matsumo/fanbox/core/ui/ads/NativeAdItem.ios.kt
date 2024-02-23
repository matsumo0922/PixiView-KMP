package me.matsumo.fanbox.core.ui.ads

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitViewController
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import me.matsumo.fanbox.core.ui.view.LocalNativeViewsProvider
import me.matsumo.fanbox.core.ui.view.NativeViews

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeAdView(
    modifier: Modifier,
) {
    val nativeView = LocalNativeViewsProvider.current.provide(NativeViews.Key.NativeAdView)

    UIKitViewController(
        modifier = modifier.size(128.dp, 128.dp),
        factory = nativeView
    )
}
