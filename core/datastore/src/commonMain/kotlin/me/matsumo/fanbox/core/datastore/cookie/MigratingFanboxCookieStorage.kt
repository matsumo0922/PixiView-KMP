package me.matsumo.fanbox.core.datastore.cookie

import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
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
@OptIn(ExperimentalCoroutinesApi::class)
class MigratingFanboxCookieStorage internal constructor(
    private val secureStorage: SecureFanboxCookieStorage,
    private val blobStore: SecureCookieBlobStore,
    private val legacyStorageFactory: () -> LegacyCookieStorage?,
    private val clearPreRoomSource: suspend () -> Unit = {},
    private val onMigrationEvent: (CookieMigrationEvent) -> Unit = {},
) : FanboxCookieStorage {

    private val routingMutex = Mutex()
    private val routingState = MutableStateFlow<CookieRouting?>(null)

    /**
     * このプロセスが開いたまま持っている旧 Room。
     *
     * 同じ `fankt.db` に対して実体を 2 つ開いてはいけないため、閉じずに残した実体は
     * ここで持ち続けて使い回す。保存先が secure へ切り替わって [routing] から外れた後も、
     * ログアウト時の削除はこの実体で行う。
     */
    private var openedLegacyStorage: LegacyCookieStorage? = null

    private var routing: CookieRouting?
        get() = routingState.value
        set(value) {
            routingState.value = value
        }

    /**
     * 現在の Cookie を流す。
     *
     * 保存先が決まるまで待ってから、決まった保存先の Flow を流す。決まる前に空リストを
     * 流さないこと。購読者は最初に受け取った値を現在の状態として扱うため、保存済みの
     * セッションがある端末で一度未ログインとして観測されてしまう。
     *
     * 保存先は移行のやり直しで Room から secure へ変わりうる。切り替わった後も購読を
     * 続けられるよう、保存先そのものの変化を購読して流し直す。
     */
    override val cookies: Flow<List<FanboxCookieRecord>> = flow {
        readStorage()

        emitAll(
            routingState.filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { it.storage().cookies },
        )
    }

    override suspend fun snapshot(): List<FanboxCookieRecord> = routingMutex.withLock {
        // 移行のやり直しが Room を閉じるのと競合しないよう、読み出しも直列化する。
        resolveRoutingLocked().storage().snapshot()
    }

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

    /**
     * Room 導入より前の保存先に残っているセッションを取り込む。取り込めた場合は true を返す。
     *
     * 読み出しから取り込み元の削除までを、ログアウトと同じ [routingMutex] の下で行う。
     * 読み出した後にログアウトが挟まると、消えたはずのセッションを取り込んで復活させるため。
     *
     * 取り込み元が空の場合は何もせず true を返す。
     */
    suspend fun importPreRoomSession(readPreRoomSession: suspend () -> FanboxCookieRecord?): Boolean {
        return routingMutex.withLock {
            val record = runCatching { readPreRoomSession() }.getOrElse { failure ->
                Napier.w(failure) { "Failed to read the pre-Room Cookie source." }

                return@withLock false
            } ?: return@withLock true

            runCatching {
                resolveRoutingLocked().let { if (it is CookieRouting.Legacy) retryMigration(it.legacyStorage) }
                requireCompletedLogout()
                secureStorage.upsert(record)
                clearPreRoomSource()
            }.onFailure { failure ->
                Napier.w(failure) { "Failed to import the pre-Room session; keeping the source for a later retry." }
            }.isSuccess
        }
    }

    /** 保存先を決めさせる。移行に失敗した場合は Room を読み続ける。 */
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
            val routing = resolveRoutingLocked()

            if (routing is CookieRouting.Legacy) retryMigration(routing.legacyStorage)

            requireCompletedLogout()

            block(secureStorage)
        }
    }

    /**
     * ログアウトの後始末が終わっていることを確かめる。
     *
     * 印が残ったまま新しいセッションを保存すると、次回起動時にログアウトの続きとみなされて
     * 消される。保存の前に後始末をやり直し、それも終わらない場合は例外を投げる。
     */
    private suspend fun requireCompletedLogout() {
        val payload = runCatching { blobStore.load() }.getOrElse { failure ->
            // 印の有無を確かめられない間は保存しない。読めない理由が一時的なものでも、
            // 実際には印が立っていた場合、保存したセッションは次回起動時に消えるため。
            Napier.w(failure) { "Failed to read the logout state before writing." }

            error("Failed to read the logout state; refusing to store a session that the next launch might delete.")
        }

        // 保存先に値があるのに初期値が返る場合は復号できていない。印が立っているかを
        // 判断できないため、読めなかった場合と同じく保存しない。
        if (payload.isEmpty && blobStore.hasStoredPayload()) {
            error("Failed to decode the logout state; refusing to store a session that the next launch might delete.")
        }

        if (!payload.isLogoutInProgress) return

        if (clearBothStores()) {
            runCatching { blobStore.clear() }.onSuccess { return }
        }

        error("Failed to finish the pending logout; refusing to store a session that the next launch would delete.")
    }

    /**
     * 移行をやり直す。成功したら保存先を secure へ切り替える。
     *
     * 失敗した場合は、呼び出し元の書き込みを Room へ落とさないため例外を投げる。
     *
     * ここでは Room を閉じない。Room の Flow は閉じられると例外で終わる契約であり、
     * すでに Cookie を購読している相手がいた場合、保存先の切り替えを受け取る前に
     * 購読ごと終わってしまう。閉じるのは起動直後の移行だけとし、やり直しの場合は
     * プロセスが終わるまで開いたままにする。
     */
    private suspend fun retryMigration(legacyStorage: LegacyCookieStorage) {
        val legacyRecords = legacyStorage.snapshot()

        val payload = SecureCookiePayload(
            records = legacyRecords.map { it.canonicalized().toSecureCookieRecord() }.deduplicated(),
            isMigrationCompleted = true,
        )

        onMigrationEvent(CookieMigrationEvent.Started(payload.records.size))

        runCatching { blobStore.save(payload) }.onFailure { failure ->
            Napier.w(failure) { "Failed to commit the migrated Cookie payload; keeping the legacy storage." }
            onMigrationEvent(CookieMigrationEvent.FallbackUsed(MigrationStage.SECURE_COMMIT))

            error("Failed to migrate the Cookies into the secure storage; refusing to write them in plain text.")
        }

        routing = CookieRouting.Secure

        runCatching { legacyStorage.clear() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the legacy Cookie storage after migration." }
            onMigrationEvent(CookieMigrationEvent.CleanupPending)
        }

        onMigrationEvent(CookieMigrationEvent.Succeeded(payload.records.size))
    }

    private suspend fun resolveRouting(): CookieRouting {
        routing?.let { return it }

        return routingMutex.withLock { resolveRoutingLocked() }
    }

    /** [routingMutex] を保持した状態で保存先を返す。まだ決まっていなければ決める。 */
    private suspend fun resolveRoutingLocked(): CookieRouting {
        return routing ?: decideRouting().also { routing = it }
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
        // 開けなかった保存先は中身が分からないため、移行の印を書かない。書くと次回起動で
        // 読み直さなくなり、開けなかっただけのセッションを恒久的に見失う。
        val legacyStorage = runCatching { openLegacyStorage() }.getOrElse { failure ->
            Napier.w(failure) { "Failed to open the legacy Cookie storage." }
            onMigrationEvent(CookieMigrationEvent.FallbackUsed(MigrationStage.LEGACY_READ))

            return CookieRouting.Secure
        }

        // 保存先そのものが無い環境。移行するものが無いため印を書いて終わる。
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
     *
     * 読み出しに使っている Room は閉じない。閉じると Cookie の購読が例外で終わり、
     * 保存先が secure へ変わったことを購読者が受け取れなくなる。ここで開いた場合だけ閉じる。
     */
    private suspend fun clearBothStores(): Boolean {
        val isSecureCleared = runCatching { secureStorage.clear() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the secure Cookie storage." }
        }.isSuccess

        // Room 導入より前の保存先も、印が立っている間に消す。印の外で消すと、その間に
        // プロセスが終わった場合に取り込み元だけが残り、次回起動で復活する。
        val isPreRoomSourceCleared = runCatching { clearPreRoomSource() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the pre-Room Cookie source." }
        }.isSuccess

        val wasAlreadyOpen = openedLegacyStorage != null
        val legacyStorage = runCatching { openLegacyStorage() }.getOrElse { failure ->
            Napier.w(failure) { "Failed to open the legacy Cookie storage during logout." }

            return false
        } ?: return isSecureCleared && isPreRoomSourceCleared

        val isLegacyCleared = runCatching { legacyStorage.clear() }.onFailure { failure ->
            Napier.w(failure) { "Failed to clear the legacy Cookie storage." }
        }.isSuccess

        if (!wasAlreadyOpen) legacyStorage.closeQuietly()

        return isSecureCleared && isPreRoomSourceCleared && isLegacyCleared
    }

    /**
     * 旧 Room を開く。既に開いている実体があればそれを返す。
     *
     * 同じ `fankt.db` に対して実体を 2 つ開くと、書き込みが衝突したり、片方の変更が
     * もう片方の Flow に出なかったりする。開く操作はここだけに集める。
     */
    private fun openLegacyStorage(): LegacyCookieStorage? {
        openedLegacyStorage?.let { return it }

        return legacyStorageFactory()?.also { openedLegacyStorage = it }
    }

    private fun LegacyCookieStorage.closeQuietly() {
        if (openedLegacyStorage === this) openedLegacyStorage = null

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
}
