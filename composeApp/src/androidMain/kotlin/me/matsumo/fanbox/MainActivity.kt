package me.matsumo.fanbox

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.theme.shouldUseDarkTheme
import org.koin.compose.KoinContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : FragmentActivity(), KoinComponent {

    private val userDataRepository: UserDataRepository by inject()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            KoinContext {
                val userData by userDataRepository.userData.collectAsStateWithLifecycle(null)
                val isSystemInDarkTheme = shouldUseDarkTheme(userData?.themeConfig ?: ThemeConfig.System)
                val windowSize = calculateWindowSizeClass()

                val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
                val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

                DisposableEffect(isSystemInDarkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isSystemInDarkTheme },
                        navigationBarStyle = SystemBarStyle.auto(lightScrim, darkScrim) { isSystemInDarkTheme },
                    )
                    onDispose {}
                }

                PixiViewApp(
                    modifier = Modifier.fillMaxSize(),
                    windowSize = windowSize.widthSizeClass,
                    nativeViews = emptyMap(),
                )

                splashScreen.setKeepOnScreenCondition { userData == null }
            }
        }
    }
}
