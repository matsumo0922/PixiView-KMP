package me.matsumo.fanbox.core.datastore.cookie

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * テスト用の [SecureCookieBlobStore]。
 *
 * 端末の secure store の代わりにメモリ上へ保存する。復号できない状態や、
 * 保存の失敗を再現できるようにしている。
 */
internal class FakeSecureCookieBlobStore(
    initialPayload: SecureCookiePayload? = null,
) : SecureCookieBlobStore {

    private val state = MutableStateFlow(initialPayload ?: SecureCookiePayload())

    override val payloads: Flow<SecureCookiePayload> = state.asStateFlow()

    /** 保存先に値が存在するか。復号できるかどうかとは独立に持つ。 */
    var hasStoredValue: Boolean = initialPayload != null

    /** true の間、[load] は初期値を返す。破損して読めない状態を表す。 */
    var isUnreadable: Boolean = false

    /** null 以外を入れると、[save] がその例外を投げる。 */
    var saveFailure: Throwable? = null

    /** null 以外を入れると、[load] がその例外を投げる。 */
    var loadFailure: Throwable? = null

    /** [save] が呼ばれた回数。 */
    var saveCount: Int = 0
        private set

    /** [clear] が呼ばれた回数。 */
    var clearCount: Int = 0
        private set

    override suspend fun load(): SecureCookiePayload {
        loadFailure?.let { throw it }

        return if (isUnreadable) SecureCookiePayload() else state.value
    }

    override suspend fun save(payload: SecureCookiePayload) {
        saveCount += 1
        saveFailure?.let { throw it }

        state.value = payload
        hasStoredValue = true
    }

    override suspend fun clear() {
        clearCount += 1
        state.value = SecureCookiePayload()
        hasStoredValue = false
        isUnreadable = false
    }

    override fun hasStoredPayload(): Boolean = hasStoredValue

    /** 保存先に実際に入っている値を返す。[isUnreadable] の影響を受けない。 */
    fun storedPayload(): SecureCookiePayload = state.value
}
