package me.matsumo.fanbox.feature.welcome.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.LoginState
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.multiplatform.webview.cookie.Cookie as WebViewCookie

class WelcomeWebViewModel(private val fanboxRepository: FanboxRepository, private val settingRepository: SettingRepository) : ViewModel() {

    /**
     * Cookie を保存する。保存できた場合だけ true を返す。
     *
     * 保存先が資格情報を受け取れない場合、保存は例外になる。呼び出し元がログイン失敗として
     * 扱えるよう、例外を投げずに戻り値で伝える。
     */
    @OptIn(ExperimentalTime::class)
    suspend fun saveCookies(cookies: List<WebViewCookie>): Boolean = suspendRunCatching {
        fanboxRepository.setCookies(cookies.toFanboxCookieRecords(Clock.System.now().toEpochMilliseconds()))
    }.isSuccess

    /**
     * Cookie を保存し、そのセッションでログインできているかを確かめる。
     *
     * FANBOX がセッションを無効と答えた場合は、保存した Cookie を消す。消さないと、無効なセッションが
     * 次回起動時に使われる。確かめられなかっただけの場合は残す。通信の失敗やメンテナンスのたびに、
     * 有効かもしれないセッションを捨てないため。
     */
    suspend fun checkSessionId(cookies: List<WebViewCookie>): LoginState {
        if (!saveCookies(cookies)) return LoginState.LoggedOut

        val loginState = fanboxRepository.checkLoginState()
        val isSessionRejected = loginState == LoginState.LoggedOut || loginState == LoginState.SessionUnreadable

        if (isSessionRejected) saveCookies(emptyList())

        return loginState
    }

    fun debugLogin() {
        viewModelScope.launch {
            settingRepository.setTestUser(true)
            settingRepository.setFollowTabDefaultHome(true)
        }
    }
}
