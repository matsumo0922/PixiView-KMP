package me.matsumo.fanbox.core.datastore.cookie

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matsumo.fankt.fanbox.FanboxCookieRecord
import me.matsumo.fankt.fanbox.FanboxCookieStorage

/**
 * 端末の secure store を保存先とする [FanboxCookieStorage]。
 *
 * fankt の契約に従い、レコードは domain / path / name を正規化した組を同一性として扱う。
 * domain は前後の空白と先頭のドットを取り除いて小文字にし、path は `/` で始まらない場合 `/` とする。
 *
 * すべての変更は [Mutex] で直列化する。完了した変更は、以降のすべての読み出しから見える。
 */
internal class SecureFanboxCookieStorage(
    private val blobStore: SecureCookieBlobStore,
) : FanboxCookieStorage {

    private val mutex = Mutex()

    override val cookies: Flow<List<FanboxCookieRecord>> =
        blobStore.payloads.records().map { records -> records.map(SecureCookieRecord::toFanboxCookieRecord) }

    override suspend fun snapshot(): List<FanboxCookieRecord> {
        return blobStore.load().records.map(SecureCookieRecord::toFanboxCookieRecord)
    }

    override suspend fun upsert(cookie: FanboxCookieRecord) {
        val replacement = cookie.canonicalized()

        mutate { records ->
            records.filterNot { it.identity == replacement.identity } + replacement.toSecureCookieRecord()
        }
    }

    override suspend fun delete(domain: String, path: String, name: String) {
        val identity = CookieIdentity(domain.canonicalDomain(), path.canonicalPath(), name)

        mutate { records -> records.filterNot { it.identity == identity } }
    }

    override suspend fun deleteExpired(nowEpochMilliseconds: Long) {
        mutate { records ->
            records.filterNot { record ->
                val expiry = record.expiresAtEpochMilliseconds

                expiry != null && expiry <= nowEpochMilliseconds
            }
        }
    }

    override suspend fun replaceAll(cookies: List<FanboxCookieRecord>) {
        val replacement = cookies.map { it.canonicalized().toSecureCookieRecord() }.deduplicated()

        mutate { replacement }
    }

    override suspend fun clear() {
        mutate { emptyList() }
    }

    /**
     * payload を読み、[transform] を適用して書き戻す。
     *
     * 移行状態は保ったまま Cookie だけを差し替える。書き込みは commit の完了を待つため、
     * 例外なく戻った時点で保存先へ反映されている。
     */
    private suspend fun mutate(transform: (List<SecureCookieRecord>) -> List<SecureCookieRecord>) {
        mutex.withLock {
            val current = blobStore.load()

            blobStore.save(current.copy(records = transform(current.records).deduplicated()))
        }
    }
}

internal data class CookieIdentity(
    val domain: String,
    val path: String,
    val name: String,
)

internal val SecureCookieRecord.identity: CookieIdentity
    get() = CookieIdentity(domain.canonicalDomain(), path.canonicalPath(), name)

internal val FanboxCookieRecord.identity: CookieIdentity
    get() = CookieIdentity(domain.canonicalDomain(), path.canonicalPath(), name)

internal fun String.canonicalDomain(): String = trim().trimStart('.').lowercase()

internal fun String.canonicalPath(): String = takeIf { startsWith('/') } ?: "/"

internal fun FanboxCookieRecord.canonicalized(): FanboxCookieRecord = copy(
    domain = domain.canonicalDomain(),
    path = path.canonicalPath(),
)

/** 同一性が重なるレコードを後勝ちで 1 件に畳む。 */
internal fun List<SecureCookieRecord>.deduplicated(): List<SecureCookieRecord> =
    associateBy { it.identity }.values.toList()
