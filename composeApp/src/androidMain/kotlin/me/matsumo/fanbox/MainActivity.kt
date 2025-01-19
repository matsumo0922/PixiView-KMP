package me.matsumo.fanbox

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.datastore.LaunchLogDataStore
import me.matsumo.fanbox.core.logs.category.ApplicationLog
import me.matsumo.fanbox.core.logs.category.ReviewsLog
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.theme.shouldUseDarkTheme
import me.matsumo.fanbox.feature.service.DownloadPostService
import org.koin.compose.KoinContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : FragmentActivity(), KoinComponent {

    private val userDataRepository: UserDataRepository by inject()

    private val downloadPostsRepository: DownloadPostsRepository by inject()

    private val launchLogDataStore: LaunchLogDataStore by inject()

    private var stayTime = 0L

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
                    nativeViews = persistentMapOf(),
                )

                splashScreen.setKeepOnScreenCondition { userData == null }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                downloadPostsRepository.reservingPosts.collectLatest {
                    if (it.isNotEmpty()) {
                        requestReview()
                    }
                }
            }
        }

        startService(Intent(this, DownloadPostService::class.java))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()

        if (stayTime == 0L) {
            lifecycleScope.launch {
                // LogConfigurator が初期化されるまで待つ
                LogConfigurator.isConfigured.first { it }

                stayTime = System.currentTimeMillis()
                ApplicationLog.open().send()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        if (stayTime != 0L) {
            ApplicationLog.close((System.currentTimeMillis() - stayTime) / 1000).send()
            stayTime = 0L
        }
    }

    private fun requestReview() {
        lifecycleScope.launch {
            if (launchLogDataStore.getLaunchCount() < 3) return@launch

            ReviewsLog.tryRequestReview().send()

            val manager = ReviewManagerFactory.create(this@MainActivity)
            val request = manager.requestReviewFlow()

            request.addOnCompleteListener {
                if (it.isSuccessful) {
                    ReviewsLog.requestReview().send()

                    val reviewInfo = it.result
                    val flow = manager.launchReviewFlow(this@MainActivity, reviewInfo)

                    flow.addOnCompleteListener {
                        ReviewsLog.reviewed().send()
                    }
                }
            }
        }
    }
}
