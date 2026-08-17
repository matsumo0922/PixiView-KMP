package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.delay
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** developer mode によって参照する配信先が切り替わることを検証するテスト。 */
class GuestChannelSelectionTest {

    /**
     * 分岐が逆になっていると、リリースが昇格前の配信物を実行する。症状は「動いている」であり、
     * 実行されている bundle を見ない限り気付けないため、ここで固定する。
     */
    @Test
    fun developerModeSelectsTheDevChannel() {
        assertTrue(guestManifestUrl(isDeveloperMode = true).contains("/zipline/v1-dev/"))
    }

    @Test
    fun withoutDeveloperModeSelectsThePromotedChannel() {
        assertTrue(guestManifestUrl(isDeveloperMode = false).contains("/zipline/v1/"))
    }

    @Test
    fun storedValueIsUsedWhenItCanBeRead() {
        assertTrue(loadDeveloperMode { true })
        assertFalse(loadDeveloperMode { false })
    }

    /** 読み取りが失敗したときに例外を通すと、起動そのものが落ちる。無効へ倒れることを固定する。 */
    @Test
    fun failureToReadIsTreatedAsDisabled() {
        assertFalse(loadDeveloperMode { error("the stored setting cannot be read") })
    }

    /**
     * 上限を超えた読み取りは打ち切られ、無効になる。打ち切りを無効へ倒さないと、遅いストレージで
     * 起動が止まるか、未検証の配信物が選ばれるかのどちらかになる。
     */
    @Test
    fun readingLongerThanTheLimitIsTreatedAsDisabled() {
        assertFalse(
            loadDeveloperMode {
                delay(READ_LIMIT_EXCEEDING_DURATION_MS)
                true
            },
        )
    }

    private companion object {
        const val READ_LIMIT_EXCEEDING_DURATION_MS = 2000L
    }
}
