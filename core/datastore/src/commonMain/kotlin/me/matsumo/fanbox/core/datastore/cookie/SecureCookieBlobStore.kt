package me.matsumo.fanbox.core.datastore.cookie

import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * [SecureCookiePayload] を 1 つの値として読み書きする保存先。
 *
 * 実体は端末の secure store（Android は Keystore で保護した鍵、iOS は Keychain）である。
 * テストから差し替えられるように interface で切っている。
 */
internal interface SecureCookieBlobStore {
    /** 現在の payload を流し、以降の変更を流す。購読開始時に必ず 1 度は値を流す。 */
    val payloads: Flow<SecureCookiePayload>

    /**
     * 現在の payload を返す。
     *
     * まだ何も書かれていない場合と、書かれているが復号できない場合の両方で
     * [SecureCookiePayload] の初期値を返す。両者は [hasStoredPayload] で区別する。
     */
    suspend fun load(): SecureCookiePayload

    /**
     * payload を書き、保存が完了するまで待つ。
     *
     * 正常に戻った場合、値は保存先へ commit されている。失敗した場合は例外を投げる。
     */
    suspend fun save(payload: SecureCookiePayload)

    /** payload を削除し、削除が完了するまで待つ。 */
    suspend fun clear()

    /**
     * 保存先に payload が存在するかを返す。復号できるかどうかは問わない。
     *
     * [load] が初期値を返したとき、これが true なら「保存されているが読めない」、
     * false なら「まだ何も保存されていない」と分かる。
     */
    fun hasStoredPayload(): Boolean
}

/**
 * KSafe を保存先とする [SecureCookieBlobStore]。
 *
 * すべての書き込みに suspend API を使う。`putDirect` 系は書き込みをキューへ積むだけで
 * 完了を待たないため、保存されないままプロセスが終わると資格情報が失われる。
 */
internal class KSafeCookieBlobStore(
    private val kSafe: KSafe,
) : SecureCookieBlobStore {

    /**
     * KSafe の Flow は、一時的な復号失敗が起きた回の emission を落とす。そのままでは
     * 購読開始直後に端末がロックされている場合などに 1 度も値が流れず、
     * `FanboxCookieStorage` の「購読者は現在値を少なくとも 1 度受け取る」契約を満たせない。
     *
     * [onStart] で現在値を補い、読めない場合は初期値を流す。
     */
    override val payloads: Flow<SecureCookiePayload> =
        kSafe.getFlow(KEY_COOKIE_PAYLOAD, SecureCookiePayload())
            .onStart { emit(runCatching { load() }.getOrElse { SecureCookiePayload() }) }
            .distinctUntilChanged()

    override suspend fun load(): SecureCookiePayload {
        return kSafe.get(KEY_COOKIE_PAYLOAD, SecureCookiePayload())
    }

    override suspend fun save(payload: SecureCookiePayload) {
        kSafe.put(KEY_COOKIE_PAYLOAD, payload)
    }

    override suspend fun clear() {
        kSafe.delete(KEY_COOKIE_PAYLOAD)
    }

    override fun hasStoredPayload(): Boolean {
        return kSafe.getKeyInfo(KEY_COOKIE_PAYLOAD) != null
    }

    private companion object {
        const val KEY_COOKIE_PAYLOAD = "fanbox_cookie_payload"
    }
}

/** [SecureCookieBlobStore] の payload から Cookie レコードだけを取り出す。 */
internal fun Flow<SecureCookiePayload>.records(): Flow<List<SecureCookieRecord>> =
    map { it.records }.distinctUntilChanged()
