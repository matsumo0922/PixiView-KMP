@file:Suppress("MatchingDeclarationName", "Filename")

package me.matsumo.fanbox.core.datastore.cookie

import android.content.Context
import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.CoroutineDispatcher
import me.matsumo.fanbox.core.datastore.OldCookieDataStore
import me.matsumo.fankt.fanbox.persistence.room.createRoomFanboxCookieStorage

/**
 * Android 向けの Cookie 保存先を作る。
 *
 * 資格情報は Keystore の鍵で保護した領域へ保存し、既存の `fankt.db` からの移行を挟む。
 * 返した実体はアプリの生存期間中 1 つだけ使い、閉じないこと。
 */
internal fun createCookieStorage(
    context: Context,
    ioDispatcher: CoroutineDispatcher,
    oldCookieDataStore: OldCookieDataStore,
): MigratingFanboxCookieStorage {
    return createMigratingCookieStorage(
        kSafe = KSafe(context),
        legacyStorageFactory = {
            RoomLegacyCookieStorage(
                createRoomFanboxCookieStorage(
                    context = context,
                    ioDispatcher = ioDispatcher,
                ),
            )
        },
        oldCookieDataStore = oldCookieDataStore,
    )
}
