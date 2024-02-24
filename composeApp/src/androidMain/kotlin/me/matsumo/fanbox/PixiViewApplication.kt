package me.matsumo.fanbox

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.fetch.NetworkFetcher
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.di.applyModules
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PixiViewApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            androidLogger()
            applyModules()
        }

        if (BuildConfig.DEBUG) {
            // StrictMode.enableDefaults()
            Napier.base(DebugAntilog())
        }

        setupCoil()
    }

    @OptIn(ExperimentalCoilApi::class)
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
                    add(NetworkFetcher.Factory())
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
