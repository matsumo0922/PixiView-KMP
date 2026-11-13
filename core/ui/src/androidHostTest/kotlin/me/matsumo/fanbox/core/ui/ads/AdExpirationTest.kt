package me.matsumo.fanbox.core.ui.ads

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** ロード済み広告の失効判定を検証するテスト。 */
class AdExpirationTest {

    @Test
    fun adBeforeExpirationIsNotExpired() {
        val loadedAt = 1_000L
        val beforeExpiration = loadedAt + AD_EXPIRATION_DURATION.inWholeMilliseconds - 1

        val isExpired = isAdExpired(
            loadedAtElapsedRealtime = loadedAt,
            currentElapsedRealtime = beforeExpiration,
        )

        assertFalse(isExpired)
    }

    @Test
    fun adPastExpirationIsExpired() {
        val loadedAt = 1_000L
        val pastExpiration = loadedAt + AD_EXPIRATION_DURATION.inWholeMilliseconds + 1

        val isExpired = isAdExpired(
            loadedAtElapsedRealtime = loadedAt,
            currentElapsedRealtime = pastExpiration,
        )

        assertTrue(isExpired)
    }
}
