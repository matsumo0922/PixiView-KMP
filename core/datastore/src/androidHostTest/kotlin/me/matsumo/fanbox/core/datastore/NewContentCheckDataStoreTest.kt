package me.matsumo.fanbox.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import me.matsumo.fanbox.core.model.NewContentType
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 新着の定期確認の記録が、確認の途中で消されたときに書き戻されないことを検証するテスト。 */
// テスト自身が Dispatcher の注入元となり、DataStore の実 I/O を使う。
@Suppress("InjectDispatcher")
@OptIn(ExperimentalTime::class)
class NewContentCheckDataStoreTest {

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val preferenceFiles = mutableListOf<File>()
    private val createdAt = Instant.parse("2026-11-01T12:00:00Z")

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        preferenceFiles.forEach { it.delete() }
    }

    @Test
    fun recordIsSavedWhenTheGenerationIsUnchanged() = runBlocking {
        val dataStore = createNewContentCheckDataStore()
        val generation = dataStore.getRecordGeneration(NewContentType.MESSAGE)

        val isSaved = dataStore.setLastSeenCreatedAtIfGeneration(NewContentType.MESSAGE, createdAt, generation)

        assertTrue(isSaved)
        assertEquals(createdAt, dataStore.getLastSeenCreatedAt(NewContentType.MESSAGE))
    }

    @Test
    fun recordClearedDuringTheCheckIsNotWrittenBack() = runBlocking {
        val dataStore = createNewContentCheckDataStore()
        val generation = dataStore.getRecordGeneration(NewContentType.MESSAGE)

        dataStore.clearLastSeenCreatedAt(NewContentType.MESSAGE)
        val isSaved = dataStore.setLastSeenCreatedAtIfGeneration(NewContentType.MESSAGE, createdAt, generation)

        assertFalse(isSaved)
        assertNull(dataStore.getLastSeenCreatedAt(NewContentType.MESSAGE))
    }

    @Test
    fun clearingOneRecordDoesNotInvalidateTheOther() = runBlocking {
        val dataStore = createNewContentCheckDataStore()
        val postGeneration = dataStore.getRecordGeneration(NewContentType.POST)

        dataStore.clearLastSeenCreatedAt(NewContentType.MESSAGE)
        val isSaved = dataStore.setLastSeenCreatedAtIfGeneration(NewContentType.POST, createdAt, postGeneration)

        assertTrue(isSaved)
        assertEquals(createdAt, dataStore.getLastSeenCreatedAt(NewContentType.POST))
    }

    private fun createNewContentCheckDataStore(): NewContentCheckDataStore {
        val dataStore = createDataStore()
        val preferenceHelper = object : PreferenceHelper {
            override fun create(name: String): DataStore<Preferences> = dataStore
            override fun delete(name: String) = Unit
        }

        return NewContentCheckDataStore(preferenceHelper, Dispatchers.IO)
    }

    private fun createDataStore(): DataStore<Preferences> {
        val preferenceFile = File.createTempFile(PreferencesName.NEW_CONTENT_CHECK, ".preferences_pb")
        preferenceFile.delete()
        preferenceFiles.add(preferenceFile)

        return PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = null,
            migrations = emptyList(),
            scope = dataStoreScope,
            produceFile = { preferenceFile.absolutePath.toPath() },
        )
    }
}
