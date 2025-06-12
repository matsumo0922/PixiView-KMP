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
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository

class WelcomeLoginViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<Boolean>>(ScreenState.Loading)
    private val _triggerLoginError = Channel<Int>(Channel.BUFFERED)

    val screenState = _screenState.asStateFlow()
    val triggerLoginError = _triggerLoginError.receiveAsFlow()

    fun fetchLoggedIn() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                if (!userDataRepository.userData.first().isTestUser) {
                    fanboxRepository.getNewsLetters()
                    setDefaultHomeTab()
                }
            }.fold(
                onSuccess = { ScreenState.Idle(true) },
                onFailure = { ScreenState.Idle(false) },
            )
        }
    }

    fun setSessionId(sessionId: String) {
        viewModelScope.launch {
            fanboxRepository.setSessionId(sessionId)
            fetchLoggedIn()
        }
    }

    fun logout() {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.logout()
            }
        }
    }

    private suspend fun setDefaultHomeTab() {
        suspendRunCatching {
            fanboxRepository.getSupportedPosts(null, 10)
        }.fold(
            onSuccess = { it.contents.isEmpty() },
            onFailure = { true },
        ).also {
            userDataRepository.setFollowTabDefaultHome(it)
        }
    }
}
