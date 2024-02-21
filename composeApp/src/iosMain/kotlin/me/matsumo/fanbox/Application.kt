package me.matsumo.fanbox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.Napier
import moe.tlaster.precompose.PreComposeApp
import org.koin.compose.KoinContext
import platform.UIKit.UIViewController

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun MainViewController(
    topSafeArea: Float,
    bottomSafeArea: Float,
    iosUis: Map<String, () -> UIViewController?>
) = ComposeUIViewController {
    PreComposeApp {
        KoinContext {
            Napier.d { "MainViewController: ${iosUis.size}" }

            PixiViewApp(
                modifier = Modifier.fillMaxSize(),
                windowSize = calculateWindowSizeClass().widthSizeClass,
            )
        }
    }
}
