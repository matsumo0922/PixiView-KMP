package me.matsumo.fanbox.feature.welcome.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fankt.fanbox.FanboxCookieRecord
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.multiplatform.webview.cookie.Cookie as WebViewCookie

class WelcomeWebViewModel(
    private val fanboxRepository: FanboxRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    suspend fun saveCookies(cookies: List<WebViewCookie>) {
        fanboxRepository.setCookies(cookies.toFanboxCookieRecords())
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

    /**
     * WebView の Cookie を fankt の保存形式へ変換する。
     *
     * WebView 側の `domain` と `path` は、Android で `GET_COOKIE_INFO` に対応していない端末では
     * null になる。取得元は [FANBOX_DOMAIN] のオリジンに限っているため、その場合は同ドメインの
     * ルートパスとして扱う。
     *
     * 有効期限は `expiresDate` が絶対時刻のミリ秒、`maxAge` が現在からの相対秒である。fankt は
     * 絶対時刻を要求するため、`maxAge` しかない場合はここで現在時刻を足して絶対値にする。どちらも
     * 無い Cookie はセッション Cookie であり、期限なしとして渡す。
     *
     * `hostOnly` は Cookie の `domain` 属性の有無で決まる。属性を持つ Cookie はサブドメインにも
     * 送られるため false、持たない Cookie は取得元ホストのみに送られるため true とする。
     */
    @OptIn(ExperimentalTime::class)
    private fun List<WebViewCookie>.toFanboxCookieRecords(): List<FanboxCookieRecord> {
        val nowEpochMilliseconds = Clock.System.now().toEpochMilliseconds()

        return map { cookie ->
            FanboxCookieRecord(
                domain = cookie.domain ?: FANBOX_DOMAIN,
                path = cookie.path ?: "/",
                name = cookie.name,
                value = cookie.value,
                expiresAtEpochMilliseconds = cookie.expiresDate
                    ?: cookie.maxAge?.let { nowEpochMilliseconds + it * MILLISECONDS_PER_SECOND },
                secure = cookie.isSecure ?: false,
                hostOnly = cookie.domain == null,
            )
        }
    }

    private companion object {
        const val FANBOX_DOMAIN = "www.fanbox.cc"
        const val MILLISECONDS_PER_SECOND = 1000L
    }
}
