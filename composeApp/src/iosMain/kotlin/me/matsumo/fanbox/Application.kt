package me.matsumo.fanbox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.toImmutableMap
import moe.tlaster.precompose.PreComposeApp
import org.koin.compose.KoinContext
import platform.UIKit.UIViewController

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Suppress("FunctionNaming", "UnusedParameter")
fun MainViewController(
    topSafeArea: Float,
    bottomSafeArea: Float,
    iosUis: Map<String, () -> UIViewController?>,
) = ComposeUIViewController {
    KoinContext {
        Napier.d { "MainViewController: ${iosUis.size}" }

        PreComposeApp {
            PixiViewApp(
                modifier = Modifier.fillMaxSize(),
                windowSize = calculateWindowSizeClass().widthSizeClass,
                nativeViews = iosUis.toImmutableMap(),
            )
        }
    }
}
