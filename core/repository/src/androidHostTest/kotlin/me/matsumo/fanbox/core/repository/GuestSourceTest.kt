package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.model.GuestSource
import me.matsumo.fankt.fanbox.FanboxGuestEvent
import org.junit.Test
import kotlin.test.assertEquals

/** guest の event から、設定画面に出す guest の出所を決める処理を検証するテスト。 */
class GuestSourceTest {

    @Test
    fun loadedBundleBecomesDeliveredWithItsCommit() {
        val event = FanboxGuestEvent.Loaded(version = "0123456789abcdef", isFromCache = false)
        val source = GuestSource.Loading.updatedBy(event)

        assertEquals(
            expected = GuestSource.Delivered(version = "0123456789abcdef", isFromCache = false),
            actual = source,
        )
    }

    @Test
    fun bundleLoadedFromTheCacheIsMarkedAsCached() {
        val event = FanboxGuestEvent.Loaded(version = "0123456789abcdef", isFromCache = true)
        val source = GuestSource.Loading.updatedBy(event)

        assertEquals(
            expected = GuestSource.Delivered(version = "0123456789abcdef", isFromCache = true),
            actual = source,
        )
    }

    @Test
    fun loadFailureBecomesNative() {
        val source = GuestSource.Loading.updatedBy(FanboxGuestEvent.LoadFailed("IllegalStateException"))

        assertEquals(GuestSource.Native, source)
    }

    // 組み込みの guest に切り替わった後の呼び出しの失敗で、表示が Native から変わらないこと。
    @Test
    fun callEventsKeepTheCurrentSource() {
        val source = GuestSource.Native
            .updatedBy(FanboxGuestEvent.SchemaMismatch("post.info"))
            .updatedBy(FanboxGuestEvent.CallFailed(endpoint = "post.info", operation = "parseResponse", errorType = "GuestFailure"))

        assertEquals(GuestSource.Native, source)
    }
}
