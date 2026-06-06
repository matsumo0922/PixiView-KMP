package me.matsumo.fanbox.feature.welcome.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import com.multiplatform.webview.cookie.Cookie as WebViewCookie
import io.ktor.http.Cookie as KtorCookie

class WelcomeWebViewModel(
    private val fanboxRepository: FanboxRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    suspend fun saveCookies(cookies: List<WebViewCookie>) {
        fanboxRepository.setCookies(cookies.toKtorCookies())
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

    private fun List<WebViewCookie>.toKtorCookies(): List<KtorCookie> {
        return map { cookie ->
            KtorCookie(
                name = cookie.name,
                value = cookie.value,
                domain = cookie.domain,
                path = cookie.path,
                secure = cookie.isSecure ?: false,
                httpOnly = cookie.isHttpOnly ?: false,
                maxAge = cookie.maxAge?.toInt(),
                expires = cookie.expiresDate?.let { GMTDate(it) },
            )
        }
    }
}
