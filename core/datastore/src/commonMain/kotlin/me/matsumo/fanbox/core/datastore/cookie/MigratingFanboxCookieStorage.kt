package me.matsumo.fanbox.core.datastore.cookie

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    override val cookies: Flow<List<FanboxCookieRecord>> = flow {
        emit(emptyList())
        emitAll(activeStorage().cookies)
    }

    override suspend fun snapshot(): List<FanboxCookieRecord> = activeStorage().snapshot()

    override suspend fun upsert(cookie: FanboxCookieRecord) = activeStorage().upsert(cookie)

    override suspend fun delete(domain: String, path: String, name: String) =
        activeStorage().delete(domain, path, name)

    override suspend fun deleteExpired(nowEpochMilliseconds: Long) =
        activeStorage().deleteExpired(nowEpochMilliseconds)

    override suspend fun replaceAll(cookies: List<FanboxCookieRecord>) = activeStorage().replaceAll(cookies)

    override suspend fun clear() = activeStorage().clear()

    /**
     * ログアウトする。
     *
     * secure と Room は別の保存先なので、両方の削除を 1 つの不可分な操作にはできない。
     * 片方だけ消えた状態でプロセスが終わると、残った方から次回起動時にセッションが戻る。
     * これを防ぐため、削除の前にログアウト中の印を secure へ書く。印は両方の削除が
     * 終わってから消すため、途中で終わっても次回起動時にログアウト中だったと分かる。
     */
    suspend fun logout() {
        routingMutex.withLock {
            blobStore.save(SecureCookiePayload(isLogoutInProgress = true))

            clearBothStores()

            blobStore.clear()

            routing = CookieRouting.Secure
        }
    }

    private suspend fun activeStorage(): FanboxCookieStorage = resolveRouting().storage()

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

            clearBothStores()
            runCatching { blobStore.clear() }

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
     */
    private suspend fun clearBothStores() {
        runCatching { secureStorage.clear() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the secure Cookie storage." }
        }

        val openedStorage = (routing as? CookieRouting.Legacy)?.legacyStorage
        val legacyStorage = openedStorage ?: runCatching { legacyStorageFactory() }.getOrNull() ?: return

        runCatching { legacyStorage.clear() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the legacy Cookie storage." }
        }

        legacyStorage.closeQuietly()
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
