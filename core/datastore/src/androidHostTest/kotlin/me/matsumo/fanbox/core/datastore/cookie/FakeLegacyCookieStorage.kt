package me.matsumo.fanbox.core.datastore.cookie

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.matsumo.fankt.fanbox.FanboxCookieRecord

/**
 * テスト用の [LegacyCookieStorage]。
 *
 * 旧 Room の代わりにメモリ上へ保存する。読み出しや削除の失敗を再現できるようにしている。
 */
internal class FakeLegacyCookieStorage(
    initialRecords: List<FanboxCookieRecord> = emptyList(),
) : LegacyCookieStorage {

    private val state = MutableStateFlow(initialRecords)

    /** null 以外を入れると、[snapshot] がその例外を投げる。 */
    var snapshotFailure: Throwable? = null

    /** null 以外を入れると、[clear] がその例外を投げる。 */
    var clearFailure: Throwable? = null

    /** [close] が呼ばれたか。 */
    var isClosed: Boolean = false
        private set

    /** [clear] が呼ばれた回数。 */
    var clearCount: Int = 0
        private set

    override val cookies: Flow<List<FanboxCookieRecord>> = state.asStateFlow()

    override suspend fun snapshot(): List<FanboxCookieRecord> {
        snapshotFailure?.let { throw it }

        return state.value
    }

    override suspend fun upsert(cookie: FanboxCookieRecord) {
        state.value = state.value.filterNot { it.name == cookie.name } + cookie
    }

    override suspend fun delete(domain: String, path: String, name: String) {
        state.value = state.value.filterNot { it.domain == domain && it.path == path && it.name == name }
    }

    override suspend fun deleteExpired(nowEpochMilliseconds: Long) {
        state.value = state.value.filterNot { record ->
            val expiry = record.expiresAtEpochMilliseconds

            expiry != null && expiry <= nowEpochMilliseconds
        }
    }

    override suspend fun replaceAll(cookies: List<FanboxCookieRecord>) {
        state.value = cookies
    }

    override suspend fun clear() {
        clearCount += 1
        clearFailure?.let { throw it }

        state.value = emptyList()
    }

    override fun close() {
        isClosed = true
    }

    /** 保存されているレコードを返す。 */
    fun storedRecords(): List<FanboxCookieRecord> = state.value
}
