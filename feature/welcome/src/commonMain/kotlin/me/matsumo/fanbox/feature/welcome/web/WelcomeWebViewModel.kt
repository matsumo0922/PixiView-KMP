package me.matsumo.fanbox.feature.welcome.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository

class WelcomeWebViewModel(
    private val fanboxRepository: FanboxRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    suspend fun saveSessionId(sessionId: String) {
        fanboxRepository.setSessionId(sessionId)
    }

    suspend fun checkSessionId(sessionId: String): Boolean {
        saveSessionId(sessionId)

        return suspendRunCatching {
            fanboxRepository.updateCsrfToken()
            fanboxRepository.getNewsLetters()
        }.onFailure {
            saveSessionId("")
        }.isSuccess
    }

    fun debugLogin() {
        viewModelScope.launch {
            userDataRepository.setTestUser(true)
            userDataRepository.setFollowTabDefaultHome(true)
        }
    }
}
