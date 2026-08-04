package me.matsumo.fanbox.core.datastore.cookie

import kotlinx.coroutines.flow.Flow
import me.matsumo.fankt.fanbox.FanboxCookieRecord
import me.matsumo.fankt.fanbox.persistence.room.RoomFanboxCookieStorage

/**
 * fankt の Room 保存先を、移行元として扱えるようにする。
 *
 * 移行が終わったら閉じる。閉じた後の操作は失敗する。
 */
internal class RoomLegacyCookieStorage(
    private val delegate: RoomFanboxCookieStorage,
) : LegacyCookieStorage {

    override val cookies: Flow<List<FanboxCookieRecord>> = delegate.cookies

    override suspend fun snapshot(): List<FanboxCookieRecord> = delegate.snapshot()

    override suspend fun upsert(cookie: FanboxCookieRecord) = delegate.upsert(cookie)

    override suspend fun delete(domain: String, path: String, name: String) =
        delegate.delete(domain, path, name)

    override suspend fun deleteExpired(nowEpochMilliseconds: Long) =
        delegate.deleteExpired(nowEpochMilliseconds)

    override suspend fun replaceAll(cookies: List<FanboxCookieRecord>) = delegate.replaceAll(cookies)

    override suspend fun clear() = delegate.clear()

    override fun close() = delegate.close()
}
