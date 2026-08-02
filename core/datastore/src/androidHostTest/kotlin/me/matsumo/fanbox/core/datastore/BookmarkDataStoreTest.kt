package me.matsumo.fanbox.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 旧形式で保存されたブックマークが読み出せることを検証するテスト。
 *
 * fixture は fankt v0.0.21 で実際に `Json.encodeToString` を実行して得た出力に基づく。
 */
class BookmarkDataStoreTest {

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val preferenceFiles = mutableListOf<File>()

    private val legacyJson = """
        {"id":{"value":"1234567","uniqueValue":"post-1234567-fe1d64ca-0564-4001-a9a0-c7aa2a084aca"},"title":"legacy post","cover":null,"user":{"userId":{"value":999},"creatorId":{"value":"creator-abc"},"name":"sample user","iconUrl":null},"excerpt":"e","feeRequired":500,"hasAdultContent":false,"isLiked":false,"isRestricted":false,"likeCount":12,"commentCount":3,"tags":[],"publishedDatetime":"2026-01-02T03:04:05Z","updatedDatetime":"2026-01-03T04:05:06Z"}
    """.trimIndent()

    private val currentJson = """
        {"id":"7654321","title":"current post","cover":null,"user":null,"excerpt":"e","feeRequired":0,"hasAdultContent":false,"isLiked":false,"isRestricted":false,"likeCount":0,"commentCount":0,"tags":[],"publishedDatetime":"2026-01-02T03:04:05Z","updatedDatetime":"2026-01-03T04:05:06Z"}
    """.trimIndent()

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        preferenceFiles.forEach { it.delete() }
    }

    @Test
    fun legacyBookmarkIsRestored() = runBlocking {
        val dataStore = createDataStore()
        dataStore.edit { it[stringPreferencesKey("1234567")] = legacyJson }

        val posts = BookmarkDataStore(createPreferenceHelper(dataStore), Dispatchers.IO).get()

        assertEquals(1, posts.size)
        assertEquals("1234567", posts.single().id.value)
        assertEquals("legacy post", posts.single().title)
        assertEquals(999L, posts.single().user?.userId?.value)
    }

    @Test
    fun legacyBookmarkIsWrittenBackInCurrentFormat() = runBlocking {
        val dataStore = createDataStore()
        dataStore.edit { it[stringPreferencesKey("1234567")] = legacyJson }

        BookmarkDataStore(createPreferenceHelper(dataStore), Dispatchers.IO).get()

        val stored = dataStore.data.first()[stringPreferencesKey("1234567")]

        assertTrue(stored!!.contains(""""id":"1234567""""))
        assertTrue(!stored.contains("uniqueValue"))
    }

    @Test
    fun currentBookmarkIsLeftUnchanged() = runBlocking {
        val dataStore = createDataStore()
        dataStore.edit { it[stringPreferencesKey("7654321")] = currentJson }

        BookmarkDataStore(createPreferenceHelper(dataStore), Dispatchers.IO).get()

        assertEquals(currentJson, dataStore.data.first()[stringPreferencesKey("7654321")])
    }

    @Test
    fun legacyAndCurrentBookmarksAreRestoredTogether() = runBlocking {
        val dataStore = createDataStore()
        dataStore.edit {
            it[stringPreferencesKey("1234567")] = legacyJson
            it[stringPreferencesKey("7654321")] = currentJson
        }

        val posts = BookmarkDataStore(createPreferenceHelper(dataStore), Dispatchers.IO).get()

        assertEquals(setOf("1234567", "7654321"), posts.map { it.id.value }.toSet())
    }

    @Test
    fun undecodableEntryIsSkippedWithoutDroppingOthers() = runBlocking {
        val dataStore = createDataStore()
        dataStore.edit {
            it[stringPreferencesKey("1234567")] = legacyJson
            it[stringPreferencesKey("broken")] = "not json"
        }

        val posts = BookmarkDataStore(createPreferenceHelper(dataStore), Dispatchers.IO).get()

        assertEquals(listOf("1234567"), posts.map { it.id.value })
    }

    @Test
    fun undecodableEntryIsNotRemovedFromStorage() = runBlocking {
        val dataStore = createDataStore()
        dataStore.edit { it[stringPreferencesKey("broken")] = "not json" }

        BookmarkDataStore(createPreferenceHelper(dataStore), Dispatchers.IO).get()

        assertEquals("not json", dataStore.data.first()[stringPreferencesKey("broken")])
    }

    private fun createPreferenceHelper(dataStore: DataStore<Preferences>): PreferenceHelper {
        return object : PreferenceHelper {
            override fun create(name: String): DataStore<Preferences> = dataStore
            override fun delete(name: String) = Unit
        }
    }

    private fun createDataStore(): DataStore<Preferences> {
        val preferenceFile = File.createTempFile(PreferencesName.FANBOX_BOOKMARK, ".preferences_pb")
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
