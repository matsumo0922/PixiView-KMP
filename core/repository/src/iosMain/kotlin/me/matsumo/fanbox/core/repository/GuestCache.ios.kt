package me.matsumo.fanbox.core.repository

import kotlinx.cinterop.ExperimentalForeignApi
import me.matsumo.fankt.fanbox.FanboxGuestCache
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * guest bundle のキャッシュを Caches ディレクトリ配下の [directoryName] に置く。
 *
 * bundle は配信先から取得し直せるため、iCloud バックアップの対象外で、容量が足りないときに OS が消せる場所に置く。
 */
@OptIn(ExperimentalForeignApi::class)
internal fun createGuestCache(directoryName: String): FanboxGuestCache {
    val cachesDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = requireNotNull(cachesDirectory?.path) { "The caches directory is unavailable" }

    return FanboxGuestCache("$path/fanbox-guest/$directoryName")
}
