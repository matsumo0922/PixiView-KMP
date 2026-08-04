package me.matsumo.fanbox.core.datastore.cookie

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.matsumo.fankt.fanbox.FanboxCookieRecord
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 旧 Room から secure store への移行を検証するテスト。
 *
 * 受け入れ条件は「既存ユーザーがアプリ更新後もログイン状態を維持している」ことと、
 * 「新規ログインが secure store のみに保存される」ことである。
 */
class MigratingFanboxCookieStorageTest {

    private val sessionCookie = FanboxCookieRecord(
        domain = "fanbox.cc",
        path = "/",
        name = "FANBOXSESSID",
        value = "session-value",
        expiresAtEpochMilliseconds = 1_800_000_000_000L,
        secure = true,
        hostOnly = false,
    )

    private fun storage(
        blobStore: FakeSecureCookieBlobStore,
        legacyStorage: FakeLegacyCookieStorage? = null,
        events: MutableList<CookieMigrationEvent> = mutableListOf(),
    ) = MigratingFanboxCookieStorage(
        secureStorage = SecureFanboxCookieStorage(blobStore),
        blobStore = blobStore,
        legacyStorageFactory = { legacyStorage },
        onMigrationEvent = { events.add(it) },
    )

    @Test
    fun legacySessionSurvivesTheUpgrade() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))

        val migrated = storage(blobStore, legacyStorage).snapshot()

        assertEquals(listOf(sessionCookie), migrated)
        assertEquals(listOf(sessionCookie.toSecureCookieRecord()), blobStore.storedPayload().records)
    }

    @Test
    fun legacyStorageIsClearedAfterTheSecureCommit() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))

        storage(blobStore, legacyStorage).snapshot()

        assertEquals(emptyList(), legacyStorage.storedRecords())
        assertTrue(legacyStorage.isClosed)
    }

    @Test
    fun migrationMarksItselfCompleted() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()

        storage(blobStore, FakeLegacyCookieStorage(listOf(sessionCookie))).snapshot()

        assertTrue(blobStore.storedPayload().isMigrationCompleted)
    }

    @Test
    fun legacyStorageIsKeptWhenTheSecureCommitFails() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore().apply { saveFailure = IllegalStateException("commit failed") }
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))

        val records = storage(blobStore, legacyStorage).snapshot()

        assertEquals(listOf(sessionCookie), records)
        assertEquals(listOf(sessionCookie), legacyStorage.storedRecords())
        assertFalse(legacyStorage.isClosed)
    }

    @Test
    fun upsertAfterAFailedMigrationNeverWritesToTheLegacyStore() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore().apply { saveFailure = IllegalStateException("commit failed") }
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))
        val storage = storage(blobStore, legacyStorage)

        storage.snapshot()

        val newCookie = sessionCookie.copy(value = "new-session")

        assertFails { storage.upsert(newCookie) }
        assertEquals(listOf(sessionCookie), legacyStorage.storedRecords())
    }

    @Test
    fun replaceAllAfterAFailedMigrationNeverWritesToTheLegacyStore() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore().apply { saveFailure = IllegalStateException("commit failed") }
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))
        val storage = storage(blobStore, legacyStorage)

        storage.snapshot()

        assertFails { storage.replaceAll(listOf(sessionCookie.copy(value = "new-session"))) }
        assertEquals(listOf(sessionCookie), legacyStorage.storedRecords())
    }

    @Test
    fun writingAfterTheSecureStoreRecoversMigratesAndUsesTheSecureStore() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore().apply { saveFailure = IllegalStateException("commit failed") }
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))
        val storage = storage(blobStore, legacyStorage)

        storage.snapshot()
        blobStore.saveFailure = null

        val newCookie = sessionCookie.copy(value = "new-session")
        storage.upsert(newCookie)

        assertEquals(listOf(newCookie.toSecureCookieRecord()), blobStore.storedPayload().records)
        assertEquals(emptyList(), legacyStorage.storedRecords())
    }

    @Test
    fun failedMigrationIsReportedAsFallback() = runBlocking {
        val events = mutableListOf<CookieMigrationEvent>()
        val blobStore = FakeSecureCookieBlobStore().apply { saveFailure = IllegalStateException("commit failed") }

        storage(blobStore, FakeLegacyCookieStorage(listOf(sessionCookie)), events).snapshot()

        assertTrue(events.any { it is CookieMigrationEvent.FallbackUsed })
    }

    @Test
    fun migratedStoreIsNotMigratedAgain() = runBlocking {
        val payload = SecureCookiePayload(
            records = listOf(sessionCookie.toSecureCookieRecord()),
            isMigrationCompleted = true,
        )
        val blobStore = FakeSecureCookieBlobStore(payload)
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie.copy(value = "stale")))

        val records = storage(blobStore, legacyStorage).snapshot()

        assertEquals("session-value", records.single().value)
        assertEquals(0, blobStore.saveCount)
    }

    @Test
    fun unreadablePayloadIsNotOverwritten() = runBlocking {
        val payload = SecureCookiePayload(
            records = listOf(sessionCookie.toSecureCookieRecord()),
            isMigrationCompleted = true,
        )
        val blobStore = FakeSecureCookieBlobStore(payload).apply { isUnreadable = true }

        storage(blobStore, FakeLegacyCookieStorage()).snapshot()

        assertEquals(0, blobStore.saveCount)
        assertEquals(listOf(sessionCookie.toSecureCookieRecord()), blobStore.storedPayload().records)
    }

    @Test
    fun emptyLegacyStoreStillMarksMigrationCompleted() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()

        storage(blobStore, FakeLegacyCookieStorage()).snapshot()

        assertTrue(blobStore.storedPayload().isMigrationCompleted)
    }

    @Test
    fun unreadableLegacyStorageLeavesMigrationRetryable() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))
            .apply { snapshotFailure = IllegalStateException("read failed") }

        storage(blobStore, legacyStorage).snapshot()

        assertEquals(0, blobStore.saveCount)
        assertFalse(blobStore.storedPayload().isMigrationCompleted)
        assertEquals(listOf(sessionCookie), legacyStorage.storedRecords())
    }

    @Test
    fun newLoginIsWrittenOnlyToTheSecureStore() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()
        val legacyStorage = FakeLegacyCookieStorage()
        val storage = storage(blobStore, legacyStorage)

        storage.replaceAll(listOf(sessionCookie))

        assertEquals(listOf(sessionCookie.toSecureCookieRecord()), blobStore.storedPayload().records)
        assertEquals(emptyList(), legacyStorage.storedRecords())
    }

    @Test
    fun cookiesFirstEmissionMatchesTheSecureSnapshot() = runBlocking {
        val payload = SecureCookiePayload(
            records = listOf(sessionCookie.toSecureCookieRecord()),
            isMigrationCompleted = true,
        )
        val blobStore = FakeSecureCookieBlobStore(payload)

        val firstEmission = storage(blobStore, FakeLegacyCookieStorage()).cookies.first()

        assertEquals(listOf(sessionCookie), firstEmission)
    }

    @Test
    fun cookiesFirstEmissionMatchesTheMigratedLegacySnapshot() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()

        val firstEmission = storage(blobStore, FakeLegacyCookieStorage(listOf(sessionCookie))).cookies.first()

        assertEquals(listOf(sessionCookie), firstEmission)
    }

    @Test
    fun cookiesFirstEmissionIsEmptyWithoutAStoredSession() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()

        val firstEmission = storage(blobStore, FakeLegacyCookieStorage()).cookies.first()

        assertEquals(emptyList(), firstEmission)
    }

    @Test
    fun logoutClearsBothStores() = runBlocking {
        val payload = SecureCookiePayload(
            records = listOf(sessionCookie.toSecureCookieRecord()),
            isMigrationCompleted = true,
        )
        val blobStore = FakeSecureCookieBlobStore(payload)
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))

        storage(blobStore, legacyStorage).logout()

        assertEquals(emptyList(), blobStore.storedPayload().records)
        assertEquals(emptyList(), legacyStorage.storedRecords())
    }

    @Test
    fun logoutRemovesTheStoredPayloadEntirely() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore(
            SecureCookiePayload(records = listOf(sessionCookie.toSecureCookieRecord())),
        )

        storage(blobStore, FakeLegacyCookieStorage()).logout()

        assertFalse(blobStore.hasStoredPayload())
    }

    @Test
    fun logoutKeepsTheMarkerWhenTheLegacyClearFails() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore(
            SecureCookiePayload(records = listOf(sessionCookie.toSecureCookieRecord())),
        )
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))
            .apply { clearFailure = IllegalStateException("clear failed") }

        storage(blobStore, legacyStorage).logout()

        assertTrue(blobStore.storedPayload().isLogoutInProgress)
        assertTrue(blobStore.hasStoredPayload())
    }

    @Test
    fun logoutKeepsTheMarkerWhenTheLegacyStoreCannotBeOpened() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore(
            SecureCookiePayload(records = listOf(sessionCookie.toSecureCookieRecord())),
        )
        val storage = MigratingFanboxCookieStorage(
            secureStorage = SecureFanboxCookieStorage(blobStore),
            blobStore = blobStore,
            legacyStorageFactory = { error("cannot open") },
        )

        storage.logout()

        assertTrue(blobStore.storedPayload().isLogoutInProgress)
    }

    @Test
    fun resumedLogoutKeepsTheMarkerWhenTheLegacyClearFails() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore(SecureCookiePayload(isLogoutInProgress = true))
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))
            .apply { clearFailure = IllegalStateException("clear failed") }

        storage(blobStore, legacyStorage).snapshot()

        assertTrue(blobStore.storedPayload().isLogoutInProgress)
    }

    @Test
    fun interruptedLogoutIsResumedOnTheNextLaunch() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore(SecureCookiePayload(isLogoutInProgress = true))
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))

        val records = storage(blobStore, legacyStorage).snapshot()

        assertEquals(emptyList(), records)
        assertEquals(emptyList(), legacyStorage.storedRecords())
    }

    @Test
    fun interruptedLogoutIsReported() = runBlocking {
        val events = mutableListOf<CookieMigrationEvent>()
        val blobStore = FakeSecureCookieBlobStore(SecureCookiePayload(isLogoutInProgress = true))

        storage(blobStore, FakeLegacyCookieStorage(), events).snapshot()

        assertTrue(events.contains(CookieMigrationEvent.LogoutResumed))
    }

    @Test
    fun routingIsDecidedOnlyOnce() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))
        val storage = storage(blobStore, legacyStorage)

        storage.snapshot()
        storage.snapshot()
        storage.snapshot()

        assertEquals(1, legacyStorage.clearCount)
    }

    @Test
    fun migrationSurvivesAFailedLegacyCleanup() = runBlocking {
        val blobStore = FakeSecureCookieBlobStore()
        val legacyStorage = FakeLegacyCookieStorage(listOf(sessionCookie))
            .apply { clearFailure = IllegalStateException("clear failed") }

        val records = storage(blobStore, legacyStorage).snapshot()

        assertEquals(listOf(sessionCookie), records)
        assertEquals(listOf(sessionCookie.toSecureCookieRecord()), blobStore.storedPayload().records)
    }
}
