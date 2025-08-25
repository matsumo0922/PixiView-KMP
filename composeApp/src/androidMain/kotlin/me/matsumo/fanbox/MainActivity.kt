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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.google.ads.mediation.inmobi.InMobiConsent
import com.google.android.gms.ads.MobileAds
import com.inmobi.sdk.InMobiSdk
import com.unity3d.ads.metadata.MetaData
import com.vungle.ads.VunglePrivacySettings
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.matsumo.fanbox.core.logs.category.ApplicationLog
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.ui.theme.shouldUseDarkTheme
import me.matsumo.fanbox.feature.service.DownloadPostService
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class MainActivity : FragmentActivity(), KoinComponent {

    private val viewModel by viewModel<MainViewModel>()
    private var stayTime = 0L

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.setting.collectAsStateWithLifecycle(null)
            val isSystemInDarkTheme = shouldUseDarkTheme(settings?.themeConfig ?: ThemeConfig.System)
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

            splashScreen.setKeepOnScreenCondition { settings == null }
        }

        initAdsSdk()
        startService(Intent(this, DownloadPostService::class.java))
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

    private fun initAdsSdk() {
        if (viewModel.isAdsSdkInitialized.value) {
            return
        }

        // AppLovin
        AppLovinSdk.getInstance(this).initialize(
            AppLovinSdkInitializationConfiguration.builder(BuildKonfig.APPLOVIN_SDK_KEY)
                .setMediationProvider(AppLovinMediationProvider.ADMOB)
                .build(),
            null,
        )
        AppLovinPrivacySettings.setHasUserConsent(true)

        // InMobi
        InMobiConsent.updateGDPRConsent(
            buildJsonObject {
                put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true)
                put("gdpr", "1")
            }.let {
                JSONObject(it)
            },
        )

        // Liftoff
        VunglePrivacySettings.setGDPRStatus(true, "v1.0.0")
        VunglePrivacySettings.setCCPAStatus(true)

        // Unity Ads
        val gdprMetaData = MetaData(this)
        gdprMetaData["gdpr.consent"] = true
        gdprMetaData.commit()

        val ccpaMetaData = MetaData(this)
        ccpaMetaData["privacy.consent"] = true
        ccpaMetaData.commit()

        MobileAds.initialize(this)
        viewModel.setAdsSdkInitialized()
    }
}
