package me.matsumo.fanbox.core.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import me.matsumo.fanbox.core.datastore.PreferenceHelper
import me.matsumo.fanbox.core.datastore.RewardLogDataStore
import me.matsumo.fanbox.core.model.RewardUsage
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** RewardRepository の用途別リワード視聴状態を検証するテスト。 */
@OptIn(ExperimentalTime::class)
class RewardRepositoryTest {

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val preferenceFiles = mutableListOf<File>()
    private val clock = MutableClock(Instant.parse("2026-07-09T00:00:00Z"))

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        preferenceFiles.forEach { it.delete() }
    }

    @Test
    fun rewardedUsageDoesNotConsumeAnotherUsageOnSameDate() = runBlocking {
        val rewardRepository = createRewardRepository()

        rewardRepository.rewarded(RewardUsage.BulkDownload)

        assertFalse(rewardRepository.isAbleToReward(RewardUsage.BulkDownload))
        assertTrue(rewardRepository.isAbleToReward(RewardUsage.CreatorSearch))
        assertTrue(rewardRepository.isAbleToReward(RewardUsage.CreatorTranslation))
    }

    @Test
    fun rewardCountsAreResetAfterDateChanges() = runBlocking {
        val rewardLogDataStore = RewardLogDataStore(createPreferenceHelper(), clock)
        val rewardRepository = RewardRepositoryImpl(
            rewardLogDataStore = rewardLogDataStore,
            ioDispatcher = Dispatchers.Unconfined,
            clock = clock,
        )

        rewardRepository.rewarded(RewardUsage.BulkDownload)
        rewardRepository.rewarded(RewardUsage.CreatorSearch)
        clock.instant = Instant.parse("2026-07-10T00:00:00Z")

        assertTrue(rewardRepository.isAbleToReward(RewardUsage.BulkDownload))
        assertEquals("2026-07-10", rewardLogDataStore.getRewardDate())
        RewardUsage.entries.forEach { usage ->
            assertEquals(0, rewardLogDataStore.getRewardedCount(usage))
        }
    }

    private fun createRewardRepository(): RewardRepository {
        val rewardLogDataStore = RewardLogDataStore(createPreferenceHelper(), clock)

        return RewardRepositoryImpl(
            rewardLogDataStore = rewardLogDataStore,
            ioDispatcher = Dispatchers.Unconfined,
            clock = clock,
        )
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

    private class MutableClock(
        var instant: Instant,
    ) : Clock {
        override fun now(): Instant {
            return instant
        }
    }
}
