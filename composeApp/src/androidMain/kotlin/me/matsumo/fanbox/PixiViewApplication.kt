package me.matsumo.fanbox

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.unity3d.ads.metadata.MetaData
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.di.applyModules
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@OptIn(KoinExperimentalAPI::class)
class PixiViewApplication : Application(), KoinStartup {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // StrictMode.enableDefaults()
            Napier.base(DebugAntilog())
        }

        setupFirebase()
        setupAdMob()
        setupCoil()
    }

    override fun onKoinStartup() = koinConfiguration {
        androidContext(this@PixiViewApplication)
        androidLogger()
        applyModules()
    }

    private fun setupAdMob() {
        // AppLovin
        AppLovinSdk.getInstance(this).initialize(
            AppLovinSdkInitializationConfiguration.builder(BuildKonfig.APPLOVIN_SDK_KEY)
                .setMediationProvider(AppLovinMediationProvider.ADMOB)
                .build(),
            null,
        )
        AppLovinPrivacySettings.setHasUserConsent(true)

        // Unity Ads
        val gdprMetaData = MetaData(this)
        gdprMetaData["gdpr.consent"] = true
        gdprMetaData.commit()

        val ccpaMetaData = MetaData(this)
        ccpaMetaData["privacy.consent"] = true
        ccpaMetaData.commit()

        MobileAds.initialize(this)
    }

    private fun setupCoil() {
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(it)
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(it, 0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(this.cacheDir.resolve("image_cache").toOkioPath())
                        .maxSizePercent(0.02)
                        .build()
                }
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(AnimatedImageDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .crossfade(true)
                .build()
        }
    }

    private fun setupFirebase() {
        val builder = FirebaseOptions.Builder()
            .setProjectId("pixiview-b5dc7")
            .setApplicationId(if (!BuildConfig.DEBUG) "1:561281916572:android:925776ddd5d210faab1759" else "1:561281916572:android:85a0c0e2e341d0fdab1759")
            .setApiKey("AIzaSyC6SxUp9ipDpVjQMSHmcgy7SZMnPUwqAmA")
            .setDatabaseUrl("https://pixiview-b5dc7.firebaseio.com")
            .setStorageBucket("pixiview-b5dc7.appspot.com")

        FirebaseApp.initializeApp(this, builder.build())
    }
}
