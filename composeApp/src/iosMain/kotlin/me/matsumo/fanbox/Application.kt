package me.matsumo.fanbox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import moe.tlaster.precompose.PreComposeApp
import org.koin.compose.KoinContext

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun MainViewController(
    topSafeArea: Float,
    bottomSafeArea: Float
) = ComposeUIViewController {
    initKoin()
    initNapier()
    initCoil()

    PreComposeApp {
        KoinContext {
            PixiViewApp(
                modifier = Modifier.fillMaxSize(),
                windowSize = calculateWindowSizeClass().widthSizeClass,
            )
        }
    }
}
