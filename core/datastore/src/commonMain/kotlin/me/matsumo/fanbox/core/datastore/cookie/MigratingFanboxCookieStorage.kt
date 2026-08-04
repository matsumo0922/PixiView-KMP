package me.matsumo.fanbox.core.datastore.cookie

import io.github.aakira.napier.Napier
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.matsumo.fankt.fanbox.FanboxCookieRecord
import me.matsumo.fankt.fanbox.FanboxCookieStorage

/**
 * 旧 Room から secure store への移行を挟む [FanboxCookieStorage]。
 *
 * すべての操作は、最初の 1 回だけ行う保存先の決定を待つ。決定を待つ間に届いた読み書きは
 * 遅れるだけで失われず、決定した保存先へ適用される。
 *
 * 移行は次の順で行い、secure への保存が完了してから初めて Room を空にする。
 * 保存の完了前に失敗した場合は Room を使い続け、次回起動で再試行する。
 *
 * 1. Room のレコードを読む
 * 2. secure へ書き、保存の完了を待つ
 * 3. 保存先を secure に決める
 * 4. Room を空にして閉じる
 */
class MigratingFanboxCookieStorage internal constructor(
    private val secureStorage: SecureFanboxCookieStorage,
    private val blobStore: SecureCookieBlobStore,
    private val legacyStorageFactory: () -> LegacyCookieStorage?,
    private val onMigrationEvent: (CookieMigrationEvent) -> Unit = {},
) : FanboxCookieStorage {

    private val routingMutex = Mutex()
    private var routing: CookieRouting? = null

    /**
     * 現在の Cookie を流す。
     *
     * 保存先が決まるまで待ってから、決まった保存先の Flow をそのまま流す。決まる前に
     * 空リストを流さないこと。購読者は最初に受け取った値を現在の状態として扱うため、
     * 保存済みのセッションがある端末で一度未ログインとして観測されてしまう。
     */
    override val cookies: Flow<List<FanboxCookieRecord>> = flow {
        emitAll(readStorage().cookies)
    }

    override suspend fun snapshot(): List<FanboxCookieRecord> = readStorage().snapshot()

    override suspend fun upsert(cookie: FanboxCookieRecord) = mutate { it.upsert(cookie) }

    override suspend fun delete(domain: String, path: String, name: String) =
        mutate { it.delete(domain, path, name) }

    override suspend fun deleteExpired(nowEpochMilliseconds: Long) =
        mutate { it.deleteExpired(nowEpochMilliseconds) }

    override suspend fun replaceAll(cookies: List<FanboxCookieRecord>) = mutate { it.replaceAll(cookies) }

    override suspend fun clear() = mutate { it.clear() }

    /**
     * ログアウトする。
     *
     * secure と Room は別の保存先なので、両方の削除を 1 つの不可分な操作にはできない。
     * 片方だけ消えた状態でプロセスが終わると、残った方から次回起動時にセッションが戻る。
     * これを防ぐため、削除の前にログアウト中の印を secure へ書く。印は両方の削除が
     * 終わってから消すため、途中で終わっても次回起動時にログアウト中だったと分かる。
     */
    suspend fun logout() {
        // 印を書いた後で呼び出し元が取り消されると、印が立ったまま削除が終わらない。
        // その状態で新しいセッションを保存すると、次回起動時に消される。取り消しを受けない。
        withContext(NonCancellable) {
            routingMutex.withLock {
                blobStore.save(SecureCookiePayload(isLogoutInProgress = true))

                if (clearBothStores()) blobStore.clear()

                routing = CookieRouting.Secure
            }
        }
    }

    /** 読み出しに使う保存先を返す。移行に失敗した場合は Room を読み続ける。 */
    private suspend fun readStorage(): FanboxCookieStorage = resolveRouting().storage()

    /**
     * 書き込みを直列化して適用する。
     *
     * 書き込みは必ず secure へ行う。移行に失敗して Room を読んでいる間も、新しい資格情報を
     * 平文の Room へ書かない。書き込みの直前に移行をやり直し、それも失敗した場合は例外を投げる。
     *
     * ログアウトと同じ [routingMutex] を取るため、ログアウトを跨いだ書き込みが
     * 消したはずのセッションを書き戻したり、ログアウト後の新しいセッションを消したりしない。
     */
    private suspend fun mutate(block: suspend (FanboxCookieStorage) -> Unit) {
        routingMutex.withLock {
            val routing = routing ?: decideRouting().also { this.routing = it }

            if (routing is CookieRouting.Legacy) retryMigration(routing.legacyStorage)

            block(secureStorage)
        }
    }

    /**
     * 移行をやり直す。成功したら保存先を secure へ切り替える。
     *
     * 失敗した場合は、呼び出し元の書き込みを Room へ落とさないため例外を投げる。
     */
    private suspend fun retryMigration(legacyStorage: LegacyCookieStorage) {
        val legacyRecords = legacyStorage.snapshot()
        val migrated = migrateRecords(legacyRecords, legacyStorage)

        if (migrated !is CookieRouting.Secure) {
            error("Failed to migrate the Cookies into the secure storage; refusing to write them in plain text.")
        }

        routing = migrated
    }

    private suspend fun resolveRouting(): CookieRouting {
        routing?.let { return it }

        return routingMutex.withLock {
            routing ?: decideRouting().also { routing = it }
        }
    }

    /**
     * 保存先を決める。この関数は [routingMutex] を保持した状態で 1 度だけ呼ばれる。
     *
     * secure の payload が読めるかどうかだけでは、まだ何も保存されていない場合と、
     * 保存されているが復号できない場合を区別できない。どちらも初期値が返るためである。
     * 保存先に payload が存在するかを先に確かめ、存在するなら secure を選んで上書きしない。
     * 破損していた場合、利用者には未ログインとして見えるが、暗号文は残るため
     * 後から破損として扱えるようにする余地が残る。
     */
    private suspend fun decideRouting(): CookieRouting {
        val payload = runCatching { blobStore.load() }.getOrElse { failure ->
            Napier.w(failure) { "Failed to read the secure Cookie payload." }
            return CookieRouting.Secure
        }

        if (payload.isLogoutInProgress) {
            onMigrationEvent(CookieMigrationEvent.LogoutResumed)

            // 両方を消し終えるまで印を残す。消し残したまま印を外すと、
            // 次回起動時に残った資格情報を移行してしまう。
            if (clearBothStores()) runCatching { blobStore.clear() }

            return CookieRouting.Secure
        }

        if (!payload.isEmpty || blobStore.hasStoredPayload()) return CookieRouting.Secure

        return migrateFromLegacy()
    }

    private suspend fun migrateFromLegacy(): CookieRouting {
        val legacyStorage = runCatching { legacyStorageFactory() }.getOrElse { failure ->
            Napier.w(failure) { "Failed to open the legacy Cookie storage." }
            null
        }

        if (legacyStorage == null) {
            runCatching { blobStore.save(SecureCookiePayload(isMigrationCompleted = true)) }

            return CookieRouting.Secure
        }

        // 読めなかった保存先は使えないため閉じて secure を選ぶ。移行の印は書かないので、
        // 次回起動で読み出しからやり直す。
        val legacyRecords = runCatching { legacyStorage.snapshot() }.getOrElse { failure ->
            Napier.w(failure) { "Failed to read the legacy Cookie storage." }
            onMigrationEvent(CookieMigrationEvent.FallbackUsed(MigrationStage.LEGACY_READ))
            legacyStorage.closeQuietly()

            return CookieRouting.Secure
        }

        if (legacyRecords.isEmpty()) {
            runCatching { blobStore.save(SecureCookiePayload(isMigrationCompleted = true)) }
            legacyStorage.closeQuietly()

            return CookieRouting.Secure
        }

        return migrateRecords(legacyRecords, legacyStorage)
    }

    /**
     * Room のレコードを secure へ移す。
     *
     * 読み出した内容と secure へ渡す内容が一致することは、同一プロセス内の値として確かめる。
     * 保存後の読み直しでは確かめられない。secure store は書き込みをメモリ上の控えへ先に反映し、
     * 読み出しはその控えから返すため、書いた直後の読み直しは保存先の状態を表さない。
     * 代わりに保存の完了そのものを根拠とする。保存が失敗した場合は例外になるため、
     * 例外なく戻ったことが保存済みであることを示す。
     */
    private suspend fun migrateRecords(
        legacyRecords: List<FanboxCookieRecord>,
        legacyStorage: LegacyCookieStorage,
    ): CookieRouting {
        onMigrationEvent(CookieMigrationEvent.Started(legacyRecords.size))

        val payload = SecureCookiePayload(
            records = legacyRecords.map { it.canonicalized().toSecureCookieRecord() }.deduplicated(),
            isMigrationCompleted = true,
        )

        runCatching { blobStore.save(payload) }.onFailure { failure ->
            Napier.w(failure) { "Failed to commit the migrated Cookie payload; keeping the legacy storage." }
            onMigrationEvent(CookieMigrationEvent.FallbackUsed(MigrationStage.SECURE_COMMIT))

            return CookieRouting.Legacy(legacyStorage)
        }

        runCatching { legacyStorage.clear() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the legacy Cookie storage after migration." }
            onMigrationEvent(CookieMigrationEvent.CleanupPending)
        }

        legacyStorage.closeQuietly()
        onMigrationEvent(CookieMigrationEvent.Succeeded(payload.records.size))

        return CookieRouting.Secure
    }

    /**
     * secure と、まだ残っていれば Room の両方から Cookie を消す。
     *
     * 片方の削除が失敗しても、もう片方の削除は行う。資格情報の消し残しを減らすため。
     * 両方を消し終えた場合だけ true を返す。呼び出し元はこれが true の場合だけ
     * ログアウト中の印を外す。消し残したまま印を外すと、次回起動時に残りが移行されるため。
     */
    private suspend fun clearBothStores(): Boolean {
        val isSecureCleared = runCatching { secureStorage.clear() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the secure Cookie storage." }
        }.isSuccess

        val openedStorage = (routing as? CookieRouting.Legacy)?.legacyStorage
        val legacyStorage = openedStorage ?: runCatching { legacyStorageFactory() }.getOrElse { failure ->
            Napier.w(failure) { "Failed to open the legacy Cookie storage during logout." }

            return false
        } ?: return isSecureCleared

        val isLegacyCleared = runCatching { legacyStorage.clear() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the legacy Cookie storage." }
        }.isSuccess

        legacyStorage.closeQuietly()

        return isSecureCleared && isLegacyCleared
    }

    private fun LegacyCookieStorage.closeQuietly() {
        runCatching { close() }.onFailure { failure ->
            Napier.w(failure) { "Failed to close the legacy Cookie storage." }
        }
    }

    /** 決定した保存先。 */
    private sealed interface CookieRouting {
        data object Secure : CookieRouting

        data class Legacy(val legacyStorage: LegacyCookieStorage) : CookieRouting
    }

    private fun CookieRouting.storage(): FanboxCookieStorage = when (this) {
        is CookieRouting.Secure -> secureStorage
        is CookieRouting.Legacy -> legacyStorage
    }
}

/**
 * 旧 Room の Cookie 保存先。
 *
 * fankt の `RoomFanboxCookieStorage` を、移行に必要な操作だけに絞って受け取るための型。
 * テストから差し替えられるように interface で切っている。
 */
internal interface LegacyCookieStorage : FanboxCookieStorage, AutoCloseable

/** 移行の経過。Cookie の値や、資格情報から導いた識別子を含めないこと。 */
internal sealed interface CookieMigrationEvent {
    data class Started(val recordCount: Int) : CookieMigrationEvent
    data class Succeeded(val recordCount: Int) : CookieMigrationEvent
    data class FallbackUsed(val stage: MigrationStage) : CookieMigrationEvent
    data object CleanupPending : CookieMigrationEvent
    data object LogoutResumed : CookieMigrationEvent
}

/** 移行のどの段階で失敗したかを表す。 */
internal enum class MigrationStage {
    LEGACY_READ,
    SECURE_COMMIT,
    LEGACY_CLEANUP,
}
