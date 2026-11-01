package me.matsumo.fanbox.feature.welcome.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.LoginState
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.welcome_login_toast_failed
import me.matsumo.fanbox.feature.welcome.loginFailureMessage
import org.jetbrains.compose.resources.StringResource

class WelcomeLoginViewModel(private val settingRepository: SettingRepository, private val fanboxRepository: FanboxRepository) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<Boolean>>(ScreenState.Loading)
    private val _triggerLoginError = Channel<StringResource>(Channel.BUFFERED)

    val screenState = _screenState.asStateFlow()
    val triggerLoginError = _triggerLoginError.receiveAsFlow()

    fun fetchLoggedIn() {
        viewModelScope.launch { checkLoggedIn() }
    }

    /**
     * セッションを保存し、ログイン状態を取り直す。
     *
     * 保存先が資格情報を受け取れない場合、保存は例外になる。取りこぼすと失敗が画面に出ないため、
     * 受け止めてログイン失敗として扱う。ログインできなかった場合は、その理由を伝える。
     */
    fun setSessionId(sessionId: String) {
        viewModelScope.launch {
            val isSaved = suspendRunCatching { fanboxRepository.setSessionId(sessionId) }.isSuccess
            val failureMessage = if (isSaved) checkLoggedIn() else Res.string.welcome_login_toast_failed

            if (failureMessage != null) {
                _screenState.value = ScreenState.Idle(false)
                _triggerLoginError.send(failureMessage)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.logout()
            }
        }
    }

    /**
     * ログインしているかを確かめて画面へ反映する。ログインしていない場合は、利用者へ見せる理由を返す。
     *
     * テスト用のユーザーは確かめずにログイン中とみなす。
     */
    private suspend fun checkLoggedIn(): StringResource? {
        _screenState.value = ScreenState.Loading

        if (settingRepository.setting.first().isTestUser) {
            _screenState.value = ScreenState.Idle(true)
            return null
        }

        val loginState = fanboxRepository.checkLoginState()
        val isLoggedIn = loginState is LoginState.LoggedIn

        if (isLoggedIn) setDefaultHomeTab()

        _screenState.value = ScreenState.Idle(isLoggedIn)

        return if (isLoggedIn) null else loginState.loginFailureMessage()
    }

    private suspend fun setDefaultHomeTab() {
        suspendRunCatching {
            fanboxRepository.getSupportedPosts(null, 10)
        }.fold(
            onSuccess = { it.contents.isEmpty() },
            onFailure = { true },
        ).also {
            settingRepository.setFollowTabDefaultHome(it)
        }
    }
}
