package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.model.GuestSource
import me.matsumo.fankt.fanbox.FanboxGuestEvent
import org.junit.Test
import kotlin.test.assertEquals

/** guest の event から、設定画面に出す guest の出所を決める処理を検証するテスト。 */
class GuestSourceTest {

    @Test
    fun loadedBundleBecomesDeliveredWithItsCommit() {
        val source = GuestSource.Loading.updatedBy(FanboxGuestEvent.Loaded("0123456789abcdef"))

        assertEquals(GuestSource.Delivered("0123456789abcdef"), source)
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
