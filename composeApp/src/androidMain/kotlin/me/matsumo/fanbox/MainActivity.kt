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
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.google.ads.mediation.inmobi.InMobiConsent
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.initialization.InitializationStatus
import com.inmobi.sdk.InMobiSdk
import com.unity3d.ads.metadata.MetaData
import com.vungle.ads.VunglePrivacySettings
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.matsumo.fanbox.core.logs.category.ApplicationLog
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.ui.ads.NativeAdsPreLoader
import me.matsumo.fanbox.core.ui.theme.shouldUseDarkTheme
import me.matsumo.fanbox.feature.service.DownloadPostService
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : FragmentActivity(), KoinComponent {

    private val viewModel by viewModel<MainViewModel>()
    private val nativeAdsPreLoader by inject<NativeAdsPreLoader>()
    private val isMobileAdsSdkInitializationStarted = AtomicBoolean(false)
    private var stayTime = 0L

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.setting.collectAsStateWithLifecycle(null)
            val isAdsSdkInitialized by viewModel.isAdsSdkInitialized.collectAsStateWithLifecycle()
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
                isAdsSdkInitialized = isAdsSdkInitialized,
            )

            splashScreen.setKeepOnScreenCondition { settings == null }
        }

        initAdsSdk()
        startService(Intent(this, DownloadPostService::class.java))
    }

    override fun onResume() {
        super.onResume()

        refreshNativeAdInventory()

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

    private fun refreshNativeAdInventory() {
        if (!viewModel.isAdsSdkInitialized.value) {
            return
        }

        nativeAdsPreLoader.refreshInventory()
    }

    private fun initAdsSdk() {
        if (viewModel.isAdsSdkInitialized.value) {
            return
        }

        configureMediationPrivacySettings()
        initializeAppLovinSdk()
    }

    private fun configureMediationPrivacySettings() {
        // AppLovin
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
    }

    private fun initializeAppLovinSdk() {
        val appLovinSdkKey = BuildKonfig.APPLOVIN_SDK_KEY

        if (appLovinSdkKey.isBlank()) {
            Napier.w { "AppLovin SDK key is blank. Skip AppLovin SDK initialization." }
            tryInitializeMobileAdsSdk()
            return
        }

        val initializationConfiguration = AppLovinSdkInitializationConfiguration.builder(appLovinSdkKey)
            .setMediationProvider(AppLovinMediationProvider.ADMOB)
            .build()

        AppLovinSdk.getInstance(this).initialize(
            initializationConfiguration,
            ::onAppLovinSdkInitialized,
        )
        startAppLovinInitializationTimeout()
    }

    private fun onAppLovinSdkInitialized(sdkConfiguration: AppLovinSdkConfiguration) {
        Napier.d { "AppLovin SDK initialized: $sdkConfiguration" }
        tryInitializeMobileAdsSdk()
    }

    private fun startAppLovinInitializationTimeout() {
        lifecycleScope.launch {
            waitForAppLovinInitializationTimeout()
        }
    }

    private suspend fun waitForAppLovinInitializationTimeout() {
        delay(APPLOVIN_INITIALIZATION_TIMEOUT_MILLIS)

        val isFallbackInitializationStarted = tryInitializeMobileAdsSdk()
        if (isFallbackInitializationStarted) {
            Napier.w { "AppLovin SDK initialization timed out. Start MobileAds initialization." }
        }
    }

    private fun tryInitializeMobileAdsSdk(): Boolean {
        if (!isMobileAdsSdkInitializationStarted.compareAndSet(false, true)) {
            return false
        }

        MobileAds.initialize(
            this,
            ::onMobileAdsInitialized,
        )
        return true
    }

    private fun onMobileAdsInitialized(initializationStatus: InitializationStatus) {
        logAdapterInitializationStatus(initializationStatus = initializationStatus)
        viewModel.setAdsSdkInitialized()
    }

    private fun logAdapterInitializationStatus(initializationStatus: InitializationStatus) {
        for ((adapterClassName, adapterStatus) in initializationStatus.adapterStatusMap) {
            logAdapterInitializationStatus(
                adapterClassName = adapterClassName,
                adapterStatus = adapterStatus,
            )
        }
    }

    private fun logAdapterInitializationStatus(
        adapterClassName: String,
        adapterStatus: AdapterStatus,
    ) {
        val initializationState = adapterStatus.initializationState
        val logMessage = "MobileAds adapter initialized: $adapterClassName, " +
            "state=$initializationState, " +
            "latency=${adapterStatus.latency}, " +
            "description=${adapterStatus.description}"

        if (initializationState == AdapterStatus.State.READY) {
            Napier.d { logMessage }
        } else {
            Napier.w { logMessage }
        }
    }
}

/** AppLovin SDK 初期化 callback を待つ最大時間。 */
private const val APPLOVIN_INITIALIZATION_TIMEOUT_MILLIS = 5_000L
