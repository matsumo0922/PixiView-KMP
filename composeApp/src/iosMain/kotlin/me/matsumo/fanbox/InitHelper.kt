package me.matsumo.fanbox

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.fetch.NetworkFetcher
import coil3.memory.MemoryCache
import coil3.request.crossfade
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import me.matsumo.fanbox.di.applyModules
import okio.Path.Companion.toPath
import org.koin.core.context.startKoin
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

fun initKoin() {
    startKoin {
        applyModules()
    }
}

fun initNapier() {
    Napier.base(DebugAntilog())
}

@OptIn(ExperimentalForeignApi::class, ExperimentalCoilApi::class)
fun initCoil() {
    val fileManager = NSFileManager.defaultManager

    val cacheDir = NSSearchPathForDirectoriesInDomains(
        directory = NSCachesDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    )

    val imageCacheDir = cacheDir.first().toString() + "/image_cache"

    if (!fileManager.fileExistsAtPath(imageCacheDir)) {
        fileManager.createDirectoryAtPath(imageCacheDir, withIntermediateDirectories = true, attributes = null, error = null)
    }

    SingletonImageLoader.setSafe {
        ImageLoader.Builder(it)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(it, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(imageCacheDir.toPath())
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
