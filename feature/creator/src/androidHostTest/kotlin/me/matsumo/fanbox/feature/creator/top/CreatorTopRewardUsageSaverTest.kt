package me.matsumo.fanbox.feature.creator.top

import me.matsumo.fanbox.core.model.RewardUsage
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** CreatorTopRewardUsageSaver の保存値と復元値を検証するテスト。 */
class CreatorTopRewardUsageSaverTest {

    @Test
    fun pendingRewardUsageCanBeRestoredFromSavedKey() {
        RewardUsage.entries.forEach { usage ->
            val savedKey = saveCreatorTopRewardUsage(usage)

            assertEquals(usage.storageKey, savedKey)
            assertEquals(usage, restoreCreatorTopRewardUsage(savedKey.orEmpty()))
        }
    }

    @Test
    fun nullOrUnknownRewardUsageDoesNotRestoreUsage() {
        assertNull(saveCreatorTopRewardUsage(null))
        assertNull(restoreCreatorTopRewardUsage("unknown"))
    }
}
