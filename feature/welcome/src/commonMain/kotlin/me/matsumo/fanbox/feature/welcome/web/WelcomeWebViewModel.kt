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

    @OptIn(ExperimentalTime::class)
    suspend fun saveCookies(cookies: List<WebViewCookie>) {
        fanboxRepository.setCookies(cookies.toFanboxCookieRecords(Clock.System.now().toEpochMilliseconds()))
    }

    suspend fun checkSessionId(cookies: List<WebViewCookie>): Boolean {
        saveCookies(cookies)

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
