package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

/** 初回起動からの経過日数の算出を検証するテスト。 */
class SettingDaysSinceFirstLaunchTest {

    @Test
    fun daysSinceFirstLaunchReturnsZeroOnFirstLaunchDay() {
        val setting = Setting.default().copy(firstLaunchTime = FIRST_LAUNCH_EPOCH_SECONDS)

        assertEquals(0, setting.daysSinceFirstLaunch(FIRST_LAUNCH_EPOCH_SECONDS + ONE_DAY_SECONDS - 1L))
    }

    @Test
    fun daysSinceFirstLaunchTruncatesPartialDay() {
        val setting = Setting.default().copy(firstLaunchTime = FIRST_LAUNCH_EPOCH_SECONDS)

        assertEquals(2, setting.daysSinceFirstLaunch(FIRST_LAUNCH_EPOCH_SECONDS + ONE_DAY_SECONDS * 3 - 1L))
    }

    @Test
    fun daysSinceFirstLaunchCountsCompletedDays() {
        val setting = Setting.default().copy(firstLaunchTime = FIRST_LAUNCH_EPOCH_SECONDS)

        assertEquals(3, setting.daysSinceFirstLaunch(FIRST_LAUNCH_EPOCH_SECONDS + ONE_DAY_SECONDS * 3))
    }

    @Test
    fun daysSinceFirstLaunchReturnsMinusOneWhenFirstLaunchIsNotRecorded() {
        val setting = Setting.default().copy(firstLaunchTime = -1L)

        assertEquals(-1, setting.daysSinceFirstLaunch(FIRST_LAUNCH_EPOCH_SECONDS))
    }

    @Test
    fun daysSinceFirstLaunchReturnsZeroWhenClockIsBeforeFirstLaunch() {
        val setting = Setting.default().copy(firstLaunchTime = FIRST_LAUNCH_EPOCH_SECONDS)

        assertEquals(0, setting.daysSinceFirstLaunch(FIRST_LAUNCH_EPOCH_SECONDS - ONE_DAY_SECONDS * 2))
    }

    private companion object {
        const val FIRST_LAUNCH_EPOCH_SECONDS = 1_760_000_000L
        const val ONE_DAY_SECONDS = 86_400L
    }
}
