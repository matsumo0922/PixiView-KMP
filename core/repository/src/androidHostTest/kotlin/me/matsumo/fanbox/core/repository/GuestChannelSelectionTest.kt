package me.matsumo.fanbox.core.repository

import org.junit.Test
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
}
