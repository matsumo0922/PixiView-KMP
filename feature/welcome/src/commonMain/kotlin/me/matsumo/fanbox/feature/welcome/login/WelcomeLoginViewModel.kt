package me.matsumo.fanbox.feature.welcome.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import kotlin.random.Random

class WelcomeLoginViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _isLoggedInFlow = MutableSharedFlow<Boolean>(replay = 1)
    private val _triggerLoginError = Channel<Int>(Channel.BUFFERED)

    val isLoggedInFlow = _isLoggedInFlow.asSharedFlow()
    val triggerLoginError = _triggerLoginError.receiveAsFlow()

    fun fetchLoggedIn() {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.updateCsrfToken()
                fanboxRepository.getNewsLetters()
                setDefaultHomeTab()
            }.isSuccess.also {
                _isLoggedInFlow.emit(it)
            }
        }
    }

    fun setSessionId(sessionId: String) {
        viewModelScope.launch {
            suspendRunCatching {
                // fanboxRepository.updateCookie("FANBOXSESSID=$sessionId;")
                fanboxRepository.updateCsrfToken()
                fanboxRepository.getNewsLetters()
                setDefaultHomeTab()
            }.onSuccess {
                _isLoggedInFlow.emit(true)
            }.onFailure {
                _triggerLoginError.send(Random.nextInt())
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching {
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
