package me.matsumo.fanbox.feature.welcome.login

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class WelcomeLoginViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _isLoggedInFlow = MutableSharedFlow<Boolean>(replay = 1)

    val isLoggedInFlow = _isLoggedInFlow.asSharedFlow()

    fun fetchLoggedIn() {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.updateCsrfToken()
                fanboxRepository.getNewsLetters()

                fanboxRepository.metaData.firstOrNull()?.also {
                    userDataRepository.setTestUser(it.context.user.userId == "100912340")
                }
            }.isSuccess.also {
                _isLoggedInFlow.emit(it)
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
