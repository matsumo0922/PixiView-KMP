package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.model.fanbox.MetaData
import kotlin.test.Test
import kotlin.test.assertEquals

/** ログインを確かめ直した結果から、次の状態を決める処理を検証するテスト。 */
class LoginStateTest {

    private val loggedIn = LoginState.LoggedIn(MetaData(apiUrl = null, context = null, csrfToken = "token"))

    // 使っている途中でメンテナンスや通信断が起きても、ログイン中のままにする。
    @Test
    fun loggedInStaysLoggedInWhenTheCheckCannotBeCompleted() {
        val checked = LoginState.Unverified(FanboxErrorKind.ServerError)

        assertEquals(loggedIn, loggedIn.updatedBy(checked))
    }

    @Test
    fun loggedInBecomesLoggedOutWhenFanboxRejectsTheSession() {
        assertEquals(LoginState.LoggedOut, loggedIn.updatedBy(LoginState.LoggedOut))
    }

    // 起動時の確認や、エラー画面からの再試行では、確かめた結果をそのまま使う。
    @Test
    fun otherStatesTakeTheCheckedState() {
        val unverified = LoginState.Unverified(FanboxErrorKind.Network)

        assertEquals(unverified, LoginState.LoggedOut.updatedBy(unverified))
        assertEquals(loggedIn, unverified.updatedBy(loggedIn))
        assertEquals(LoginState.LoggedOut, LoginState.SessionUnreadable.updatedBy(LoginState.LoggedOut))
    }

    @Test
    fun failureThatIsNotFromFanboxLeavesTheSessionUnverified() {
        assertEquals(LoginState.Unverified(FanboxErrorKind.Unknown), IllegalStateException().toLoginState())
    }
}
