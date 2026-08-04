@file:Suppress("MatchingDeclarationName", "Filename")

package me.matsumo.fanbox.core.datastore.cookie

import eu.anifantakis.lib.ksafe.KSafe
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import me.matsumo.fankt.fanbox.persistence.room.createRoomFanboxCookieStorage
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask

/**
 * iOS 向けの Cookie 保存先を作る。
 *
 * 資格情報は Keychain へ保存し、既存の `fankt.db` からの移行を挟む。
 * 返した実体はアプリの生存期間中 1 つだけ使い、閉じないこと。
 */
internal fun createCookieStorage(
    ioDispatcher: CoroutineDispatcher,
): MigratingFanboxCookieStorage {
    excludeLegacyDatabaseFromBackup()

    return createMigratingCookieStorage(
        kSafe = KSafe(),
        legacyStorageFactory = {
            RoomLegacyCookieStorage(
                createRoomFanboxCookieStorage(ioDispatcher = ioDispatcher),
            )
        },
    )
}

/**
 * 旧 Room の DB を iCloud バックアップの対象から外す。
 *
 * `fankt.db` は `NSDocumentDirectory` に置かれ、iOS はこのディレクトリを既定でバックアップに含める。
 * この DB は Cookie を平文で保持するため、除外しないと移行前の資格情報が iCloud に入る。
 *
 * Keychain の項目は端末外に出ない設定のため、除外の指定は要らない。
 */
@OptIn(ExperimentalForeignApi::class)
private fun excludeLegacyDatabaseFromBackup() {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    ) ?: return

    for (fileName in LEGACY_DATABASE_FILE_NAMES) {
        val fileUrl = documentDirectory.URLByAppendingPathComponent(fileName) ?: continue

        fileUrl.excludeFromBackup()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSURL.excludeFromBackup() {
    val path = path ?: return

    if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return

    val isExcluded = setResourceValue(
        value = true,
        forKey = NSURLIsExcludedFromBackupKey,
        error = null,
    )

    if (!isExcluded) {
        Napier.w { "Failed to exclude the legacy Cookie database from backup." }
    }
}

/** 旧 Room の DB と、SQLite が併せて作るファイル。 */
private val LEGACY_DATABASE_FILE_NAMES = listOf("fankt.db", "fankt.db-wal", "fankt.db-shm")
