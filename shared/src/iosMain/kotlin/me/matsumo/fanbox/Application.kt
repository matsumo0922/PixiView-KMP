package me.matsumo.fanbox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.toImmutableMap
import org.koin.compose.KoinContext
import platform.UIKit.UIViewController

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
// Swift から呼ぶ entry point と引数の互換性を維持する。移行: https://github.com/matsumo0922/PixiView-KMP/issues/164
@Suppress("FunctionNaming", "UnusedParameter")
fun MainViewController(
    topSafeArea: Float,
    bottomSafeArea: Float,
    iosUis: Map<String, () -> UIViewController?>,
) = ComposeUIViewController {
    KoinContext {
        Napier.d { "MainViewController: ${iosUis.size}" }

        PixiViewApp(
            modifier = Modifier.fillMaxSize(),
            windowSize = calculateWindowSizeClass().widthSizeClass,
            nativeViews = iosUis.toImmutableMap(),
        )
    }
}
