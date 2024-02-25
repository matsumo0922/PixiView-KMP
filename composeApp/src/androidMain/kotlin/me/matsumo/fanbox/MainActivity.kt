package me.matsumo.fanbox

import android.os.Bundle
import androidx.activity.compose.setContent
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.compose.KoinContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : FragmentActivity(), KoinComponent {

    private val userDataRepository: UserDataRepository by inject()

    private val fanboxRepository: FanboxRepository by inject()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PreComposeApp {
                KoinContext {
                    val windowSize = calculateWindowSizeClass()
                    val systemUiController = rememberSystemUiController()

                    val userData by userDataRepository.userData.collectAsStateWithLifecycle(initial = null)
                    val isSystemInDarkTheme = isSystemInDarkTheme()

                    splashScreen.setKeepOnScreenCondition { userData == null }

                    if (userData != null) {
                        DisposableEffect(systemUiController, userData!!.themeConfig, isSystemInDarkTheme) {
                            systemUiController.systemBarsDarkContentEnabled = (userData!!.themeConfig == ThemeConfig.Light || !isSystemInDarkTheme)
                            onDispose {}
                        }
                    }

                    PixiViewApp(
                        modifier = Modifier.fillMaxSize(),
                        windowSize = windowSize.widthSizeClass,
                        nativeViews = emptyMap(),
                    )
                }
            }
        }
    }
}
