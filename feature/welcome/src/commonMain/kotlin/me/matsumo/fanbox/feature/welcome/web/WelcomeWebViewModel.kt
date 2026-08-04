package me.matsumo.fanbox.feature.welcome.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.multiplatform.webview.cookie.Cookie as WebViewCookie

class WelcomeWebViewModel(
    private val fanboxRepository: FanboxRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    /**
     * Cookie を保存する。保存できた場合だけ true を返す。
     *
     * 保存先が資格情報を受け取れない場合、保存は例外になる。呼び出し元がログイン失敗として
     * 扱えるよう、例外を投げずに戻り値で伝える。
     */
    @OptIn(ExperimentalTime::class)
    suspend fun saveCookies(cookies: List<WebViewCookie>): Boolean {
        return suspendRunCatching {
            fanboxRepository.setCookies(cookies.toFanboxCookieRecords(Clock.System.now().toEpochMilliseconds()))
        }.isSuccess
    }

    suspend fun checkSessionId(cookies: List<WebViewCookie>): Boolean {
        if (!saveCookies(cookies)) return false

        return suspendRunCatching {
            fanboxRepository.updateCsrfToken()
            fanboxRepository.getNewsLetters()
        }.onFailure {
            saveCookies(emptyList())
        }.isSuccess
    }

    fun debugLogin() {
        viewModelScope.launch {
            settingRepository.setTestUser(true)
            settingRepository.setFollowTabDefaultHome(true)
        }
    }
}
