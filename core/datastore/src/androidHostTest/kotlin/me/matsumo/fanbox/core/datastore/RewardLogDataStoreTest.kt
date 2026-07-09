package me.matsumo.fanbox.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import me.matsumo.fanbox.core.model.RewardUsage
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

/** RewardLogDataStore の用途別リワード視聴履歴を検証するテスト。 */
class RewardLogDataStoreTest {

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val preferenceFiles = mutableListOf<File>()

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        preferenceFiles.forEach { it.delete() }
    }

    @Test
    fun rewardedCountsAreSeparatedByUsage() = runBlocking {
        val rewardLogDataStore = RewardLogDataStore(createPreferenceHelper())

        rewardLogDataStore.rewarded(RewardUsage.BulkDownload)
        rewardLogDataStore.rewarded(RewardUsage.CreatorSearch)
        rewardLogDataStore.rewarded(RewardUsage.CreatorSearch)

        assertEquals(1, rewardLogDataStore.getRewardedCount(RewardUsage.BulkDownload))
        assertEquals(2, rewardLogDataStore.getRewardedCount(RewardUsage.CreatorSearch))
        assertEquals(0, rewardLogDataStore.getRewardedCount(RewardUsage.CreatorTranslation))
    }

    @Test
    fun resetClearsAllUsageCounts() = runBlocking {
        val rewardLogDataStore = RewardLogDataStore(createPreferenceHelper())

        RewardUsage.entries.forEach { usage ->
            rewardLogDataStore.rewarded(usage)
        }

        rewardLogDataStore.reset()

        RewardUsage.entries.forEach { usage ->
            assertEquals(0, rewardLogDataStore.getRewardedCount(usage))
        }
    }

    private fun createPreferenceHelper(): PreferenceHelper {
        return object : PreferenceHelper {
            private val stores = mutableMapOf<String, DataStore<Preferences>>()

            override fun create(name: String): DataStore<Preferences> {
                return stores.getOrPut(name) { createDataStore(name) }
            }

            override fun delete(name: String) = Unit
        }
    }

    private fun createDataStore(name: String): DataStore<Preferences> {
        val preferenceFile = File.createTempFile(name, ".preferences_pb")
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
